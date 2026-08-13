# Trips list — concept image brief

Purpose: source material for generating reference imagery for the Phase 5 Trips list.

Per `docs/Atlas_UI_Rework_Implementation_Plan.md` §5, generated imagery establishes
**composition and visual intent**, not literal text, geography, measurements, or
business truth. Every field below is real, drawn from `TripListItemUiState`.

---

## 1. What this screen is

A browse surface listing every trip the user has recorded, newest first. It is **not**
the map canvas — Home owns the full-screen map. Trips is a scrolling list of floating
cards on warm paper, in the same frame as the Countries list.

A trip is a travel memory. The page should feel like flipping through a personal
travel journal, not scanning a database table.

---

## 2. Screen zones, top to bottom

### 2.1 Editorial header

| Element | Content | Role |
|---|---|---|
| Eyebrow | `VIATGES` | Small caps, accent orange, monospace-adjacent label |
| Title | `El teu recorregut` | Large serif display, near-black ink |
| Counter | `12` | Monospace, right-aligned, quiet |

### 2.2 Control row

Two equal-width rounded controls side by side, each a small stacked label + value:

- `FILTRA` → `Tots` (other values: `En curs`, `Planejats`, `Completats`)
- `ORDENA` → `Data ↓` (other values: `Data ↑`, `Títol`, `Durada`)

No search field. The user has tens of trips, not hundreds.

### 2.3 Group headings

Quiet uppercase labels separating the list: `EN CURS`, `PLANEJATS`, `COMPLETATS`,
`SENSE ESTAT`. Muted grey, small, generous space above.

### 2.4 Trip cards

See §3.

### 2.5 Bottom navigation

Five destinations: `Inici`, `Països`, `Viatges` (active), `Vols`, `Progrés`.
Above the bottom-right of the bar sits a compact rectangular `Registra` action,
separate from navigation.

---

## 3. The trip card — one card, four levels of degradation

There is **one** card. Its parts fall away as the trip carries less data. Do not draw
these as four different components.

### Level A — full card (cover photo present)

Photo band across the top of the card, roughly 40% of card height, with:

- a **status pill** floating top-right on the image;
- a dark gradient scrim rising from the bottom of the image;
- the **trip title** in large serif over the scrim;
- the **date** below it in monospace.

Below the image, on paper:

- **main route** in monospace: place names joined by orange arrows —
  `Tokyo → Osaka`;
- **side-trip line** (only when nested places exist): indented, tinted vertical rule
  on its left, muted monospace list — `Kamakura · Enoshima · Nikko`;
- **footer row**: country flag emoji on the left, and on the right a quiet uppercase
  metric — `2 PARADES · 3 SORTIDES`.

### Level B — full card, no cover photo

Identical, but the photo band is a **deterministic gradient** derived from the trip,
in deep editorial tones (teal-green, ochre-brown, slate-blue, plum). Never a grey box,
never a broken-image icon. Title and date still sit over it.

### Level C — single-place trip

Compact single row, no image: flag emoji, serif title, monospace
`LISBOA · MAR 2026`, and a small status dot on the right. Roughly one third the
height of a full card.

### Level D — bare trip (title only)

Same compact row, further reduced: faded globe glyph instead of a flag, title, and
`SENSE DATA · SENSE PARADES` in monospace. Muted dot. Must read as deliberate and
calm, not as an error.

---

## 4. Real data fields available

Everything the card can show, and nothing it cannot:

| Field | Example | Notes |
|---|---|---|
| Title | `Japó 2026` | Only mandatory field on a trip |
| Status | `EN CURS` | One of four; see colour map |
| Date | `1–14 JUN 2026`, `SET 2025`, `2024` | Flexible precision: day, month, or year only |
| Main route | `Tokyo → Osaka` | First and last stop, or full chain when short |
| Side trips | `Kamakura · Enoshima` | Places visited *from* a main stop |
| Main stop count | `2 PARADES` | |
| Side trip count | `3 SORTIDES` | Omit the segment entirely when zero |
| Countries | `3 PAÏSOS` or flags | Distinct countries across all stops |
| Flags | 🇯🇵 / 🇨🇭🇦🇹🇮🇹 | Emoji flags, up to three then overflow |
| Cover photo | landscape photo | Optional |

Notes exist on a trip but do **not** appear in the list.

---

## 5. Vocabulary — Catalan, and what is banned

Visible text is Catalan. Use exactly these words:

- `PARADES` — stops on the main route
- `SORTIDES` — places visited from a stop
- `PAÏSOS`, `VIATGES`, `FILTRA`, `ORDENA`, `Registra`
- Status: `EN CURS`, `PLANEJAT`, `COMPLETAT`

**Never show these words.** They name concepts deliberately removed from the product:

- `Excursió` / `excursions`
- `Itinerari` / `grup`
- `Viatge ràpid` vs `viatge normal` — there is one trip type only

---

## 6. Visual system

Direction: **Warm Editorial Atlas / Cartographer's Ink** — warm printed paper, real
ink contrast, calm and premium. Data is the decoration: routes, counts, and dates
carry the visual interest.

### Typefaces

| Role | Family | Used for |
|---|---|---|
| Display | Fraunces (serif, heavy) | Page title, trip titles |
| UI / body | Hanken Grotesk | Labels, buttons, secondary text |
| Instrument | Space Mono | Dates, routes, counts, codes |

Monospace is the instrument layer. Never set long prose in it.

### Palette

| Token | Hex | Use |
|---|---|---|
| Paper | `#F8F2E7` | Page background |
| Card | `#FFFBF2` | Card surface |
| Ink | `#182C2A` | Primary text |
| Muted ink | `#62706C` | Secondary text |
| Accent | `#D85B37` | Eyebrows, route arrows, small accents |
| In progress | `#CE5231` | Status |
| Planned | `#D9A11D` | Status |
| Completed | `#168D72` | Status |
| Deep navy | `#012E5D` | Map water, if any map appears |

Colour always carries stable meaning — status colours are never decorative.

### Form

- Cards: ~14px corner radius, hairline warm border, soft low shadow.
- Cards float on visible paper; space separates them.
- Sentence case in prose, uppercase reserved for small labels and metrics.
- No heavy Material chrome, no elevation stacks, no pure white, no pure black.

---

## 7. States worth generating separately

1. **Populated** — mixed statuses, some with covers, at least one with side trips.
2. **Empty** — no trips at all: a single invitation card with a short Catalan line
   and one clear action. An invitation, not an apology.
3. **Degraded** — a screen where most trips lack covers and dates, to prove the
   layout stays calm.
4. **Long text** — a very long Catalan trip title and a five-country flag row, to
   test wrapping and overflow.

---

## 8. Prompt seed

> A mobile app screen for a personal travel journal, warm printed-atlas aesthetic.
> Cream paper background (#F8F2E7), floating cards in warmer off-white (#FFFBF2)
> with hairline borders and soft shadows. Top: small orange eyebrow label
> "VIATGES", large heavy serif heading "El teu recorregut", small monospace counter.
> Two rounded filter/sort controls below. Then a vertical list of trip cards: each
> rich card has a landscape photo band with a coloured status pill, the trip title in
> heavy serif over a dark gradient, a monospace date, and below the photo a monospace
> route of place names joined by orange arrows, an indented muted line of side-trip
> place names, and a footer with flag emoji and a small uppercase metric. One card is
> a compact single row with just a flag, title, and monospace location and date.
> Bottom navigation bar with five items and a small rectangular action button.
> Editorial, calm, premium, high contrast ink on paper. No Material Design chrome.
