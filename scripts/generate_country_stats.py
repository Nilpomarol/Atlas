"""Generate assets/data/country_stats.json for Atlas v4.0 country stats.

Sources (build-time only — the app never calls these at runtime, it reads the
bundled JSON):
  * REST Countries v4    — identity, geography, culture, practical facts.
  * World Bank Open Data  — demographics, economy, health, education,
                            environment, infrastructure, inequality, tourism.
                            Uses mrv=5 (most-recent-5-values, paginated) and
                            keeps the latest non-null per country. NOTE: mrnev=1
                            and multi-year date ranges time out badly from some
                            networks — mrv=N is the reliable query.
  * UNDP HDR (CSV)        — HDI, IHDI, GII, GDI, mean/expected years schooling.
  * Curated overrides     — scripts/data/curated_overrides.json (Nobel, UNESCO,
                            CPI, press freedom, democracy index, gov debt/fiscal,
                            Schengen/NATO/OECD, social rights). Built by
                            build_curated_overrides.py.

The spine is our own countries.json (244 entries); iso2 is the join key, iso3
joins to World Bank / IMF / UNDP. Curated fields (Nobel, UNESCO, social rights,
etc., phase 4.1) slot in later with no code change.

See Documentation/Atlas_v4.0_Country_Stats_Spec.md for the field contract.

Usage:
    python scripts/generate_country_stats.py
"""

from __future__ import annotations

import csv
import io
import json
import re
import sys
import time
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COUNTRIES_PATH = ROOT / "app" / "src" / "main" / "assets" / "data" / "countries.json"
OUTPUT_PATH = ROOT / "app" / "src" / "main" / "assets" / "data" / "country_stats.json"
CURATED_PATH = ROOT / "scripts" / "data" / "curated_overrides.json"
DATASET_VERSION = "2026.1"

USER_AGENT = "atlas-stats-generator/1.0"

UNDP_CSV_URL = (
    "https://hdr.undp.org/sites/default/files/2025_HDR/"
    "HDR25_Composite_indices_complete_time_series.csv"
)

# Category display keys (order is imposed by the screen, not the DAO).
CAT_IDENTITAT = "identitat"
CAT_GEOGRAFIA = "geografia"
CAT_DEMOGRAFIA = "demografia"
CAT_SALUT = "salut"
CAT_ECONOMIA = "economia"
CAT_FINANCES = "finances"
CAT_DESENV = "desenvolupament"
CAT_EDUCACIO = "educacio"
CAT_MEDIAMBIENT = "mediambient"
CAT_INFRA = "infraestructura"
CAT_GOVERNANCA = "governanca"
CAT_DESIGUALTAT = "desigualtat"
CAT_CULTURA = "cultura"
CAT_TURISME = "turisme"
CAT_PRACTIC = "practic"
CAT_DRETS = "drets"


# --------------------------------------------------------------------------- #
# HTTP helpers
# --------------------------------------------------------------------------- #
def _get_json(url: str, timeout: int = 25, retries: int = 3):
    last_err = None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return json.load(resp)
        except Exception as err:  # noqa: BLE001 - best-effort fetch with retry
            last_err = err
            time.sleep(1.2 * (attempt + 1))
    raise RuntimeError(f"GET failed after {retries} tries: {url}\n  {last_err}")


def _get_text(url: str, timeout: int = 40, retries: int = 3) -> str:
    last_err = None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
            with urllib.request.urlopen(req, timeout=timeout) as resp:
                return resp.read().decode("utf-8", errors="replace")
        except Exception as err:  # noqa: BLE001
            last_err = err
            time.sleep(1.2 * (attempt + 1))
    raise RuntimeError(f"GET text failed: {url}\n  {last_err}")


def _safe_int(v):
    try:
        return int(str(v))
    except (TypeError, ValueError):
        return None


# --------------------------------------------------------------------------- #
# Source fetchers
# --------------------------------------------------------------------------- #
def fetch_rest_countries(iso2_codes):
    """{iso2: rc_object} via batched /v4/alpha?codes= (full objects)."""
    out = {}
    for i in range(0, len(iso2_codes), 50):
        batch = iso2_codes[i:i + 50]
        url = "https://restcountries.com/v4/alpha?codes=" + ",".join(batch)
        try:
            for obj in _get_json(url):
                if obj.get("cca2"):
                    out[obj["cca2"]] = obj
        except Exception as err:  # noqa: BLE001
            print(f"  RC batch {i} failed: {err}")
        time.sleep(0.3)
    return out


def fetch_wb_mrv(code, mrv=5):
    """{iso3: (value, year)} keeping the latest non-null of the last `mrv` years.
    Paginates; gives up on the indicator (returns partial) rather than hanging."""
    result = {}
    page, pages = 1, 1
    while page <= pages:
        url = (
            f"https://api.worldbank.org/v2/country/all/indicator/{code}"
            f"?format=json&per_page=500&mrv={mrv}&page={page}"
        )
        try:
            data = _get_json(url, timeout=25, retries=3)
        except Exception:  # noqa: BLE001 - skip this indicator on repeated failure
            break
        meta = data[0] if isinstance(data, list) and data else {}
        rows = data[1] if isinstance(data, list) and len(data) > 1 and data[1] else []
        if page == 1:
            pages = meta.get("pages", 1) or 1
        for r in rows:
            iso3 = r.get("countryiso3code")
            val = r.get("value")
            if not iso3 or len(iso3) != 3 or val is None:
                continue
            year = _safe_int(r.get("date"))
            cur = result.get(iso3)
            if cur is None or (year is not None and year > (cur[1] or 0)):
                result[iso3] = (float(val), year)
        page += 1
        time.sleep(0.2)
    return result


def fetch_undp():
    """{metric: {iso3: (value, year)}} for hdi/ihdi/gii/gdi/mys/eys (latest year)."""
    try:
        text = _get_text(UNDP_CSV_URL)
    except Exception as err:  # noqa: BLE001
        print(f"  UNDP CSV failed: {err}")
        return {}
    reader = csv.reader(io.StringIO(text))
    headers = next(reader)
    idx = {h.strip(): i for i, h in enumerate(headers)}
    if "iso3" not in idx:
        print("  UNDP CSV: no iso3 column")
        return {}
    iso3_col = idx["iso3"]

    def year_cols(prefix):
        cols = []
        for h, i in idx.items():
            m = re.match(rf"^{prefix}_(\d{{4}})$", h)
            if m:
                cols.append((int(m.group(1)), i))
        cols.sort(key=lambda x: -x[0])  # newest first
        return cols

    prefixes = {"hdi": "hdi", "ihdi": "ihdi", "gii": "gii",
                "gdi": "gdi", "mys": "mys", "eys": "eys"}
    ycols = {k: year_cols(v) for k, v in prefixes.items()}
    out = {k: {} for k in prefixes}
    for row in reader:
        if len(row) <= iso3_col:
            continue
        iso3 = row[iso3_col].strip()
        if len(iso3) != 3:
            continue
        for metric, cols in ycols.items():
            for year, ci in cols:
                if ci >= len(row):
                    continue
                raw = row[ci].strip()
                if raw in ("", ".."):
                    continue
                try:
                    out[metric][iso3] = (float(raw), year)
                except ValueError:
                    continue
                break
    return out


# --------------------------------------------------------------------------- #
# Catalan formatting + translation
# --------------------------------------------------------------------------- #
def fmt_number(value: float, kind: str) -> str:
    """Catalan locale: thousands '.', decimal ','."""
    if kind == "int":
        s = f"{int(round(value)):,}".replace(",", ".")
        return s
    if kind == "dec2":
        s = f"{value:,.2f}"
    elif kind == "idx3":
        s = f"{value:,.3f}"
    else:  # dec1
        s = f"{value:,.1f}"
    return s.translate(str.maketrans({",": ".", ".": ","}))


BOOL_CA = {True: "Sí", False: "No"}
DRIVING_CA = {"right": "Dreta", "left": "Esquerra"}
WEEKDAY_CA = {
    "monday": "Dilluns", "tuesday": "Dimarts", "wednesday": "Dimecres",
    "thursday": "Dijous", "friday": "Divendres", "saturday": "Dissabte",
    "sunday": "Diumenge",
}

CONTINENT_CA = {
    "Africa": "Àfrica", "Asia": "Àsia", "Europe": "Europa",
    "North America": "Amèrica del Nord", "South America": "Amèrica del Sud",
    "Central America": "Amèrica Central", "Oceania": "Oceania",
    "Antarctica": "Antàrtida", "Americas": "Amèrica",
}

SUBREGION_CA = {
    "Southern Asia": "Àsia meridional", "Western Asia": "Àsia occidental",
    "Eastern Asia": "Àsia oriental", "South-Eastern Asia": "Sud-est asiàtic",
    "Central Asia": "Àsia central", "Northern Europe": "Europa septentrional",
    "Western Europe": "Europa occidental", "Southern Europe": "Europa meridional",
    "Eastern Europe": "Europa oriental", "Southeast Europe": "Sud-est europeu",
    "Northern Africa": "Àfrica septentrional", "Western Africa": "Àfrica occidental",
    "Eastern Africa": "Àfrica oriental", "Middle Africa": "Àfrica central",
    "Southern Africa": "Àfrica meridional", "Northern America": "Amèrica del Nord",
    "Central America": "Amèrica Central", "South America": "Amèrica del Sud",
    "Caribbean": "Carib", "Polynesia": "Polinèsia", "Melanesia": "Melanèsia",
    "Micronesia": "Micronèsia", "Australia and New Zealand": "Austràlia i Nova Zelanda",
}

LANGUAGE_CA = {
    "Spanish": "Castellà", "Catalan": "Català", "Basque": "Basc", "Galician": "Gallec",
    "English": "Anglès", "French": "Francès", "German": "Alemany", "Italian": "Italià",
    "Portuguese": "Portuguès", "Dutch": "Neerlandès", "Russian": "Rus", "Arabic": "Àrab",
    "Chinese": "Xinès", "Mandarin": "Mandarí", "Japanese": "Japonès", "Korean": "Coreà",
    "Hindi": "Hindi", "Bengali": "Bengalí", "Urdu": "Urdú", "Turkish": "Turc",
    "Greek": "Grec", "Polish": "Polonès", "Czech": "Txec", "Slovak": "Eslovac",
    "Hungarian": "Hongarès", "Romanian": "Romanès", "Bulgarian": "Búlgar",
    "Croatian": "Croat", "Serbian": "Serbi", "Slovene": "Eslovè", "Slovenian": "Eslovè",
    "Swedish": "Suec", "Norwegian": "Noruec", "Danish": "Danès", "Finnish": "Finès",
    "Icelandic": "Islandès", "Irish": "Irlandès", "Welsh": "Gal·lès",
    "Ukrainian": "Ucraïnès", "Belarusian": "Bielorús", "Lithuanian": "Lituà",
    "Latvian": "Letó", "Estonian": "Estonià", "Albanian": "Albanès",
    "Macedonian": "Macedoni", "Hebrew": "Hebreu", "Persian": "Persa",
    "Thai": "Tai", "Vietnamese": "Vietnamita", "Indonesian": "Indonesi",
    "Malay": "Malai", "Tagalog": "Tagàlog", "Filipino": "Filipí",
    "Swahili": "Suahili", "Amharic": "Amhàric", "Somali": "Somali",
    "Afrikaans": "Afrikaans", "Zulu": "Zulu", "Hausa": "Hausa",
    "Mongolian": "Mongol", "Nepali": "Nepalès", "Sinhala": "Singalès",
    "Burmese": "Birmà", "Khmer": "Khmer", "Lao": "Laosià", "Armenian": "Armeni",
    "Georgian": "Georgià", "Azerbaijani": "Àzeri", "Kazakh": "Kazakh",
    "Uzbek": "Uzbek", "Maltese": "Maltès", "Luxembourgish": "Luxemburguès",
}

RELIGION_CA = {
    "roman catholicism": "Catolicisme romà", "catholicism": "Catolicisme",
    "christianity": "Cristianisme", "protestantism": "Protestantisme",
    "eastern orthodox": "Cristianisme ortodox", "orthodox": "Cristianisme ortodox",
    "islam": "Islam", "sunni islam": "Islam sunnita", "shia islam": "Islam xiïta",
    "hinduism": "Hinduisme", "buddhism": "Budisme", "judaism": "Judaisme",
    "sikhism": "Sikhisme", "atheist": "Ateisme", "agnostic": "Agnosticisme",
    "irreligion": "Sense religió", "indifferent or irreligious": "Sense religió",
    "folk religion": "Religió tradicional", "other religion": "Altres religions",
    "shinto": "Sintoisme", "taoism": "Taoisme", "animism": "Animisme",
    "bahai": "Bahaisme", "unanswered": "Sense resposta", "none": "Cap",
    "no religion": "Sense religió", "unaffiliated": "No afiliats",
    "other": "Altres", "others": "Altres", "no answer": "Sense resposta",
    "traditional": "Religió tradicional", "confucianism": "Confucianisme",
    "jainism": "Jainisme", "zoroastrianism": "Zoroastrisme",
    "spiritism": "Espiritisme", "evangelicalism": "Evangelisme",
}

# Ethnic group names → Catalan (plural demonyms). Unmatched fall back to Title case.
ETHNICITY_CA = {
    "chinese": "Xinesos", "malay": "Malais", "indian": "Indis", "han": "Han",
    "hausa": "Hausa", "yoruba": "Ioruba", "igbo": "Igbo", "fulani": "Fulbe",
    "tiv": "Tiv", "kanuri": "Kanuri", "ibibio": "Ibibio", "ijaw": "Ijaw",
    "arab": "Àrabs", "berber": "Berbers", "white": "Blancs", "black": "Negres",
    "hispanic": "Hispans", "asian": "Asiàtics", "persian": "Perses",
    "kurd": "Kurds", "kurdish": "Kurds", "pashtun": "Paixtus", "tajik": "Tadjiks",
    "russian": "Russos", "turkish": "Turcs", "turkmen": "Turcmans",
    "mestizo": "Mestissos", "amerindian": "Amerindis", "mulatto": "Mulats",
    "bantu": "Bantus", "khmer": "Khmers", "thai": "Tais", "lao": "Laosians",
    "vietnamese": "Vietnamites", "japanese": "Japonesos", "korean": "Coreans",
    "european": "Europeus", "african": "Africans", "uzbek": "Uzbeks",
    "kazakh": "Kazakhs", "armenian": "Armenis", "georgian": "Georgians",
    "others": "Altres", "other": "Altres", "mixed": "Mestissos",
}

GOV_WORD_CA = {
    "republic": "república", "monarchy": "monarquia", "parliamentary": "parlamentària",
    "presidential": "presidencialista", "constitutional": "constitucional",
    "federal": "federal", "unitary": "unitària", "democracy": "democràcia",
    "democratic": "democràtica", "semi-presidential": "semipresidencialista",
    "absolute": "absoluta", "federation": "federació", "confederation": "confederació",
    "provisional": "provisional", "directorial": "directorial", "state": "estat",
    "dependent": "dependent", "territory": "territori", "communist": "comunista",
    "socialist": "socialista", "islamic": "islàmica", "one-party": "de partit únic",
}


def breakdown_json(entries, translate):
    """Encode a [{name, percentage}] list as a compact JSON-array string for the
    fact `value` column: [{"name": <ca>, "pct": <float>}, ...]. None if empty."""
    items = []
    for e in entries:
        name = e.get("name")
        pct = e.get("percentage")
        if not name or pct is None:
            continue
        items.append({"name": translate(name), "pct": round(float(pct), 1)})
    return json.dumps(items, ensure_ascii=False) if items else None


def translate_gov_type(text):
    if not text:
        return None
    words = text.replace("-", " ").split()
    out, matched = [], False
    for w in words:
        key = w.lower()
        if key in GOV_WORD_CA:
            out.append(GOV_WORD_CA[key])
            matched = True
        else:
            out.append(w.lower())
    if not matched:
        return text
    phrase = " ".join(out)
    return phrase[0].upper() + phrase[1:]


# --------------------------------------------------------------------------- #
# Numeric field catalog
# (key, category, label_ca, source, code, unit, fmt, rankable, direction, tier)
#   source:    "wb" | "imf" | "undp"
#   direction: "high" (bigger ranks #1) | "low" (smaller ranks #1)
#   tier:      "none" | "quantile" | "named" | "hdi"
# --------------------------------------------------------------------------- #
NUMERIC_FIELDS = [
    # Geografia
    ("area", CAT_GEOGRAFIA, "Superfície", "wb", "AG.LND.TOTL.K2", "km²", "int", True, "high", "named"),
    ("agri_land", CAT_GEOGRAFIA, "Terra agrícola", "wb", "AG.LND.AGRI.ZS", "%", "dec1", False, "high", "none"),
    ("arable_land", CAT_GEOGRAFIA, "Terra cultivable", "wb", "AG.LND.ARBL.ZS", "%", "dec1", False, "high", "none"),
    ("forest_land", CAT_GEOGRAFIA, "Bosc", "wb", "AG.LND.FRST.ZS", "%", "dec1", True, "high", "quantile"),
    # Demografia
    ("population", CAT_DEMOGRAFIA, "Població", "wb", "SP.POP.TOTL", "hab.", "int", True, "high", "named"),
    ("pop_growth", CAT_DEMOGRAFIA, "Creixement demogràfic", "wb", "SP.POP.GROW", "%", "dec1", False, "high", "none"),
    ("density", CAT_DEMOGRAFIA, "Densitat", "wb", "EN.POP.DNST", "hab./km²", "dec1", True, "high", "none"),
    ("urban_pct", CAT_DEMOGRAFIA, "Població urbana", "wb", "SP.URB.TOTL.IN.ZS", "%", "dec1", False, "high", "none"),
    ("age_0_14", CAT_DEMOGRAFIA, "Població 0–14 anys", "wb", "SP.POP.0014.TO.ZS", "%", "dec1", False, "high", "none"),
    ("age_15_64", CAT_DEMOGRAFIA, "Població 15–64 anys", "wb", "SP.POP.1564.TO.ZS", "%", "dec1", False, "high", "none"),
    ("age_65_plus", CAT_DEMOGRAFIA, "Població 65+ anys", "wb", "SP.POP.65UP.TO.ZS", "%", "dec1", False, "high", "none"),
    ("fertility", CAT_DEMOGRAFIA, "Taxa de fecunditat", "wb", "SP.DYN.TFRT.IN", "fills/dona", "dec1", False, "high", "none"),
    ("birth_rate", CAT_DEMOGRAFIA, "Natalitat", "wb", "SP.DYN.CBRT.IN", "‰", "dec1", False, "high", "none"),
    ("death_rate", CAT_DEMOGRAFIA, "Mortalitat", "wb", "SP.DYN.CDRT.IN", "‰", "dec1", False, "high", "none"),
    ("net_migration", CAT_DEMOGRAFIA, "Migració neta", "wb", "SM.POP.NETM", "pers.", "int", False, "high", "none"),
    ("female_pct", CAT_DEMOGRAFIA, "Percentatge de dones", "wb", "SP.POP.TOTL.FE.ZS", "%", "dec1", False, "high", "none"),
    # Salut
    ("life_expectancy", CAT_SALUT, "Esperança de vida", "wb", "SP.DYN.LE00.IN", "anys", "dec1", True, "high", "quantile"),
    ("life_exp_male", CAT_SALUT, "Esperança de vida (homes)", "wb", "SP.DYN.LE00.MA.IN", "anys", "dec1", False, "high", "none"),
    ("life_exp_female", CAT_SALUT, "Esperança de vida (dones)", "wb", "SP.DYN.LE00.FE.IN", "anys", "dec1", False, "high", "none"),
    ("infant_mortality", CAT_SALUT, "Mortalitat infantil", "wb", "SP.DYN.IMRT.IN", "‰", "dec1", True, "low", "quantile"),
    ("under5_mortality", CAT_SALUT, "Mortalitat <5 anys", "wb", "SH.DYN.MORT", "‰", "dec1", False, "low", "none"),
    ("maternal_mortality", CAT_SALUT, "Mortalitat materna", "wb", "SH.STA.MMRT", "/100k", "int", False, "low", "none"),
    ("physicians", CAT_SALUT, "Metges", "wb", "SH.MED.PHYS.ZS", "/1000", "dec2", True, "high", "none"),
    ("hospital_beds", CAT_SALUT, "Llits hospitalaris", "wb", "SH.MED.BEDS.ZS", "/1000", "dec1", False, "high", "none"),
    ("health_exp_gdp", CAT_SALUT, "Despesa en salut", "wb", "SH.XPD.CHEX.GD.ZS", "% PIB", "dec1", False, "high", "none"),
    ("health_exp_pc", CAT_SALUT, "Despesa en salut per càpita", "wb", "SH.XPD.CHEX.PC.CD", "USD", "int", False, "high", "none"),
    ("water_access", CAT_SALUT, "Aigua potable", "wb", "SH.H2O.BASW.ZS", "%", "dec1", False, "high", "none"),
    ("sanitation", CAT_SALUT, "Sanejament", "wb", "SH.STA.BASS.ZS", "%", "dec1", False, "high", "none"),
    ("obesity", CAT_SALUT, "Sobrepès (adults)", "wb", "SH.STA.OWAD.ZS", "%", "dec1", False, "high", "none"),
    ("smoking", CAT_SALUT, "Tabaquisme", "wb", "SH.PRV.SMOK", "%", "dec1", False, "low", "none"),
    ("suicide_rate", CAT_SALUT, "Taxa de suïcidis", "wb", "SH.STA.SUIC.P5", "/100k", "dec1", False, "low", "none"),
    # Economia
    ("gdp", CAT_ECONOMIA, "PIB", "wb", "NY.GDP.MKTP.CD", "USD", "int", True, "high", "named"),
    ("gdp_ppp", CAT_ECONOMIA, "PIB (PPA)", "wb", "NY.GDP.MKTP.PP.CD", "USD", "int", True, "high", "named"),
    ("gdp_per_capita", CAT_ECONOMIA, "PIB per càpita", "wb", "NY.GDP.PCAP.CD", "USD", "int", True, "high", "quantile"),
    ("gdp_pc_ppp", CAT_ECONOMIA, "PIB per càpita (PPA)", "wb", "NY.GDP.PCAP.PP.CD", "USD", "int", True, "high", "quantile"),
    ("gdp_growth", CAT_ECONOMIA, "Creixement del PIB", "wb", "NY.GDP.MKTP.KD.ZG", "%", "dec1", False, "high", "none"),
    ("inflation", CAT_ECONOMIA, "Inflació", "wb", "FP.CPI.TOTL.ZG", "%", "dec1", False, "low", "none"),
    ("unemployment", CAT_ECONOMIA, "Atur", "wb", "SL.UEM.TOTL.ZS", "%", "dec1", False, "low", "none"),
    ("gni_per_capita", CAT_ECONOMIA, "RNB per càpita", "wb", "NY.GNP.PCAP.CD", "USD", "int", True, "high", "quantile"),
    ("agri_gdp", CAT_ECONOMIA, "Agricultura", "wb", "NV.AGR.TOTL.ZS", "% PIB", "dec1", False, "high", "none"),
    ("industry_gdp", CAT_ECONOMIA, "Indústria", "wb", "NV.IND.TOTL.ZS", "% PIB", "dec1", False, "high", "none"),
    ("services_gdp", CAT_ECONOMIA, "Serveis", "wb", "NV.SRV.TOTL.ZS", "% PIB", "dec1", False, "high", "none"),
    ("labor_force", CAT_ECONOMIA, "Força laboral", "wb", "SL.TLF.CACT.ZS", "%", "dec1", False, "high", "none"),
    ("exchange_rate", CAT_ECONOMIA, "Tipus de canvi", "wb", "PA.NUS.FCRF", "LCU/USD", "dec2", False, "high", "none"),
    ("rd_expenditure", CAT_ECONOMIA, "Despesa en R+D", "wb", "GB.XPD.RSDV.GD.ZS", "% PIB", "dec2", True, "high", "none"),
    # Finances (gov_debt + fiscal_balance come from curated IMF data — see CURATED_NUMERIC)
    ("exports_gdp", CAT_FINANCES, "Exportacions", "wb", "NE.EXP.GNFS.ZS", "% PIB", "dec1", False, "high", "none"),
    ("imports_gdp", CAT_FINANCES, "Importacions", "wb", "NE.IMP.GNFS.ZS", "% PIB", "dec1", False, "high", "none"),
    ("current_account", CAT_FINANCES, "Compte corrent", "wb", "BN.CAB.XOKA.GD.ZS", "% PIB", "dec1", False, "high", "none"),
    ("fdi_inflows", CAT_FINANCES, "Inversió estrangera directa", "wb", "BX.KLT.DINV.CD.WD", "USD", "int", True, "high", "none"),
    ("tax_revenue", CAT_FINANCES, "Pressió fiscal", "wb", "GC.TAX.TOTL.GD.ZS", "% PIB", "dec1", False, "high", "none"),
    ("reserves", CAT_FINANCES, "Reserves totals", "wb", "FI.RES.TOTL.CD", "USD", "int", True, "high", "none"),
    # Desenvolupament humà (UNDP HDR composite indices)
    ("hdi", CAT_DESENV, "IDH", "undp", "hdi", None, "idx3", True, "high", "hdi"),
    ("ihdi", CAT_DESENV, "IDH ajustat per desigualtat", "undp", "ihdi", None, "idx3", True, "high", "quantile"),
    ("gii", CAT_DESENV, "Índex de desigualtat de gènere", "undp", "gii", None, "idx3", True, "low", "quantile"),
    ("gdi", CAT_DESENV, "Índex de desenvolupament de gènere", "undp", "gdi", None, "idx3", False, "high", "none"),
    # Educació
    ("literacy", CAT_EDUCACIO, "Alfabetització", "wb", "SE.ADT.LITR.ZS", "%", "dec1", True, "high", "quantile"),
    ("edu_expenditure", CAT_EDUCACIO, "Despesa en educació", "wb", "SE.XPD.TOTL.GD.ZS", "% PIB", "dec1", False, "high", "none"),
    ("primary_enroll", CAT_EDUCACIO, "Matriculació primària", "wb", "SE.PRM.ENRR", "%", "dec1", False, "high", "none"),
    ("secondary_enroll", CAT_EDUCACIO, "Matriculació secundària", "wb", "SE.SEC.ENRR", "%", "dec1", False, "high", "none"),
    ("tertiary_enroll", CAT_EDUCACIO, "Matriculació terciària", "wb", "SE.TER.ENRR", "%", "dec1", True, "high", "none"),
    ("compulsory_years", CAT_EDUCACIO, "Anys d'escolaritat obligatòria", "wb", "SE.COM.DURS", "anys", "int", False, "high", "none"),
    ("mean_schooling", CAT_EDUCACIO, "Anys mitjans d'escolaritat", "undp", "mys", "anys", "dec1", True, "high", "none"),
    ("expected_schooling", CAT_EDUCACIO, "Anys esperats d'escolaritat", "undp", "eys", "anys", "dec1", False, "high", "none"),
    # Medi ambient i territori
    ("co2_per_capita", CAT_MEDIAMBIENT, "CO₂ per càpita", "wb", "EN.GHG.CO2.PC.CE.AR5", "t", "dec1", False, "low", "none"),
    ("co2_total", CAT_MEDIAMBIENT, "CO₂ total", "wb", "EN.GHG.CO2.MT.CE.AR5", "Mt", "dec1", False, "low", "none"),
    ("energy_per_capita", CAT_MEDIAMBIENT, "Energia per càpita", "wb", "EG.USE.PCAP.KG.OE", "kg ep", "int", False, "high", "none"),
    ("renewable_energy", CAT_MEDIAMBIENT, "Energia renovable", "wb", "EG.FEC.RNEW.ZS", "%", "dec1", True, "high", "quantile"),
    ("electricity_access", CAT_MEDIAMBIENT, "Accés a electricitat", "wb", "EG.ELC.ACCS.ZS", "%", "dec1", False, "high", "none"),
    ("pm25", CAT_MEDIAMBIENT, "Contaminació PM2.5", "wb", "EN.ATM.PM25.MC.M3", "µg/m³", "dec1", False, "low", "none"),
    ("protected_areas", CAT_MEDIAMBIENT, "Àrees protegides", "wb", "ER.PTD.TOTL.ZS", "%", "dec1", False, "high", "none"),
    ("forest_area_km2", CAT_MEDIAMBIENT, "Superfície forestal", "wb", "AG.LND.FRST.K2", "km²", "int", True, "high", "none"),
    # Infraestructura i tecnologia
    ("internet_users", CAT_INFRA, "Usuaris d'internet", "wb", "IT.NET.USER.ZS", "%", "dec1", True, "high", "quantile"),
    ("mobile_subs", CAT_INFRA, "Subscripcions mòbils", "wb", "IT.CEL.SETS.P2", "/100", "dec1", False, "high", "none"),
    ("broadband", CAT_INFRA, "Banda ampla fixa", "wb", "IT.NET.BBND.P2", "/100", "dec1", True, "high", "none"),
    ("air_passengers", CAT_INFRA, "Passatgers aeris", "wb", "IS.AIR.PSGR", "pass./any", "int", True, "high", "none"),
    # Governança i política
    ("military_exp_gdp", CAT_GOVERNANCA, "Despesa militar", "wb", "MS.MIL.XPND.GD.ZS", "% PIB", "dec1", False, "high", "none"),
    ("military_exp_usd", CAT_GOVERNANCA, "Despesa militar", "wb", "MS.MIL.XPND.CD", "USD", "int", True, "high", "none"),
    # Desigualtat
    ("gini", CAT_DESIGUALTAT, "Índex de Gini", "wb", "SI.POV.GINI", None, "dec1", True, "low", "quantile"),
    ("income_top10", CAT_DESIGUALTAT, "Renda del 10% superior", "wb", "SI.DST.10TH.10", "%", "dec1", False, "high", "none"),
    ("income_bottom10", CAT_DESIGUALTAT, "Renda del 10% inferior", "wb", "SI.DST.FRST.10", "%", "dec1", False, "high", "none"),
    ("poverty_215", CAT_DESIGUALTAT, "Pobresa (2,15 USD/dia)", "wb", "SI.POV.DDAY", "%", "dec1", False, "low", "none"),
    ("poverty_national", CAT_DESIGUALTAT, "Pobresa (línia nacional)", "wb", "SI.POV.NAHC", "%", "dec1", False, "low", "none"),
    # Turisme
    ("tourist_arrivals", CAT_TURISME, "Arribades turístiques", "wb", "ST.INT.ARVL", "pers./any", "int", True, "high", "quantile"),
    ("tourism_receipts", CAT_TURISME, "Ingressos pel turisme", "wb", "ST.INT.RCPT.CD", "USD", "int", True, "high", "none"),
]

# RC fallback for rankable numerics the World Bank may miss (key -> getter).
def _rc_gini(rc):
    arr = rc.get("gini") or []
    if arr:
        item = max(arr, key=lambda x: x.get("year", "0"))
        return float(item["value"]), _safe_int(item.get("year"))
    return None

RC_FALLBACK = {
    "population": lambda rc: (float(rc["population"]), None) if rc.get("population") else None,
    "area": lambda rc: (float(rc["area"]), None) if rc.get("area") else None,
    "density": lambda rc: (float(rc["density"]), None) if rc.get("density") else None,
    "gdp": lambda rc: (float(rc["gdp"]["total"]), None) if rc.get("gdp", {}).get("total") else None,
    "gdp_per_capita": lambda rc: (float(rc["gdp"]["perCapita"]), None) if rc.get("gdp", {}).get("perCapita") else None,
    "gini": _rc_gini,
    "hdi": lambda rc: (float(rc["hdi"]), None) if isinstance(rc.get("hdi"), (int, float)) else None,
}


# --------------------------------------------------------------------------- #
# Curated overrides (scripts/data/curated_overrides.json, keyed by iso2).
# Numeric values are {"v": value, "y": year}; text values are plain strings.
# --------------------------------------------------------------------------- #
# (key, category, label, unit, fmt, rankable, direction, tier)
CURATED_NUMERIC = [
    ("gov_debt", CAT_FINANCES, "Deute públic", "% PIB", "dec1", False, "low", "none"),
    ("fiscal_balance", CAT_FINANCES, "Balanç fiscal", "% PIB", "dec1", False, "high", "none"),
    ("cpi", CAT_GOVERNANCA, "Percepció de corrupció", None, "int", True, "high", "quantile"),
    ("press_freedom", CAT_GOVERNANCA, "Llibertat de premsa", None, "dec1", True, "high", "quantile"),
    ("democracy_index", CAT_GOVERNANCA, "Índex de democràcia", None, "dec2", True, "high", "quantile"),
    ("nobel_laureates", CAT_CULTURA, "Premis Nobel", None, "int", True, "high", "none"),
    ("unesco_sites", CAT_CULTURA, "Patrimoni de la Humanitat", None, "int", True, "high", "none"),
]

# (key, category, label)
CURATED_TEXT = [
    ("highest_point", CAT_GEOGRAFIA, "Punt més alt"),
    ("schengen", CAT_GOVERNANCA, "Espai Schengen"),
    ("nato_member", CAT_GOVERNANCA, "Membre de l'OTAN"),
    ("oecd_member", CAT_GOVERNANCA, "Membre de l'OCDE"),
    ("death_penalty", CAT_DRETS, "Pena de mort"),
    ("same_sex_marriage", CAT_DRETS, "Matrimoni igualitari"),
    ("euthanasia", CAT_DRETS, "Eutanàsia"),
    ("abortion", CAT_DRETS, "Avortament"),
]


def load_curated():
    if not CURATED_PATH.exists():
        print(f"  (no curated file at {CURATED_PATH})")
        return {}
    return json.loads(CURATED_PATH.read_text(encoding="utf-8"))


# --------------------------------------------------------------------------- #
# REST Countries text / categorical facts
# --------------------------------------------------------------------------- #
def rc_text_facts(rc, name_ca_by_iso3):
    facts = []

    def add(cat, key, label, value, unit=None):
        if value:
            facts.append((cat, key, label, value, unit))

    name = rc.get("name") or {}
    add(CAT_IDENTITAT, "official_name", "Nom oficial", name.get("official"))
    cap = rc.get("capital") or []
    add(CAT_IDENTITAT, "capital", "Capital", ", ".join(cap) if cap else None)
    dem = rc.get("demonyms") or []
    eng = next((d for d in dem if d.get("lang") == "eng"), None)
    if eng:
        male, female = eng.get("male"), eng.get("female")
        add(CAT_IDENTITAT, "demonym", "Gentilici",
            male if male == female else f"{male} / {female}")
    gov = rc.get("government") or {}
    add(CAT_IDENTITAT, "government_type", "Forma de govern", translate_gov_type(gov.get("type")))
    add(CAT_IDENTITAT, "anthem", "Himne nacional", rc.get("anthem"))
    add(CAT_IDENTITAT, "olympic_code", "Codi olímpic", rc.get("cioc"))
    add(CAT_IDENTITAT, "fifa_code", "Codi FIFA", rc.get("fifa"))
    coa = rc.get("coatOfArms") or {}
    add(CAT_IDENTITAT, "coat_of_arms", "Escut", coa.get("svg"))

    add(CAT_GEOGRAFIA, "landlocked", "Sense litoral", BOOL_CA.get(bool(rc.get("landlocked"))))
    borders = rc.get("borders") or []
    if borders:
        add(CAT_GEOGRAFIA, "borders", "Fronteres",
            ", ".join(name_ca_by_iso3.get(b, b) for b in borders))
        add(CAT_GEOGRAFIA, "border_count", "Països fronterers", str(len(borders)))

    langs = rc.get("languages") or []
    if langs:
        add(CAT_CULTURA, "languages", "Idiomes oficials",
            ", ".join(LANGUAGE_CA.get(l.get("name"), l.get("name")) for l in langs if l.get("name")))
    curr = rc.get("currencies") or []
    if curr:
        c0 = curr[0]
        nm = (c0.get("name") or "").strip()
        nm = nm[0].upper() + nm[1:] if nm else nm
        code = c0.get("code")
        add(CAT_CULTURA, "currency", "Moneda", f"{nm} ({code})" if code else nm)
    add(CAT_CULTURA, "religion", "Religió",
        breakdown_json(rc.get("religion") or [],
                       lambda n: RELIGION_CA.get(n.lower(), n.title())))
    add(CAT_CULTURA, "national_holiday", "Festa nacional", rc.get("nationalHoliday"))

    add(CAT_DEMOGRAFIA, "ethnic_groups", "Grups ètnics",
        breakdown_json(rc.get("ethnicity") or [],
                       lambda n: ETHNICITY_CA.get(n.lower(), n.title())))

    add(CAT_GOVERNANCA, "un_member", "Membre de l'ONU", BOOL_CA.get(bool(rc.get("unMember"))))
    blocs = rc.get("regionalBlocs") or []
    add(CAT_GOVERNANCA, "eu_member", "Membre de la UE",
        BOOL_CA[any(b.get("acronym") == "EU" for b in blocs)])

    calling = rc.get("callingCodes") or []
    if calling:
        add(CAT_PRACTIC, "calling_code", "Prefix telefònic", "+" + str(calling[0]))
    else:
        idd = rc.get("idd") or {}
        suf = idd.get("suffixes") or []
        if idd.get("root"):
            add(CAT_PRACTIC, "calling_code", "Prefix telefònic",
                idd["root"] + (suf[0] if len(suf) == 1 else ""))
    tld = rc.get("tld") or []
    add(CAT_PRACTIC, "tld", "Domini d'internet", ", ".join(tld) if tld else None)
    car = rc.get("car") or {}
    add(CAT_PRACTIC, "driving_side", "Circulació", DRIVING_CA.get(car.get("side")))
    tz = rc.get("timezones") or []
    add(CAT_PRACTIC, "timezones", "Fus horari", " / ".join(tz) if tz else None)
    add(CAT_PRACTIC, "start_of_week", "Inici de la setmana", WEEKDAY_CA.get(rc.get("startOfWeek")))
    return facts


def spine_text_facts(country):
    facts = []
    cont = country.get("continent")
    if cont:
        facts.append((CAT_GEOGRAFIA, "continent", "Continent", CONTINENT_CA.get(cont, cont), None))
    sub = country.get("subregion")
    if sub:
        facts.append((CAT_GEOGRAFIA, "subregion", "Subregió", SUBREGION_CA.get(sub, sub), None))
    return facts


def hdi_tier(value):
    if value >= 0.800:
        return "Molt alt"
    if value >= 0.700:
        return "Alt"
    if value >= 0.550:
        return "Mitjà"
    return "Baix"


# --------------------------------------------------------------------------- #
# Rank & tier
# --------------------------------------------------------------------------- #
QUANTILE_LABELS = ["Capdavanter", "Alt", "Mitjà", "Baix", "Inferior"]
NAMED_LABELS = ["Gegant", "Gran", "Mitjà", "Petit", "Microestat"]


def assign_ranks(facts_by_country, field_meta):
    for key, (direction, tier_style) in field_meta.items():
        rows = []
        for iso2, facts in facts_by_country.items():
            f = facts.get(key)
            if f and f.get("_num") is not None:
                rows.append((iso2, f["_num"]))
        if not rows:
            continue
        rows.sort(key=lambda r: r[1], reverse=(direction == "high"))
        total = len(rows)
        for idx, (iso2, _num) in enumerate(rows):
            rank = idx + 1
            f = facts_by_country[iso2][key]
            f["rank"] = rank
            f["rankTotal"] = total
            if tier_style == "quantile":
                f["tier"] = QUANTILE_LABELS[min(4, (rank - 1) * 5 // total)]
            elif tier_style == "named":
                f["tier"] = NAMED_LABELS[min(4, (rank - 1) * 5 // total)]
            # "hdi"/"none": tier left as set at emission (or absent)


# --------------------------------------------------------------------------- #
# Main
# --------------------------------------------------------------------------- #
def main():
    print("Loading spine (countries.json)...")
    spine = json.loads(COUNTRIES_PATH.read_text(encoding="utf-8"))
    countries = spine["countries"]
    name_ca_by_iso3 = {c["iso3"]: c["name_ca"] for c in countries if c.get("iso3")}
    print(f"  {len(countries)} countries")

    print("Fetching REST Countries v4...")
    rc_by_iso2 = fetch_rest_countries([c["iso2"] for c in countries])
    print(f"  matched {len(rc_by_iso2)} RC objects")

    # Numeric data: key -> {iso3: (value, year)} -------------------------------
    numeric_data = {}
    wb_fields = [f for f in NUMERIC_FIELDS if f[3] == "wb"]
    undp_fields = [f for f in NUMERIC_FIELDS if f[3] == "undp"]

    print(f"Fetching World Bank indicators ({len(wb_fields)}, mrv=5)...")
    for n, f in enumerate(wb_fields, 1):
        key, code = f[0], f[4]
        d = fetch_wb_mrv(code)
        numeric_data[key] = d
        print(f"  [{n:2d}/{len(wb_fields)}] {code:24s} {len(d):3d} countries")

    print("Loading curated overrides...")
    curated = load_curated()

    print("Fetching UNDP HDR composite indices...")
    undp_raw = fetch_undp()
    for f in undp_fields:
        key, src = f[0], f[4]
        d = undp_raw.get(src, {})
        numeric_data[key] = d
        print(f"  {src:6s} -> {key:20s} {len(d):3d} countries")

    # Sort order per (category, key) from declared field order -----------------
    cat_positions = {}
    rc_field_order = [
        (CAT_IDENTITAT, ["official_name", "capital", "demonym", "government_type",
                         "anthem", "olympic_code", "fifa_code", "coat_of_arms"]),
        (CAT_GEOGRAFIA, ["continent", "subregion", "landlocked", "borders", "border_count"]),
        (CAT_CULTURA, ["languages", "currency", "religion", "national_holiday"]),
        (CAT_GOVERNANCA, ["un_member", "eu_member"]),
        (CAT_PRACTIC, ["calling_code", "tld", "driving_side", "timezones", "start_of_week"]),
    ]
    for cat, keys in rc_field_order:
        cat_positions.setdefault(cat, []).extend(keys)
    for f in NUMERIC_FIELDS:
        key, cat = f[0], f[1]
        lst = cat_positions.setdefault(cat, [])
        if key not in lst:
            lst.append(key)
    # Breakdown facts (not numeric fields) placed at the end of their category.
    cat_positions.setdefault(CAT_DEMOGRAFIA, []).append("ethnic_groups")
    # Curated facts placed after the API-sourced fields in each category.
    for cat, keys in (
        (CAT_GEOGRAFIA, ["highest_point"]),
        (CAT_FINANCES, ["gov_debt", "fiscal_balance"]),
        (CAT_GOVERNANCA, ["cpi", "press_freedom", "democracy_index",
                          "schengen", "nato_member", "oecd_member"]),
        (CAT_CULTURA, ["nobel_laureates", "unesco_sites"]),
        (CAT_DRETS, ["death_penalty", "same_sex_marriage", "euthanasia", "abortion"]),
    ):
        lst = cat_positions.setdefault(cat, [])
        for k in keys:
            if k not in lst:
                lst.append(k)
    sort_order_map = {}
    for cat, keys in cat_positions.items():
        for idx, k in enumerate(keys):
            sort_order_map[(cat, k)] = idx * 10

    # Static field metadata for ranking ---------------------------------------
    field_meta = {f[0]: (f[8], f[9]) for f in NUMERIC_FIELDS if f[7]}
    for f in CURATED_NUMERIC:
        if f[5]:  # rankable
            field_meta[f[0]] = (f[6], f[7])

    print("Building facts...")
    iso3_by_iso2 = {c["iso2"]: c.get("iso3") for c in countries}
    facts_by_country = {}

    for country in countries:
        iso2 = country["iso2"]
        iso3 = iso3_by_iso2.get(iso2)
        rc = rc_by_iso2.get(iso2, {})
        facts = {}

        def put(cat, key, label, value, unit=None, year=None, num=None, tier=None):
            facts[key] = {
                "category": cat, "key": key, "labelCa": label,
                "value": value, "unit": unit, "year": year,
                "rank": None, "rankTotal": None, "tier": tier,
                "sortOrder": sort_order_map.get((cat, key), 0),
                "_num": num,
            }

        for cat, key, label, value, unit in rc_text_facts(rc, name_ca_by_iso3):
            put(cat, key, label, value, unit)
        for cat, key, label, value, unit in spine_text_facts(country):
            put(cat, key, label, value, unit)

        for f in NUMERIC_FIELDS:
            key, cat, label, source, code, unit, fmt, rankable, direction, tier_style = f
            vy = numeric_data.get(key, {}).get(iso3) if iso3 else None
            if vy is None and key in RC_FALLBACK:
                vy = RC_FALLBACK[key](rc)
            if vy is None:
                continue
            num, year = vy
            tier = hdi_tier(num) if tier_style == "hdi" else None
            put(cat, key, label, fmt_number(num, fmt), unit, year, num=num, tier=tier)

        cur = curated.get(iso2, {})
        for f in CURATED_NUMERIC:
            key, cat, label, unit, fmt, rankable, direction, tier_style = f
            vy = cur.get(key)
            if not vy:
                continue
            num = float(vy["v"])
            put(cat, key, label, fmt_number(num, fmt), unit, vy.get("y"), num=num)
        for key, cat, label in CURATED_TEXT:
            val = cur.get(key)
            if val:
                put(cat, key, label, val)

        if facts:
            facts_by_country[iso2] = facts

    print("Assigning ranks and tiers...")
    assign_ranks(facts_by_country, field_meta)

    print("Writing JSON...")
    out_countries = []
    total_facts = 0
    for country in countries:
        facts = facts_by_country.get(country["iso2"])
        if not facts:
            continue
        rows = []
        for f in facts.values():
            f.pop("_num", None)
            rows.append(f)
        rows.sort(key=lambda r: (r["category"], r["sortOrder"]))
        out_countries.append({"iso2": country["iso2"], "facts": rows})
        total_facts += len(rows)

    payload = {"version": DATASET_VERSION, "countries": out_countries}
    OUTPUT_PATH.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"Done. {len(out_countries)} countries, {total_facts} facts "
          f"(avg {total_facts / max(1, len(out_countries)):.1f}) -> {OUTPUT_PATH}")


if __name__ == "__main__":
    sys.exit(main())
