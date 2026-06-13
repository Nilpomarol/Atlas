# Atlas UI Polish Checklist

## Purpose

Use this checklist when reviewing or polishing Atlas UI.

Atlas should feel like a warm editorial travel atlas: visual, personal, calm, readable, and structured without looking like a generic productivity app.

## 1. Screen-level hierarchy

- Is there one clear subject for the screen?
- Is the hero/map/route/stat/fact clearly the main focus?
- Are supporting details visually quieter?
- Can the user understand the screen in 3 seconds?
- Is the primary action obvious?
- Are destructive or secondary actions visually de-emphasized?

## 2. Atlas visual language

Check that the screen follows:

- warm paper surfaces
- deep ink text
- subtle borders
- restrained accents
- editorial headings
- monospace labels/data where appropriate
- data-driven decoration: routes, ledgers, timelines, ranks, maps, facts

Avoid:

- generic Material-only look
- random local color palettes
- overuse of saturated color
- repeated identical cards with no visual rhythm
- decorative icons that do not clarify meaning

## 3. Typography

- Display/headline/title text uses the Atlas display style.
- Body text remains readable and not overly decorative.
- Monospace is reserved for codes, labels, ranks, dates, coordinates, compact metadata, flight numbers, airport codes, and similar instrument-layer text.
- Long paragraphs do not use monospace.
- Numeric/stat values have enough visual weight.
- Secondary metadata is clearly secondary.

## 4. Spacing and density

- Screen horizontal padding is consistent.
- Section gaps are consistent.
- Cards have consistent internal padding.
- Icon/text spacing is consistent.
- Rows are not cramped.
- Cards are not overfilled.
- Empty space feels intentional, not accidental.
- There are no awkward one-off spacer values.

## 5. Alignment

- Icons align with text baseline or visual center.
- Labels and values align consistently across rows.
- Clear/delete buttons do not float awkwardly.
- Chips align with adjacent text.
- Multi-line text does not break row rhythm.
- Flight route endpoints and time rows remain readable.

## 6. Components and reuse

Before accepting a polish change:

- Is this pattern already represented by a shared component?
- Should this repeated pattern become a reusable composable?
- Does the new component fit the existing component kit?
- Does it duplicate another card/row/field with only minor differences?
- Is the component API stable and minimal?

## 7. States

For every relevant screen/component, check:

- loading state
- empty state
- error state
- partial-data state
- offline/no API key state where applicable
- null/missing value state
- very long text
- very short text
- small screen
- dark mode if supported

## 8. Catalan UI text

- User-facing labels are Catalan.
- Catalan strings fit the layout.
- Long Catalan labels do not overflow badly.
- Technical provider names are hidden unless needed.
- Raw API labels are not shown directly.

## 9. Country Info screen checks

- Empty categories are hidden.
- Dense fact lists remain browseable.
- Category headings are clear.
- Rank/tier/year metadata is useful but not visually dominant.
- Breakdown facts such as religion/ethnicity are rendered as distributions, not raw JSON.
- Country facts feel like an atlas page, not a spreadsheet.
- Hero photo/map/facts do not compete for attention.

## 10. Country detail checks

- The country identity is immediately clear.
- Country state chips use consistent state colors.
- Map/hero feels integrated with the page.
- Stats summary is useful without being dense.
- Related trips/flights/logs are easy to scan.

## 11. Flight UI checks

- Origin and destination are immediately clear.
- Airport codes and route arrows are readable.
- Scheduled vs actual times are visually distinct.
- Local/UTC mode is clear.
- Arrival day offsets are visible when relevant.
- Airline logo/name fallback works.
- Aircraft image fallback works.
- Duration, distance, and delay are not buried.

## 12. Trip and itinerary UI checks

- Trip status and date range are clear.
- Route progression is readable.
- Manual stops, generated itinerary stops, and excursion stops are visually distinguishable.
- Reorder/edit/delete states are not confusing.
- Photos enhance memory value without making rows too dense.

## 13. Form/dialog checks

- The form supports quick capture.
- Optional fields do not feel mandatory.
- Validation errors are understandable.
- Date/time pickers are clear.
- Scheduled vs actual groups are visually distinct.
- Search suggestions are readable and not visually noisy.
- Manual fallback is discoverable when search fails.

## 14. Final polish review output

When reviewing UI, report findings in this priority order:

1. User-visible bugs or confusing behavior.
2. Inconsistencies with Atlas design system.
3. Missing states.
4. Component reuse opportunities.
5. Small visual refinements.
6. Subjective ideas clearly marked as optional.
