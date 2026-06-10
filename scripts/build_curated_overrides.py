"""Consolidate all curated / manual country data into one ISO2-keyed file:
    scripts/data/curated_overrides.json

Sources:
  * Old project files (read once from Travel-app-2.0/.../src/data):
      - nobel-prizes-by-country-2026.json   -> nobel_laureates
      - manual-overrides.json               -> cpi, press_freedom
      - manual-overrides2.json              -> unesco_sites, highest_point
      - democracy-index-eiu.csv             -> democracy_index (latest year)
      - country-dynamic.json                -> gov_debt, fiscal_balance (IMF)
  * Inline curated lists (pasted by the user):
      - schengen / nato / oecd membership
      - death_penalty (Amnesty 2025), same_sex_marriage, euthanasia
      - abortion (Center for Reproductive Rights WALM 2023, 5 categories)

The generator (generate_country_stats.py) loads the output and emits facts.
Run this whenever the curated data changes, then re-run the generator.

Usage:  python scripts/build_curated_overrides.py
"""

from __future__ import annotations

import csv
import json
import re
import unicodedata
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
COUNTRIES_PATH = ROOT / "app" / "src" / "main" / "assets" / "data" / "countries.json"
OUT_DIR = ROOT / "scripts" / "data"
OUT_PATH = OUT_DIR / "curated_overrides.json"

OLD = Path(r"C:\Users\nilpo\Documents\Projectes\Travel-app-2.0\Atlas\worker\src\data")


# --------------------------------------------------------------------------- #
# Name -> ISO2 resolution
# --------------------------------------------------------------------------- #
def _norm(s: str) -> str:
    s = unicodedata.normalize("NFKD", s)
    s = "".join(c for c in s if not unicodedata.combining(c))
    s = s.lower().strip()
    s = re.sub(r"[\.’']", "", s)        # drop dots/apostrophes
    s = s.replace("&", "and").replace("-", " ")
    s = re.sub(r"\s+", " ", s)
    return s


ALIASES = {
    "united states": "US", "united states of america": "US", "usa": "US",
    "united kingdom": "GB", "great britain": "GB", "britain": "GB",
    "czechia": "CZ", "czech republic": "CZ", "czech rep": "CZ",
    "turkiye": "TR", "turkey": "TR",
    "the netherlands": "NL", "netherlands": "NL",
    "korea": "KR", "republic of korea": "KR", "south korea": "KR",
    "north korea": "KP", "dem peoples rep of korea": "KP",
    "democratic peoples republic of korea": "KP",
    "slovak republic": "SK", "slovak rep": "SK", "slovakia": "SK",
    "cape verde": "CV", "cabo verde": "CV",
    "vietnam": "VN", "viet nam": "VN",
    "dr congo": "CD", "democratic republic of congo": "CD",
    "democratic republic of the congo": "CD", "dem rep of congo": "CD",
    "congo": "CG", "republic of the congo": "CG",
    "cote divoire": "CI", "ivory coast": "CI",
    "sao tome and principe": "ST",
    "russia": "RU", "russian federation": "RU", "russian fed": "RU",
    "bosnia and herzegovina": "BA", "bosnia herzegovina": "BA",
    "antigua and barbuda": "AG", "saint kitts and nevis": "KN",
    "saint vincent and the grenadines": "VC", "saint lucia": "LC",
    "trinidad and tobago": "TT", "timor leste": "TL", "east timor": "TL",
    "eswatini": "SZ", "eswatini formerly swaziland": "SZ", "swaziland": "SZ",
    "central african rep": "CF", "central african republic": "CF",
    "guinea bissau": "GW", "brunei darussalam": "BN", "brunei": "BN",
    "laos": "LA", "syria": "SY", "iran": "IR", "moldova": "MD",
    "tanzania": "TZ", "venezuela": "VE", "bolivia": "BO", "palestine": "PS",
    "gambia": "GM", "bahamas": "BS", "micronesia": "FM",
    "equatorial guinea": "GQ", "guinea": "GN",
    "hong kong": "HK", "taiwan": "TW", "puerto rico": "PR",
    "aruba": "AW", "curacao": "CW", "french guiana": "GF",
    "new caledonia": "NC", "greenland": "GL", "western sahara": "EH",
    "kosovo": "XK", "san marino": "SM", "andorra": "AD", "monaco": "MC",
    "liechtenstein": "LI", "maldives": "MV",
}


def build_resolver(countries):
    by_name = {}
    for c in countries:
        iso2 = c["iso2"]
        for key in (c.get("name_en"), c.get("name_ca")):
            if key:
                by_name.setdefault(_norm(key), iso2)
    unresolved = set()

    def resolve(name):
        n = _norm(name)
        if n in ALIASES:
            return ALIASES[n]
        if n in by_name:
            return by_name[n]
        unresolved.add(name.strip())
        return None

    resolve.unresolved = unresolved
    return resolve


# --------------------------------------------------------------------------- #
# Inline curated lists
# --------------------------------------------------------------------------- #
SCHENGEN = [
    "Austria", "Belgium", "Bulgaria", "Croatia", "Cyprus", "Czech Republic",
    "Denmark", "Estonia", "Finland", "France", "Germany", "Greece", "Hungary",
    "Iceland", "Italy", "Latvia", "Liechtenstein", "Lithuania", "Luxembourg",
    "Malta", "Netherlands", "Norway", "Poland", "Portugal", "Romania",
    "Slovakia", "Slovenia", "Spain", "Sweden", "Switzerland",
]

NATO = [
    "Albania", "Belgium", "Bulgaria", "Canada", "Croatia", "Czechia", "Denmark",
    "Estonia", "Finland", "France", "Germany", "Greece", "Hungary", "Iceland",
    "Italy", "Latvia", "Lithuania", "Luxembourg", "Montenegro", "North Macedonia",
    "Norway", "Poland", "Portugal", "Romania", "Slovakia", "Slovenia", "Spain",
    "Sweden", "Netherlands", "Türkiye", "United Kingdom", "United States",
]

OECD = [
    "Australia", "Austria", "Belgium", "Canada", "Chile", "Colombia",
    "Costa Rica", "Czechia", "Denmark", "Estonia", "Finland", "France",
    "Germany", "Greece", "Hungary", "Iceland", "Ireland", "Israel", "Italy",
    "Japan", "Korea", "Latvia", "Lithuania", "Luxembourg", "Mexico",
    "Netherlands", "New Zealand", "Norway", "Poland", "Portugal",
    "Slovak Republic", "Slovenia", "Spain", "Sweden", "Switzerland", "Türkiye",
    "United Kingdom", "United States",
]

# Amnesty International — countries that recorded death sentences in 2025 -> active.
DEATH_PENALTY_ACTIVE = [
    "China", "Egypt", "Democratic Republic of the Congo", "Nigeria", "Bangladesh",
    "Viet Nam", "India", "Thailand", "Yemen", "Sudan", "Iraq", "Indonesia",
    "Pakistan", "Sri Lanka", "Mauritania", "USA", "Tunisia", "Kuwait", "Malaysia",
    "Libya", "Somalia", "Algeria", "Mali", "Tanzania", "Jordan", "Laos", "Myanmar",
    "Niger", "Ethiopia", "Qatar", "Gambia", "Maldives", "United Arab Emirates",
    "Comoros", "Kenya", "Singapore", "Taiwan", "Syria", "Bahrain", "Japan",
    "Lebanon", "Morocco", "Trinidad and Tobago", "Afghanistan", "Iran",
    "North Korea", "Saudi Arabia", "South Sudan",
]

SAME_SEX_LEGAL = [
    # Europe
    "Andorra", "Austria", "Belgium", "Denmark", "Estonia", "Finland", "France",
    "Germany", "Greece", "Iceland", "Ireland", "Liechtenstein", "Luxembourg",
    "Malta", "Netherlands", "Norway", "Portugal", "Slovenia", "Spain", "Sweden",
    "Switzerland", "United Kingdom",
    # Americas
    "Argentina", "Brazil", "Canada", "Chile", "Colombia", "Costa Rica", "Cuba",
    "Ecuador", "Mexico", "United States", "Uruguay",
    # Asia-Pacific + Africa
    "Australia", "New Zealand", "Taiwan", "Thailand", "South Africa",
]
SAME_SEX_FOREIGN = ["Israel"]  # recognizes foreign marriages only

EUTHANASIA_ACTIVE = [
    "Australia", "Belgium", "Canada", "Colombia", "Ecuador", "Luxembourg",
    "Netherlands", "New Zealand", "Portugal", "Spain", "Uruguay",
]
EUTHANASIA_ASSISTED = ["Austria", "Germany", "Italy", "Switzerland", "United States"]
EUTHANASIA_PASSIVE = [
    "Argentina", "Chile", "France", "India", "Ireland", "Japan", "Mexico",
    "South Korea", "United Kingdom",
]

# Abortion — Center for Reproductive Rights, WALM 2023 (least -> most restrictive)
ABORTION = {
    "Lliure demanda": [  # Category I — On Request (77)
        "Albania", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
        "Belarus", "Belgium", "Benin", "Bosnia and Herzegovina", "Bulgaria",
        "Cambodia", "Canada", "Cape Verde", "China", "Colombia", "Croatia",
        "Cuba", "Cyprus", "Czech Republic", "North Korea", "Denmark",
        "Equatorial Guinea", "Estonia", "Finland", "France", "French Guiana",
        "Georgia", "Germany", "Greece", "Guinea Bissau", "Guyana", "Hungary",
        "Iceland", "Ireland", "Italy", "Kazakhstan", "Kosovo", "Kyrgyzstan",
        "Latvia", "Lithuania", "Luxembourg", "Maldives", "Moldova", "Mongolia",
        "Montenegro", "Mozambique", "Nepal", "Netherlands", "New Caledonia",
        "New Zealand", "North Macedonia", "Norway", "Portugal", "South Korea",
        "Romania", "Russia", "San Marino", "Sao Tome and Principe", "Serbia",
        "Singapore", "Slovakia", "Slovenia", "South Africa", "Spain", "Sweden",
        "Switzerland", "Tajikistan", "Thailand", "Tunisia", "Türkiye",
        "Turkmenistan", "Ukraine", "Uruguay", "Uzbekistan", "Vietnam",
    ],
    "Per causes socioeconòmiques": [  # Category II (12)
        "Barbados", "Belize", "Ethiopia", "Fiji", "Great Britain", "Hong Kong",
        "India", "Japan", "Rwanda", "Saint Vincent and the Grenadines", "Taiwan",
        "Zambia",
    ],
    "Per preservar la salut": [  # Category III (47)
        "Algeria", "Angola", "Bahamas", "Bolivia", "Botswana", "Burkina Faso",
        "Burundi", "Cameroon", "Central African Republic", "Chad", "Comoros",
        "Costa Rica", "Democratic Republic of the Congo", "Djibouti", "Ecuador",
        "Eritrea", "Eswatini", "Ghana", "Grenada", "Guinea", "Israel", "Jordan",
        "Kenya", "Kuwait", "Lesotho", "Liberia", "Liechtenstein", "Malaysia",
        "Mauritius", "Monaco", "Morocco", "Namibia", "Nauru", "Niger", "Pakistan",
        "Peru", "Poland", "Puerto Rico", "Qatar", "Saint Lucia", "Samoa",
        "Saudi Arabia", "Seychelles", "Togo", "Trinidad and Tobago", "Vanuatu",
        "Zimbabwe",
    ],
    "Per salvar la vida": [  # Category IV (43)
        "Afghanistan", "Antigua and Barbuda", "Bahrain", "Bangladesh", "Bhutan",
        "Brazil", "Brunei", "Chile", "Cote d'Ivoire", "Dominica", "Gabon",
        "Gambia", "Guatemala", "Indonesia", "Iran", "Kiribati", "Lebanon",
        "Libya", "Malawi", "Mali", "Marshall Islands", "Micronesia", "Myanmar",
        "Nigeria", "Oman", "Palestine", "Panama", "Papua New Guinea", "Paraguay",
        "Saint Kitts and Nevis", "Solomon Islands", "Somalia", "South Sudan",
        "Sri Lanka", "Sudan", "Syria", "Tanzania", "Timor-Leste", "Tuvalu",
        "Uganda", "United Arab Emirates", "Venezuela", "Yemen",
    ],
    "Prohibit": [  # Category V (22)
        "Andorra", "Aruba", "Congo", "Curacao", "Dominican Republic", "Egypt",
        "El Salvador", "Haiti", "Honduras", "Iraq", "Jamaica", "Laos",
        "Madagascar", "Malta", "Mauritania", "Nicaragua", "Palau", "Philippines",
        "Senegal", "Sierra Leone", "Suriname", "Tonga",
    ],
    "Varia segons la regió": ["Mexico", "United States"],
}


# --------------------------------------------------------------------------- #
# Build
# --------------------------------------------------------------------------- #
def main():
    countries = json.loads(COUNTRIES_PATH.read_text(encoding="utf-8"))["countries"]
    spine_iso2 = {c["iso2"] for c in countries}
    resolve = build_resolver(countries)

    data = {}  # iso2 -> {field: value}

    def set_field(iso2, field, value):
        if iso2 and iso2 in spine_iso2:
            data.setdefault(iso2, {})[field] = value

    # --- Nobel (iso2 flagCode) ---
    nobel = json.loads((OLD / "nobel-prizes-by-country-2026.json").read_text(encoding="utf-8"))
    for row in nobel:
        code = row.get("flagCode")
        n = row.get("NobelPrizesTotalPrizeCountSince1901_2025")
        if code and n:
            set_field(code, "nobel_laureates", {"v": n, "y": 2025})

    # --- CPI + press freedom (iso2) ---
    mo = json.loads((OLD / "manual-overrides.json").read_text(encoding="utf-8"))["countries"]
    for iso2, rec in mo.items():
        if rec.get("cpi") is not None:
            set_field(iso2, "cpi", {"v": rec["cpi"], "y": rec.get("cpiYear", 2024)})
        if rec.get("pressScore") is not None:
            set_field(iso2, "press_freedom", {"v": rec["pressScore"], "y": rec.get("pressYear", 2025)})

    # --- UNESCO + highest point (iso2, dedup: first valid wins) ---
    mo2 = json.loads((OLD / "manual-overrides2.json").read_text(encoding="utf-8"))
    for entry in mo2:
        iso2 = entry.get("iso2")
        if not iso2 or iso2 not in spine_iso2:
            continue
        rec = data.get(iso2, {})
        if "unesco_sites" not in rec and entry.get("unescoCount"):
            try:
                cnt = int(entry["unescoCount"])
                if cnt > 0:
                    set_field(iso2, "unesco_sites", {"v": cnt, "y": 2024})
            except ValueError:
                pass
        label = entry.get("highestPointLabel")
        if "highest_point" not in data.get(iso2, {}) and label and entry.get("elevation"):
            if not re.match(r"^Q\d+$", label.strip()):  # skip raw Wikidata Q-codes
                try:
                    m = int(round(float(entry["elevation"])))
                    set_field(iso2, "highest_point", f"{label.strip()} · {m:,} m".replace(",", "."))
                except ValueError:
                    pass

    # --- Democracy index (iso3, latest year) ---
    iso3_to_iso2 = {c["iso3"]: c["iso2"] for c in countries if c.get("iso3")}
    dem_latest = {}  # iso3 -> (year, value)
    with (OLD / "democracy-index-eiu.csv").open(encoding="utf-8") as f:
        for row in csv.DictReader(f):
            iso3 = row.get("Code")
            try:
                year = int(row["Year"]); val = float(row["Democracy Index"])
            except (TypeError, ValueError):
                continue
            if iso3 and (iso3 not in dem_latest or year > dem_latest[iso3][0]):
                dem_latest[iso3] = (year, val)
    for iso3, (year, val) in dem_latest.items():
        set_field(iso3_to_iso2.get(iso3), "democracy_index", {"v": val, "y": year})

    # --- IMF debt + fiscal balance (iso2) ---
    dyn = json.loads((OLD / "country-dynamic.json").read_text(encoding="utf-8"))["countries"]
    for iso2, rec in dyn.items():
        econ = rec.get("economy", {})
        for src, dst in (("publicDebtPctGdp", "gov_debt"), ("fiscalBalancePctGdp", "fiscal_balance")):
            v = econ.get(src, {})
            if v.get("value") is not None:
                set_field(iso2, dst, {"v": v["value"], "y": v.get("year")})

    # --- Memberships: Sí for members, No for every other spine country ---
    for field, names in (("schengen", SCHENGEN), ("nato_member", NATO), ("oecd_member", OECD)):
        members = {resolve(n) for n in names}
        for iso2 in spine_iso2:
            set_field(iso2, field, "Sí" if iso2 in members else "No")

    # --- Social rights (partial coverage; only set where we have data) ---
    for n in DEATH_PENALTY_ACTIVE:
        set_field(resolve(n), "death_penalty", "Vigent")
    for n in SAME_SEX_LEGAL:
        set_field(resolve(n), "same_sex_marriage", "Legal")
    for n in SAME_SEX_FOREIGN:
        set_field(resolve(n), "same_sex_marriage", "Reconegut (estranger)")
    for n in EUTHANASIA_ACTIVE:
        set_field(resolve(n), "euthanasia", "Legal")
    for n in EUTHANASIA_ASSISTED:
        set_field(resolve(n), "euthanasia", "Suïcidi assistit")
    for n in EUTHANASIA_PASSIVE:
        set_field(resolve(n), "euthanasia", "Només passiva")
    for status, names in ABORTION.items():
        for n in names:
            set_field(resolve(n), "abortion", status)

    OUT_DIR.mkdir(parents=True, exist_ok=True)
    OUT_PATH.write_text(json.dumps(data, ensure_ascii=False, indent=2, sort_keys=True),
                        encoding="utf-8")

    # Report
    field_counts = {}
    for rec in data.values():
        for k in rec:
            field_counts[k] = field_counts.get(k, 0) + 1
    print(f"Wrote {OUT_PATH} — {len(data)} countries")
    for k in sorted(field_counts):
        print(f"  {k:18s} {field_counts[k]:3d}")
    if resolve.unresolved:
        print("\nUNRESOLVED names (skipped):")
        for nm in sorted(resolve.unresolved):
            print(f"  - {nm}")


if __name__ == "__main__":
    main()
