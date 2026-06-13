# Atlas Codex Workflows

## Purpose

This document defines practical Codex workflows for Atlas.

Use `AGENTS.md` files for persistent rules. Use custom agents only when they add value: exploration, review, UI polish, data correctness, or complex multi-layer changes.

## Default rule

Do not use multiple agents for every task.

Use:

```text
Small change       → normal Codex task
Component polish   → atlas_compose_worker
Screen polish      → atlas_ui_polish_reviewer, then atlas_compose_worker
Data/API change    → atlas_data_reviewer when risk is meaningful
Final PR review    → atlas_qa_reviewer
```

## Workflow A — Small UI component polish

Use for a single card, field, chip, row, or section.

Prompt:

```text
Use atlas_compose_worker.

Polish [ComponentName].

Goals:
- Keep current behavior.
- Improve [alignment / spacing / hierarchy / density].
- Reuse existing Atlas theme tokens and components.
- Do not change navigation, data models, or ViewModels unless necessary.
- Do not introduce dependencies.

Before editing, inspect nearby usages so the component stays consistent.
After editing, summarize files changed, visual changes, checks run, and manual review needed.
```

## Workflow B — Screen polish review before implementation

Use before polishing a full screen.

Prompt:

```text
Use atlas_ui_polish_reviewer.

Review [ScreenName].
Do not edit code.

Focus on:
- visual hierarchy
- spacing
- typography
- component reuse
- missing states
- consistency with the Atlas design system
- long Catalan text
- small-screen behavior

Return a prioritized implementation plan with high-impact items first.
```

Then implement:

```text
Use atlas_compose_worker.

Implement only the high-impact items from the previous UI polish review.
Keep the changes small.
Preserve behavior.
Do not redesign unrelated areas.
Run relevant checks if available.
```

## Workflow C — Country Info screen polish

Use during v4.0 polish.

Prompt:

```text
Use atlas_ui_polish_reviewer.

Review the Country Info screen.
Do not edit code.

Use docs/ui-polish-checklist.md and docs/Atlas_v4.0_Country_Stats_Spec.md.

Focus on:
- dense fact readability
- section hierarchy
- ranks and tiers
- year metadata
- hidden empty sections
- distribution/breakdown rendering
- Catalan labels
- whether the screen feels like an atlas page rather than a spreadsheet

Return a prioritized polish plan.
```

Implementation prompt:

```text
Use atlas_compose_worker.

Apply the top 2–3 Country Info polish improvements from the review.
Constraints:
- Do not change the country_stat_facts schema.
- Do not change importer behavior.
- Do not change domain/data models unless a UI state fix requires it.
- Keep visible text Catalan.
- Preserve empty-section hiding.
```

## Workflow D — Data/API correctness review

Use for Room, dataset importer, API client, or backup/import changes.

Prompt:

```text
Use atlas_data_reviewer.

Review the current change for data correctness.
Do not edit code.

Focus on:
- Room migrations
- entity/domain mapping
- dataset import idempotence
- backup/import compatibility
- stable identifiers
- null/missing data handling
- API error handling
- local-first constraints

Return concrete findings only.
```

## Workflow E — Final PR review

Use before committing/merging a larger change.

Prompt:

```text
Use atlas_qa_reviewer.

Review the current diff as a final Atlas QA pass.
Do not edit code.

Prioritize:
- user-visible bugs
- regressions
- broken empty/loading/error states
- navigation problems
- data loss risk
- incorrect country/trip/flight derivation
- missing checks/tests for important logic

Ignore purely subjective style unless it affects consistency or usability.
```

## Workflow F — Feature touching multiple layers

Use for features that touch UI + ViewModel + domain + data.

Prompt:

```text
First use atlas_qa_reviewer in read-only mode to identify risk areas and relevant files.
Then use atlas_compose_worker or normal Codex implementation depending on the task.
If data/API/Room is touched, run atlas_data_reviewer after implementation.
Finally run relevant Gradle checks.
```

## Good task scoping examples

Good:

```text
Polish DateTimeDisplayCard alignment and filled/empty visual states.
```

Good:

```text
Review FlightEditScreen for scheduled vs actual time clarity. Do not edit code.
```

Good:

```text
Improve Country Info section hierarchy without changing the country_stat_facts schema.
```

Bad:

```text
Make the UI better.
```

Bad:

```text
Redesign Atlas.
```

Bad:

```text
Clean up the whole codebase.
```

## Token discipline

- Scope prompts to one component, one screen, or one flow.
- Ask for read-only review before implementation on large UI tasks.
- Avoid asking every agent to inspect the full repo.
- Do not use subagents for tiny changes.
- Prefer AGENTS.md rules for repeated constraints instead of repeating them in every prompt.
