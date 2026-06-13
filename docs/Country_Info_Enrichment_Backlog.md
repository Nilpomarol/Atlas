# Country Info — Enrichment Backlog

Last updated: 2026-06-12.

This document records the result of the render-taxonomy audit and the
enrichment work still outstanding for the Country Info screen. It exists to
answer one sequencing question: *can we polish the screen now without having to
remake it when we enrich the data later?*

## Conclusion

Yes. The render taxonomy is structurally complete:

- All 16 raw categories map to a display section (no orphan categories).
- Every fact resolves to one of 6 render types (`TEXT` is the catch-all).
- Each key renders as the same type across all 244 countries — no key is
  ambiguous between countries.

Therefore most enrichment is **content that fills existing slots** and can come
after polish. The only items that would force layout rework are the two new
render shapes in Tier 0, which should be decided before polishing.

## Tier 0 — decide before polishing (new render shapes)

These currently render as raw strings and have no proper visual home. Resolving
the *render decision* now (not the full data) protects the polish work.

- **Image / URL facts.** `coat_of_arms` (220 countries) is a raw `https://…`
  string shown as text. Decide: render as image via Coil, or hide. Likely also
  applies to any future `flag` / emblem fact.
- **Date facts.** `national_holiday` renders raw ISO (`1919-08-19`). Decide a
  date render variant with Catalan formatting.

## Tier 1 — data quality fixes (flow into existing slots)

- **Bad `national_holiday` years.** `AL` = `2912-11-28`, `AW` = `2976-03-18`
  are typos. `CH` = `1291-08-01` is historically correct (Swiss National Day)
  but may break naive date parsing — handle or special-case.
- **Missing units on numeric facts.** Many numeric facts render as bare numbers
  with no unit, e.g. `birth_rate`, `death_rate`, `fertility`,
  `life_exp_male` / `life_exp_female`, `co2_per_capita`, per-1.000 rates.
  Add `unit` metadata in the dataset so values read as `35,0 ‰`, `81,6 anys`,
  etc. No render change required.
- **`landlocked` boolean** renders as plain `Sí`/`No` outside the `drets`
  STATUS treatment. Optional: reuse a status/boolean chip.

## Tier 2 — content depth (more facts / more ranks)

- **More ranks.** Highlights and KPI tiers are driven by `rank`/`rankTotal`.
  Facts without ranks can't surface as highlights. Adding ranks to more facts
  enriches the highlights shelf with no layout change. Notably `gdi` (184
  countries) has no rank though its HDI sibling does.
- **List-shaped values** (`borders`, `languages`) are comma-joined strings.
  Render fine as text; optional future chip treatment.
- Optional scenic-query refinement for portrait country photos (carried over
  from the handoff remaining-review list).

## What is explicitly NOT needed before polish

- No new section is required — every category already has a section.
- No new fundamental render type beyond Tier 0 image + date.
- Bulk fact volume (the ~24k facts) does not need to grow before polishing;
  layout is data-driven and hides empty sections.
