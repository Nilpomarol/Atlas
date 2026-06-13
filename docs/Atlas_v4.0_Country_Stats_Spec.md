# Atlas v4.0 — Country Stats Dataset Spec

## 0. Status & purpose

* **Status:** implemented in v4.0 M1 and M2. DB v22 introduced the fact table; DB v23 added the optional country-photo cache. The current work is UI polish.
* **Goal:** a deep, flexible per-country facts dataset (~165 fields across 16 categories) powering a new Country Info screen. Catalan throughout. Global **ranks** and Catalan **tiers** on every rankable numeric metric.
* **Current dataset:** version `2026.1`, 244 countries, about 24,000 facts, and 16 raw categories. It combines API-driven fields with the curated overrides documented below.
* **Future additions:** remaining curated fields can be added as rows with a dataset version bump and no Room schema change.
* The implemented info screen hides empty sections/fields automatically.

---

## 1. Schema — DB v22

Single flexible fact table. Composite PK = no UUIDs. Adding fields never needs a migration — only the `CREATE TABLE` (v21→v22) is new.

```kotlin
@Entity(
    tableName = "country_stat_facts",
    primaryKeys = ["country_iso2", "category", "key"]
)
data class CountryStatFactEntity(
    @ColumnInfo(name = "country_iso2") val countryIso2: String,
    val category: String,        // see §2 category keys
    val key: String,             // stable machine key, e.g. "life_expectancy"
    @ColumnInfo(name = "label_ca") val labelCa: String,
    val value: String,           // pre-formatted, Catalan locale ("83,2")
    val unit: String? = null,    // "anys", "km²", "hab./km²"… rendered after value
    val year: Int? = null,       // source year → muted note
    val rank: Int? = null,       // global rank among countries with data
    @ColumnInfo(name = "rank_total") val rankTotal: Int? = null,
    val tier: String? = null,    // Catalan tier label, see §3
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0
)
```

### Migration

```sql
CREATE TABLE country_stat_facts (
    country_iso2 TEXT NOT NULL,
    category     TEXT NOT NULL,
    key          TEXT NOT NULL,
    label_ca     TEXT NOT NULL,
    value        TEXT NOT NULL,
    unit         TEXT,
    year         INTEGER,
    rank         INTEGER,
    rank_total   INTEGER,
    tier         TEXT,
    sort_order   INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (country_iso2, category, key)
);
CREATE INDEX index_country_stat_facts_country_iso2 ON country_stat_facts(country_iso2);
```

No FK to `countries` (dataset import order independence; mirrors how `airlines` was handled). `country_iso2` is the join key everywhere.

### DAO / repository / domain

```kotlin
// CountryStatFactDao
@Query("SELECT * FROM country_stat_facts WHERE country_iso2 = :iso2 ORDER BY category, sort_order")
fun observeByCountry(iso2: String): Flow<List<CountryStatFactEntity>>

@Query("DELETE FROM country_stat_facts")
suspend fun clear()

@Upsert
suspend fun upsertAll(rows: List<CountryStatFactEntity>)
```

* `CountryStatFact` domain model mirrors the entity.
* `CountryStatRepository.observeByCountry(iso2): Flow<List<CountryStatFact>>` — returns all rows; the ViewModel groups by `category` for sectioned layout. A country has ≤ ~25 rows, so one query + in-memory group is fine.
* Importer is **source-agnostic**: it reads `assets/data/country_stats.json` and upserts. v4.1 only adds rows to that JSON (or a second merged JSON).
* Track installed version in `dataset_metadata` (name `country_stats`, version `2026.1`), same pattern as the other datasets.

---

## 2. Categories

Order = display order. Section shown only if it has ≥ 1 non-empty fact.

| # | key | Title (CA) |
|---|---|---|
| 1 | `identitat` | Identitat |
| 2 | `geografia` | Geografia |
| 3 | `demografia` | Demografia |
| 4 | `salut` | Salut |
| 5 | `economia` | Economia |
| 6 | `finances` | Finances |
| 7 | `desenvolupament` | Desenvolupament humà |
| 8 | `educacio` | Educació |
| 9 | `mediambient` | Medi ambient i territori |
| 10 | `infraestructura` | Infraestructura i tecnologia |
| 11 | `governanca` | Governança i política |
| 12 | `desigualtat` | Desigualtat |
| 13 | `cultura` | Cultura |
| 14 | `drets` | Drets i societat |
| 15 | `turisme` | Turisme |
| 16 | `practic` | Pràctic per viatjar |

---

## 3. Rank & tier system

Computed **in the generation script**, across all 244 countries, per rankable metric.

* `rank` / `rankTotal` — global position among countries that have a value. Rendered `"30è del món"` (`rankTotal` available for `"30è de 195"` if desired).
* **`direction`** — controls rank ordering:
  * `high` → rank 1 = highest value (life expectancy, GDP, literacy…).
  * `low` → rank 1 = lowest value, i.e. "best" for metrics where less is more notable in a positive sense (infant mortality, maternal mortality, Gini).
* **`tierStyle`** — which tier label set to assign (or none):
  * `hdi` → UNDP's official 4 bands: **Molt alt · Alt · Mitjà · Baix**.
  * `named` → magnitude buckets (size): **Gegant · Gran · Mitjà · Petit · Microestat** (population, area).
  * `quantile` → Catalan ranking bands by quintile of the (direction-adjusted) distribution: **Capdavanter · Alt · Mitjà · Baix · Inferior**.
  * `none` → rank only, **no tier chip** (neutral-magnitude metrics where an evaluative band would be misleading: CO₂, military spend, exchange rate, inflation, debt…).

A rendered fact: **`Població — 49,6 M hab. · 30è del món · 2023`** + tier chip `Gran`.

---

## 3b. Breakdown fields (religion, ethnic_groups)

A few facts are **distributions**, not single values. They reuse the same flat
row — the `value` column holds a **JSON array string** the M2 screen parses:

```
key "religion"      label "Religió"       value: [{"name":"Catolicisme romà","pct":58.6}, …]
key "ethnic_groups" label "Grups ètnics"  value: [{"name":"Xinesos","pct":74.3}, …]
```

* `pct` is a raw JSON number (no Catalan formatting — the UI renders it, e.g. `58,6%`).
* `unit`/`year`/`rank`/`tier` are null; ordering by `sortOrder` within the category.
* Names are translated to Catalan (`RELIGION_CA` / `ETHNICITY_CA` maps; unmatched → Title case).
* **¹ Coverage:** `religion` is present for ~every country; `ethnicity` (RC v4) is
  **partial** — present where RC has it (e.g. Singapore, Nigeria), absent elsewhere.
  No schema/DTO change — `value` is already a string.

## 3c. Curated overrides (moved v4.1 → v4.0)

`scripts/build_curated_overrides.py` consolidates all manual/curated data into
`scripts/data/curated_overrides.json` (keyed by ISO2). The generator loads it via
`CURATED_NUMERIC` / `CURATED_TEXT`. Re-run the builder when curated data changes,
then re-run the generator. Fields added:

| Field | Category | Source | Coverage |
|---|---|---|---|
| `cpi` | Governança | Transparency Intl 2024 | 182 |
| `press_freedom` | Governança | RSF 2025 (score) | 177 |
| `democracy_index` | Governança | EIU 2024 | 167 |
| `schengen` · `nato_member` · `oecd_member` | Governança | curated lists (Sí/No) | 244 |
| `nobel_laureates` | Cultura | Wikipedia/curated | 79 |
| `unesco_sites` | Cultura | curated | 172 |
| `highest_point` | Geografia | curated (`Teide · 3.715 m`) | 224 |
| `gov_debt` · `fiscal_balance` | Finances | IMF (via old project, % PIB) | 192 / 195 |
| **`death_penalty`** | **Drets i societat** | Amnesty 2025 | 48¹ |
| **`same_sex_marriage`** | Drets i societat | ILGA/curated | 39 |
| **`euthanasia`** | Drets i societat | curated | 25 |
| **`abortion`** | Drets i societat | Center for Reproductive Rights (WALM 2023) | 202 |

New category **`drets` — "Drets i societat"** (16th category).

**Caveats:** `death_penalty` is one-sided — only "Vigent" (active) countries from
the Amnesty sentences list; abolitionist countries are left blank, not asserted.
Social-rights fields are partial factual snapshots and need periodic refresh.
The Schengen list uses the user's provided list verbatim (includes Cyprus).
The live **IMF DataMapper API IP-blocks** this environment, so debt/fiscal come
from the old project's already-fetched IMF data rather than a live call.

**Still pending (the remaining manual fields):** `cannabis`, `voting_age`,
`conscription`, `independence_year`, `motto`, `plug_type`.

## 4. Field catalog

Columns: **Key** · **Label (CA)** · **Source/code** · **Unit** · **Rank** (dir) · **Tier** · **Phase**.
Source legend: **RC** = REST Countries v4 (build-time only), **WB** = World Bank API (`mrnev=1`), **UNDP** = HDR CSV bundle, **WGI** = WB Governance, **CUR** = curated bundle.
`sortOrder` = row order × 10 within each category.

### 1 · Identitat
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `official_name` | Nom oficial | RC `name` | – | – | – | 4.0 |
| `capital` | Capital | RC `capital` | – | – | – | 4.0 |
| `demonym` | Gentilici | RC `demonyms` → CA | – | – | – | 4.0 |
| `government_type` | Forma de govern | RC `government.type` → CA | – | – | – | 4.0 |
| `anthem` | Himne nacional | RC `anthem` | – | – | – | 4.0 |
| `iso_codes` | Codis ISO | RC `cca2`/`cca3` | – | – | – | 4.0 |
| `olympic_code` | Codi olímpic | RC `cioc` | – | – | – | 4.0 |
| `fifa_code` | Codi FIFA | RC `fifa` | – | – | – | 4.0 |
| `flag` | Bandera | RC `flags.svg` | – | – | – | 4.0 |
| `coat_of_arms` | Escut | RC `coatOfArms.svg` | – | – | – | 4.0 |
| `independence_year` | Any d'independència | CUR | – | – | – | 4.1 |
| `motto` | Lema nacional | CUR | – | – | – | 4.1 |

### 2 · Geografia
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `area` | Superfície | RC `area` / WB `AG.LND.TOTL.K2` | km² | high | named | 4.0 |
| `continent` | Continent | RC `continents` → CA | – | – | – | 4.0 |
| `subregion` | Subregió | RC `subregion` → CA | – | – | – | 4.0 |
| `landlocked` | Sense litoral | RC `landlocked` | – | – | – | 4.0 |
| `borders` | Fronteres | RC `borders` → CA names | – | – | – | 4.0 |
| `border_count` | Països fronterers | RC `borders.size` | – | high | none | 4.0 |
| `agri_land` | Terra agrícola | WB `AG.LND.AGRI.ZS` | % | – | none | 4.0 |
| `arable_land` | Terra cultivable | WB `AG.LND.ARBL.ZS` | % | – | none | 4.0 |
| `forest_land` | Bosc | WB `AG.LND.FRST.ZS` | % | high | quantile | 4.0 |

### 3 · Demografia
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `population` | Població | WB `SP.POP.TOTL` | hab. | high | named | 4.0 |
| `pop_growth` | Creixement demogràfic | WB `SP.POP.GROW` | % | – | none | 4.0 |
| `density` | Densitat | WB `EN.POP.DNST` | hab./km² | high | none | 4.0 |
| `urban_pct` | Població urbana | WB `SP.URB.TOTL.IN.ZS` | % | – | none | 4.0 |
| `age_0_14` | Població 0–14 | WB `SP.POP.0014.TO.ZS` | % | – | none | 4.0 |
| `age_15_64` | Població 15–64 | WB `SP.POP.1564.TO.ZS` | % | – | none | 4.0 |
| `age_65_plus` | Població 65+ | WB `SP.POP.65UP.TO.ZS` | % | – | none | 4.0 |
| `fertility` | Taxa de fecunditat | WB `SP.DYN.TFRT.IN` | fills/dona | – | none | 4.0 |
| `birth_rate` | Natalitat | WB `SP.DYN.CBRT.IN` | ‰ | – | none | 4.0 |
| `death_rate` | Mortalitat | WB `SP.DYN.CDRT.IN` | ‰ | – | none | 4.0 |
| `net_migration` | Migració neta | WB `SM.POP.NETM` | pers. | – | none | 4.0 |
| `female_pct` | Percentatge de dones | WB `SP.POP.TOTL.FE.ZS` | % | – | none | 4.0 |
| `ethnic_groups` | Grups ètnics | RC v4 `ethnicity` → CA · **breakdown** | – | – | – | 4.0¹ |

### 4 · Salut
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `life_expectancy` | Esperança de vida | WB `SP.DYN.LE00.IN` | anys | high | quantile | 4.0 |
| `life_exp_male` | EV homes | WB `SP.DYN.LE00.MA.IN` | anys | – | none | 4.0 |
| `life_exp_female` | EV dones | WB `SP.DYN.LE00.FE.IN` | anys | – | none | 4.0 |
| `infant_mortality` | Mortalitat infantil | WB `SP.DYN.IMRT.IN` | ‰ | low | quantile | 4.0 |
| `under5_mortality` | Mortalitat <5 anys | WB `SH.DYN.MORT` | ‰ | low | none | 4.0 |
| `maternal_mortality` | Mortalitat materna | WB `SH.STA.MMRT` | /100k | low | none | 4.0 |
| `physicians` | Metges | WB `SH.MED.PHYS.ZS` | /1000 | high | none | 4.0 |
| `hospital_beds` | Llits hospitalaris | WB `SH.MED.BEDS.ZS` | /1000 | high | none | 4.0 |
| `health_exp_gdp` | Despesa en salut | WB `SH.XPD.CHEX.GD.ZS` | % PIB | – | none | 4.0 |
| `health_exp_pc` | Despesa salut p.c. | WB `SH.XPD.CHEX.PC.CD` | USD | high | none | 4.0 |
| `water_access` | Aigua potable | WB `SH.H2O.BASW.ZS` | % | high | none | 4.0 |
| `sanitation` | Sanejament | WB `SH.STA.BASS.ZS` | % | high | none | 4.0 |
| `obesity` | Obesitat (adults) | WB `SH.STA.OWAD.ZS` *(verify; else WHO)* | % | – | none | 4.0 |
| `smoking` | Tabaquisme | WB `SH.PRV.SMOK` | % | – | none | 4.0 |
| `suicide_rate` | Taxa de suïcidis | WB `SH.STA.SUIC.P5` | /100k | low | none | 4.0 |

### 5 · Economia
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `gdp` | PIB | WB `NY.GDP.MKTP.CD` | USD | high | named | 4.0 |
| `gdp_ppp` | PIB (PPA) | WB `NY.GDP.MKTP.PP.CD` | USD | high | named | 4.0 |
| `gdp_per_capita` | PIB per càpita | WB `NY.GDP.PCAP.CD` | USD | high | quantile | 4.0 |
| `gdp_pc_ppp` | PIB p.c. (PPA) | WB `NY.GDP.PCAP.PP.CD` | USD | high | quantile | 4.0 |
| `gdp_growth` | Creixement del PIB | WB `NY.GDP.MKTP.KD.ZG` | % | – | none | 4.0 |
| `inflation` | Inflació | WB `FP.CPI.TOTL.ZG` | % | low | none | 4.0 |
| `unemployment` | Atur | WB `SL.UEM.TOTL.ZS` | % | low | none | 4.0 |
| `gni_per_capita` | RNB per càpita | WB `NY.GNP.PCAP.CD` | USD | high | quantile | 4.0 |
| `agri_gdp` | Agricultura | WB `NV.AGR.TOTL.ZS` | % PIB | – | none | 4.0 |
| `industry_gdp` | Indústria | WB `NV.IND.TOTL.ZS` | % PIB | – | none | 4.0 |
| `services_gdp` | Serveis | WB `NV.SRV.TOTL.ZS` | % PIB | – | none | 4.0 |
| `labor_force` | Força laboral | WB `SL.TLF.CACT.ZS` | % | – | none | 4.0 |
| `exchange_rate` | Tipus de canvi | WB `PA.NUS.FCRF` | LCU/USD | – | none | 4.0 |
| `rd_expenditure` | Despesa R+D | WB `GB.XPD.RSDV.GD.ZS` | % PIB | high | none | 4.0 |

### 6 · Finances
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `gov_debt` | Deute públic | WB `GC.DOD.TOTL.GD.ZS` | % PIB | low | none | 4.0 |
| `fiscal_balance` | Balanç fiscal | WB `GC.NLD.TOTL.GD.ZS` | % PIB | – | none | 4.0 |
| `exports_gdp` | Exportacions | WB `NE.EXP.GNFS.ZS` | % PIB | – | none | 4.0 |
| `imports_gdp` | Importacions | WB `NE.IMP.GNFS.ZS` | % PIB | – | none | 4.0 |
| `current_account` | Compte corrent | WB `BN.CAB.XOKA.GD.ZS` | % PIB | – | none | 4.0 |
| `fdi_inflows` | Inversió estrangera | WB `BX.KLT.DINV.CD.WD` | USD | high | none | 4.0 |
| `tax_revenue` | Pressió fiscal | WB `GC.TAX.TOTL.GD.ZS` | % PIB | – | none | 4.0 |
| `reserves` | Reserves totals | WB `FI.RES.TOTL.CD` | USD | high | none | 4.0 |

### 7 · Desenvolupament humà
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `hdi` | IDH | UNDP (RC `hdi` fallback) | – | high | hdi | 4.0 |
| `hdi_tier` | Categoria IDH | UNDP | – | – | – | 4.0 |
| `ihdi` | IDH ajustat per desigualtat | UNDP | – | high | quantile | 4.0 |
| `gii` | Índex de desigualtat de gènere | UNDP | – | low | quantile | 4.0 |
| `gdi` | Índex de desenvolupament de gènere | UNDP | – | – | none | 4.0 |
| `mpi` | Pobresa multidimensional | UNDP | – | low | none | 4.0 |

### 8 · Educació
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `literacy` | Alfabetització | WB `SE.ADT.LITR.ZS` | % | high | quantile | 4.0 |
| `edu_expenditure` | Despesa en educació | WB `SE.XPD.TOTL.GD.ZS` | % PIB | – | none | 4.0 |
| `primary_enroll` | Matriculació primària | WB `SE.PRM.ENRR` | % | – | none | 4.0 |
| `secondary_enroll` | Matriculació secundària | WB `SE.SEC.ENRR` | % | – | none | 4.0 |
| `tertiary_enroll` | Matriculació terciària | WB `SE.TER.ENRR` | % | high | none | 4.0 |
| `compulsory_years` | Anys d'escolaritat obligatòria | WB `SE.COM.DURS` | anys | – | none | 4.0 |
| `expected_schooling` | Anys esperats d'escolaritat | UNDP | anys | – | none | 4.0 |
| `mean_schooling` | Anys mitjans d'escolaritat | UNDP | anys | high | none | 4.0 |

### 9 · Medi ambient i territori
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `co2_per_capita` | CO₂ per càpita | WB `EN.ATM.CO2E.PC` *(verify code)* | t | low | none | 4.0 |
| `co2_total` | CO₂ total | WB `EN.ATM.CO2E.KT` *(verify)* | kt | low | none | 4.0 |
| `energy_per_capita` | Energia per càpita | WB `EG.USE.PCAP.KG.OE` | kg ep | – | none | 4.0 |
| `renewable_energy` | Energia renovable | WB `EG.FEC.RNEW.ZS` | % | high | quantile | 4.0 |
| `electricity_access` | Accés a electricitat | WB `EG.ELC.ACCS.ZS` | % | high | none | 4.0 |
| `pm25` | Contaminació PM2.5 | WB `EN.ATM.PM25.MC.M3` | µg/m³ | low | none | 4.0 |
| `protected_areas` | Àrees protegides | WB `ER.PTD.TOTL.ZS` | % | high | none | 4.0 |
| `forest_area_km2` | Superfície forestal | WB `AG.LND.FRST.K2` | km² | high | none | 4.0 |

### 10 · Infraestructura i tecnologia
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `internet_users` | Usuaris d'internet | WB `IT.NET.USER.ZS` | % | high | quantile | 4.0 |
| `mobile_subs` | Subscripcions mòbils | WB `IT.CEL.SETS.P2` | /100 | – | none | 4.0 |
| `broadband` | Banda ampla fixa | WB `IT.NET.BBND.P2` | /100 | high | none | 4.0 |
| `air_passengers` | Passatgers aeris | WB `IS.AIR.PSGR` | pass./any | high | none | 4.0 |

### 11 · Governança i política
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `un_member` | Membre de l'ONU | RC `unMember` | – | – | – | 4.0 |
| `eu_member` | Membre de la UE | RC `regionalBlocs` | – | – | – | 4.0 |
| `political_stability` | Estabilitat política | WGI `PV.PER.RNK` | pct | high | quantile | 4.0 |
| `gov_effectiveness` | Eficàcia del govern | WGI `GE.PER.RNK` | pct | high | quantile | 4.0 |
| `rule_of_law` | Estat de dret | WGI `RL.PER.RNK` | pct | high | quantile | 4.0 |
| `corruption_control` | Control de la corrupció | WGI `CC.PER.RNK` | pct | high | quantile | 4.0 |
| `voice_accountability` | Veu i rendició de comptes | WGI `VA.PER.RNK` | pct | high | quantile | 4.0 |
| `regulatory_quality` | Qualitat reguladora | WGI `RQ.PER.RNK` | pct | high | quantile | 4.0 |
| `military_exp_gdp` | Despesa militar | WB `MS.MIL.XPND.GD.ZS` | % PIB | – | none | 4.0 |
| `military_exp_usd` | Despesa militar | WB `MS.MIL.XPND.CD` | USD | high | none | 4.0 |
| `schengen` | Espai Schengen | CUR | – | – | – | 4.1 |
| `nato_member` | Membre de l'OTAN | CUR | – | – | – | 4.1 |
| `oecd_member` | Membre de l'OCDE | CUR | – | – | – | 4.1 |

### 12 · Desigualtat
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `gini` | Índex de Gini | WB `SI.POV.GINI` (RC `gini` fallback) | – | low | quantile | 4.0 |
| `income_top10` | Renda del 10% superior | WB `SI.DST.10TH.10` | % | – | none | 4.0 |
| `income_bottom10` | Renda del 10% inferior | WB `SI.DST.FRST.10` | % | – | none | 4.0 |
| `poverty_215` | Pobresa (2,15 USD/dia) | WB `SI.POV.DDAY` | % | low | none | 4.0 |
| `poverty_national` | Pobresa (línia nacional) | WB `SI.POV.NAHC` | % | low | none | 4.0 |

### 13 · Cultura
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `languages` | Idiomes oficials | RC `languages` → CA | – | – | – | 4.0 |
| `currency` | Moneda | RC `currencies` | – | – | – | 4.0 |
| `religion` | Religió | RC v4 `religion` → CA · **breakdown** | – | – | – | 4.0 |
| `national_holiday` | Festa nacional | RC `nationalHoliday` | – | – | – | 4.0 |
| `nobel_laureates` | Premis Nobel | CUR | – | high | none | 4.1 |
| `unesco_sites` | Patrimoni UNESCO | CUR | – | high | none | 4.1 |

### 14 · Drets i societat (all CUR, all 4.1)
| Key | Label | Statuses (CA) |
|---|---|---|
| `death_penalty` | Pena de mort | Abolida · Abolida (delictes ordinaris) · Moratòria de fet · Vigent |
| `same_sex_marriage` | Matrimoni igualitari | Legal · Unions civils · No reconegut · Prohibit · Criminalitzat |
| `abortion` | Avortament | Lliure demanda · Per causes · Per salut/vida · Prohibit |
| `euthanasia` | Eutanàsia | Legal · Suïcidi assistit · Il·legal |
| `cannabis` | Cànnabis | Legal · Descriminalitzat · Il·legal |
| `voting_age` | Edat de vot | (número) anys |
| `conscription` | Servei militar obligatori | Sí · No · Selectiu |

### 15 · Turisme
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `tourist_arrivals` | Arribades turístiques | WB `ST.INT.ARVL` | pers./any | high | quantile | 4.0 |
| `tourism_receipts` | Ingressos pel turisme | WB `ST.INT.RCPT.CD` | USD | high | none | 4.0 |

### 16 · Pràctic per viatjar
| Key | Label | Source | Unit | Rank | Tier | Phase |
|---|---|---|---|---|---|---|
| `calling_code` | Prefix telefònic | RC `idd` | – | – | – | 4.0 |
| `tld` | Domini d'internet | RC `tld` | – | – | – | 4.0 |
| `driving_side` | Circulació | RC `car.side` → CA | – | – | – | 4.0 |
| `timezones` | Fus horari | RC `timezones` | – | – | – | 4.0 |
| `start_of_week` | Inici de la setmana | RC `startOfWeek` → CA | – | – | – | 4.0 |
| `plug_type` | Endoll i voltatge | CUR | – | – | – | 4.1 |

**Counts:** ~135 fields phase 4.0, ~30 phase 4.1. Total ~165.

---

## 5. Sources reference

| Source | Access | Notes |
|---|---|---|
| **REST Countries v4** | `GET https://restcountries.com/v4/all?fields=…` (or per-country) | **Build-time only.** Preview API (breaking changes possible) — safe because we bundle the static output; app never calls it at runtime. Provides anthem, government.type, religion, nationalHoliday, regionalBlocs, cioc, fifa, coatOfArms, demonyms, gini, car.side, etc. |
| **World Bank** | `GET https://api.worldbank.org/v2/country/all/indicator/{CODE}?format=json&mrnev=1&per_page=400` | One call per indicator → latest value for all countries, each with its `date` (→ `year`). Uses ISO3; map ISO3↔ISO2 once. |
| **WB Governance (WGI)** | same API, `*.PER.RNK` codes | Percentile ranks 0–100. |
| **UNDP HDR** | bundled CSV (`hdr.undp.org` latest release) | HDI, IHDI, GII, GDI, MPI, expected/mean schooling. Latest 2023/24 report. |
| **Curated bundles** | hand-authored JSON in `scripts/data/` | Social rights, Nobel, plug types, ethnic groups (from CIA `factbook.json`), independence year, motto, Schengen/NATO/OECD sets. Phase 4.1. |

**Codes marked *(verify)*** — WB has renamed some climate indicators (CO₂ family, PM2.5). The script author must confirm each returns data before relying on it; fall back to the documented replacement if deprecated.

---

## 6. Catalan strategy

* **Labels** — always Catalan (table above).
* **Categorical values translated to Catalan:** driving side (`right`→`Dreta`), government type, religion names, languages, border-country names (via our `countries.json` `nameCa`), continent/subregion, HDI tier, start-of-week, all social-rights statuses, booleans (`Sí`/`No`).
* **Numbers** — locale formatting only: thousands `.`, decimal `,` (`505.992`, `83,2`). Large counts compacted where natural (`49,6 M`). Units live in the `unit` column, appended at render.
* **Translation tables in the script:** ~45 languages, ~20 religions, ~25 government-type phrases, continents/subregions cover effectively all countries. Anything unmatched falls back to the source string (never blank).
* **Ethnic group names (4.1)** — major groups translated; obscure endonyms kept as-is.

---

## 7. Generation script architecture

`scripts/generate_country_stats.py` — modular, one fetcher per source, a merger assembles the final JSON. Re-runnable; re-run when any single source updates.

```
fetch_restcountries()   -> {iso2: {...rc fields...}}      # one HTTP call, v4
fetch_worldbank(codes)  -> {code: {iso3: (value, year)}}  # one call per indicator
fetch_wgi(codes)        -> percentile ranks
load_undp(csv)          -> {iso3: {hdi, ihdi, gii, ...}}
load_curated(dir)       -> {iso2: {social rights, nobel, plug, ...}}   # 4.1

build():
  for iso2 in our_244_countries:           # our countries.json is the spine
    facts = []
    for field in FIELD_CONFIG:             # the §4 catalog, as ordered config
      raw = resolve(field, sources, iso2)  # source priority + ISO3↔ISO2
      if raw is None: continue             # never emit empty rows
      facts.append(format_fact(field, raw))   # label, value (CA locale), unit, year, sortOrder
  assign_ranks_and_tiers(all_countries, FIELD_CONFIG)   # global pass per rankable metric
  write assets/data/country_stats.json
```

* **`FIELD_CONFIG`** is the §4 catalog encoded as a list: `key, category, labelCa, source, code, unit, rankable, direction, tierStyle, sortOrder, phase`. Single source of truth; adding a field = one config row.
* **Rank pass** runs after all values are collected: for each `rankable` field, sort countries by (direction-adjusted) value, assign `rank`/`rankTotal`, then bucket into `tier` per `tierStyle`.
* **Phase filter** — the current v4.0 output includes API-driven rows and the curated overrides listed in section 3c. Future rows use the same script and output file.

---

## 8. JSON output format

`assets/data/country_stats.json`, dataset version `2026.1`:

```json
{
  "version": "2026.1",
  "countries": [
    {
      "iso2": "ES",
      "facts": [
        { "category": "demografia", "key": "population", "labelCa": "Població",
          "value": "49.570.725", "unit": "hab.", "year": 2023,
          "rank": 30, "rankTotal": 195, "tier": "Gran", "sortOrder": 0 },
        { "category": "salut", "key": "life_expectancy", "labelCa": "Esperança de vida",
          "value": "83,2", "unit": "anys", "year": 2022,
          "rank": 4, "rankTotal": 190, "tier": "Capdavanter", "sortOrder": 0 }
      ]
    }
  ]
}
```

Importer flattens `facts` into `country_stat_facts` rows, stamping `country_iso2`. Coverage: RC v4 has ~250 entries, our dataset 244 — the ~6 mismatches just yield fewer rows.

---

## 9. Implementation summary

* **Implemented:** DB v22 + `country_stat_facts` + DAO/repository/domain mapping + source-agnostic importer + dataset-version tracking + API-driven fields + curated overrides + ranks/tiers.
* **Implemented:** Country Info presentation and screen rendering, including hidden empty sections and distribution parsing.
* **Implemented in DB v23:** optional country-photo metadata/cache used by the Country Info hero.
* **Future dataset work:** add remaining curated rows, regenerate JSON, and bump the dataset version. The flexible fact schema should not require an app migration.

---

## 10. Remaining work

1. Polish Country Info spacing, hierarchy, colors, and section defaults after device review.
2. Add remaining manual fields when reliable data is available.
3. Refresh time-sensitive curated snapshots as needed.
