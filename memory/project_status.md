---
name: project-status
description: Current Atlas app development phase, last completed work, and next items
metadata:
  type: project
---

v3.2 is complete (per-stop photos, cover photos, DB v21). Navbar root-nav fix done.

Stats screen Mapa tab ✅ complete (2026-06-09):
- No scroll conflict: Map tab fills screen, other tabs remain scrollable
- `StatsMapCanvas.kt` — fullscreen canvas with projection-based zoom (no graphicsLayer), fixed-size markers, pan/pinch up to 20×, tooltip dismisses on move
- 50m Natural Earth GeoJSON in assets — higher-quality country outlines
- Six filter-toggleable layers: country state fills, solid completed flight arcs, dashed planned arcs, airport dots, trip stop markers, excursion stop markers
- Tap-to-identify (priority: trip stops → excursion stops → airports → route midpoints → country polygon ray-cast)
- Starting position: centered on living country at 5× zoom via LaunchedEffect; falls back to world view
- Reset button: top-right Refresh icon restores the personalised initial position
- `MapFilterOverlay`: collapsible 2×3 grid of layer toggles (bottom-right)
- `GeoProjection` gained `projectZoomed()` and `unproject()`
- `StatsFlightMapRoute` gained `isPlanned`, `fromCode`, `toCode`; `StatsMapMarker` added to ViewModel

**Why:** pre-v4.0 stats polish pass. Resum + Cronologia + Mapa tabs all complete.

All pre-v4.0 items now done (auto-status update committed `9187e13`; all Stats tabs complete).

**v4.0 — Country depth: planning complete (2026-06-09).** Dataset design fully specced in `Documentation/Atlas_v4.0_Country_Stats_Spec.md`:
- Flexible `country_stat_facts` table (DB v22), composite PK `(country_iso2, category, key)`, columns incl. `rank`/`rank_total`/`tier`. Adding fields needs no migration.
- ~165 fields, 16 categories, Catalan labels + categorical values. Global ranks + Catalan tiers (Capdavanter/Alt/Mitjà/Baix/Inferior; hdi & named overrides).
- Two-phase: v4.0 ≈ 135 API-driven fields (zero curation), v4.1 ≈ 30 curated (independence year, ethnic groups, motto, social rights, Nobel, plug types, Schengen/NATO/OECD). v4.1 = more JSON rows, no app code change.
- Sources: **REST Countries v4** (build-time only — preview API, but safe since output is bundled; gives anthem, government.type, religion, nationalHoliday, regionalBlocs, cioc, fifa, coatOfArms, gini, car.side), **World Bank** API (`mrnev=1`, ~95 indicators), **UNDP HDR** CSV, **WB Governance** percentile ranks, curated bundles.
- Generation: `scripts/generate_country_stats.py` (to write) → `assets/data/country_stats.json` v2026.1.

**v4.0 M1 — IN PROGRESS (2026-06-09).** Schema + importer + generation script all written; real dataset generated.

Done:
- Kotlin DB v22: `CountryStatFactEntity` (composite PK `country_iso2,category,key` + rank/rank_total/tier cols), `CountryStatFactDao` (observeByCountry/clear/upsertAll), `CountryStatFact` domain + mapper, `CountryStatRepository`(+Impl), `CountryStatDatasetDto`, `CountryStatDatasetImporter` (full-replace on version change). Wired into `AtlasDatabase` (v22 + `MIGRATION_21_22`), `DatasetConstants` (`COUNTRY_STATS_KEY/VERSION="2026.1"`), `AtlasAppContainer`.
- `scripts/generate_country_stats.py` → `assets/data/country_stats.json` (v2026.1): **244 countries, 21,293 facts, avg 87/country, 6.56 MB, valid UTF-8.** Catalan formatting + translation, global ranks + tiers all working.

Data-source lessons (CRITICAL for re-runs):
- **World Bank: use `mrv=5` (most-recent-5-values) + pagination, NOT `mrnev=1` or date ranges** — the latter two TIME OUT badly from this network (caused a 13-min hang). mrv=5 is reliable (~3-10s/indicator). Key by `countryiso3code`, keep latest non-null. 78 WB indicators, coverage 100-261 countries.
- **WGI governance percentile ranks (PV/GE/RL/CC/VA/RQ.PER.RNK) return ~0 countries — dropped.** Governance indices (CPI, press freedom) come from curated files in v4.1.
- **IMF DataMapper** (debt `GGXWDG_NGDP`, fiscal `GGXCNL_NGDP`) needs a browser User-Agent and IP-blocks (403) after repeated hits — both fields came back EMPTY this run. Re-run from a fresh network/IP to fill them.
- **UNDP HDR CSV** (HDR25 composite indices) gives hdi/ihdi/gii/gdi/mys/eys — worked great (~170-195 countries).
- Reference for technique + curated data: old project `C:\Users\nilpo\Documents\Projectes\Travel-app-2.0\Atlas\worker\` — has `fetch-countries.ts` (proven WB approach) and curated files: `nobel-prizes-by-country-2026.json`, `manual-overrides.json` (CPI+press freedom), `manual-overrides2.json` (UNESCO+highest point), plus already-generated `country-static.json`/`country-dynamic.json`.

`assembleDebug` BUILD SUCCESSFUL — schema/Room/KSP all compile (DB v22 verified).

**Religion + ethnic groups are now breakdowns (2026-06-09):** RC v4 `religion` (every country) and `ethnicity` (partial — SG/NG yes, US/ES/IN no) stored as a **JSON-array string** in the fact `value`: `[{"name":"Catolicisme romà","pct":58.6}, …]`. key `religion` (Cultura, label "Religió"), key `ethnic_groups` (Demografia, label "Grups ètnics"). `pct` is a raw JSON number; UI formats it. No schema/DTO change (value is already String). M2 screen parses the JSON. Names translated via `RELIGION_CA`/`ETHNICITY_CA`. Ethnic groups thus moved v4.1→v4.0. Generator re-run in progress to bake these in.

**Curated overrides integrated (2026-06-09):** new `scripts/build_curated_overrides.py` consolidates all manual/curated data → `scripts/data/curated_overrides.json` (keyed by iso2); the generator loads it via `CURATED_NUMERIC`/`CURATED_TEXT` + `load_curated()`. Fields added (mostly moved v4.1→v4.0):
- Governança: `cpi` (Transparency Intl 2024, 182), `press_freedom` (RSF 2025 score, 177), `democracy_index` (EIU 2024, 167), `schengen`/`nato_member`/`oecd_member` (Sí/No, 244 each).
- Cultura: `nobel_laureates` (79), `unesco_sites` (172).
- Geografia: `highest_point` (224, e.g. "Teide · 3.715 m").
- Finances: `gov_debt` + `fiscal_balance` (% PIB) — now from curated (old project's IMF data, 192/195), since the live IMF API IP-blocks this env. IMF fetch code removed from generator.
- New category **`drets` (Drets i societat)**: `death_penalty` (48, Amnesty 2025 — "Vigent" only; abolitionist countries left blank, one-sided), `same_sex_marriage` (39 "Legal" + Israel "Reconegut (estranger)"), `euthanasia` (25: Legal/Suïcidi assistit/Només passiva), `abortion` (202, Center for Reproductive Rights WALM 2023 → Lliure demanda/Per causes socioeconòmiques/Per preservar la salut/Per salvar la vida/Prohibit/Varia).

Source files live in old project `Travel-app-2.0/Atlas/worker/src/data/` (nobel, manual-overrides, manual-overrides2, democracy-index-eiu.csv, country-dynamic.json); `build_curated_overrides.py` reads from there + inline pasted lists. **Caveats:** Schengen list used the user's verbatim list (incl. Cyprus); social-rights fields are partial/one-sided snapshots needing periodic refresh.

Still pending (the "other manuals", to do later): cannabis, voting_age, conscription, independence_year, motto, plug_type. Re-run after adding them.

Minor polish deferred: gov-type translation is word-by-word/awkward; gentilici + national_holiday still English (v4.1). Full spec in `Documentation/Atlas_v4.0_Country_Stats_Spec.md`.

Next after M1: M2 Country Info screen (design once dataset reviewed — now available).
