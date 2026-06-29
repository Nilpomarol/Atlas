# Atlas Agentic Setup

This repository contains the active Codex and Claude Code setup for Atlas. The files are already installed in their final locations.

## Canonical Project Documents

Read these in priority order:

1. `docs/README.md` - documentation map, current release facts, and doc status.
2. `docs/Atlas_Technical_Architecture.md` - implemented architecture and boundaries.
3. `docs/Atlas_Data_Model.md` - implemented conceptual data model.
4. `docs/Atlas_Photo_Backup_Spec.md` - current backup/export/import contract.
5. `docs/Atlas_Product_Specification.md` - long-term product direction.
6. `docs/Atlas - Design System.html` - visual reference.
7. `docs/Atlas_Post_v2.0_Roadmap.md` - completed roadmap and future directions.
8. `docs/Handoff_Prompt.md` - historical development handoff snapshot.
9. Version-specific implementation specs, such as `docs/Atlas_v4.0_Country_Stats_Spec.md`.

Persistent agent rules live in root and nested `AGENTS.md` files. Claude wrappers import those rules through their corresponding `CLAUDE.md`.

## Agent Locations

Codex agents:

```text
.codex/agents/atlas_ui_polish_reviewer.toml
.codex/agents/atlas_compose_worker.toml
.codex/agents/atlas_data_reviewer.toml
.codex/agents/atlas_qa_reviewer.toml
.codex/agents/atlas_architecture_reviewer.toml
```

Claude Code agents:

```text
.claude/agents/atlas-ui-polish-reviewer.md
.claude/agents/atlas-compose-worker.md
.claude/agents/atlas-data-reviewer.md
.claude/agents/atlas-qa-reviewer.md
.claude/agents/atlas-architecture-reviewer.md
```

Codex agent identifiers use underscores. Claude Code agent identifiers use lowercase hyphens.

## Agent Roles

- `atlas_ui_polish_reviewer`: read-only UI and UX review.
- `atlas_compose_worker`: small, focused Compose implementation.
- `atlas_data_reviewer`: read-only Room, dataset, API, cache, and backup review.
- `atlas_qa_reviewer`: read-only regression and correctness review.
- `atlas_architecture_reviewer`: read-only layer and dependency review.

The Compose worker is the only custom agent intended to edit files.

## Recommended UI Polish Workflow

1. Run the UI polish reviewer against one screen or component.
2. Use `docs/ui-polish-checklist.md`.
3. Implement only the highest-impact items with the Compose worker.
4. Run the smallest relevant Gradle check.
5. Review the result on a device or screenshot when visual behavior changed.

Country Info review example:

```text
Use atlas_ui_polish_reviewer.

Review the Country Info screen.
Do not edit code.
Use docs/ui-polish-checklist.md, docs/Atlas_Data_Model.md, and docs/Atlas_v4.0_Country_Stats_Spec.md.
Return a prioritized polish plan.
```

Implementation example:

```text
Use atlas_compose_worker.

Implement only the top 2-3 high-impact items from the review.
Preserve behavior, architecture boundaries, and the current Room/data contract.
```

More task patterns are documented in `docs/codex-workflows.md`.

## Maintenance Rules

- Keep `docs/README.md`, architecture, data model, and backup docs synchronized with implemented database and release status.
- Treat `docs/Handoff_Prompt.md` as a handoff snapshot, not the only long-term source of truth.
- Keep agent prompts short and defer shared rules to `AGENTS.md`.
- Keep UI-specific rules under `app/src/main/java/com/atlas/ui/`.
- Keep data, domain, and presentation rules scoped to their matching directories.
- Remove superseded release documents instead of leaving conflicting operational instructions.
- Do not store secrets, API keys, tokens, or local machine paths in agent documentation.

