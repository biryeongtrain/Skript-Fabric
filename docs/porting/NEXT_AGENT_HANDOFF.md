# Next Agent Handoff

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Read Order

1. [README.md](README.md)
2. this file
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) and [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) only for parallel runs

## Current State

- Source ports complete: conditions `28 / 28`, expressions `84 / 84`, effects `24 / 24`
- Stage 5 event backend rows active: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Upstream `ch/njol/skript` baseline: local `140`, upstream `1189`, shortfall `1049`
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
  - `./gradlew build --rerun-tasks` passed

## Most Recent Merged Slice

- legacy `parseStatic` expression-placeholder flags restored
- explicit-literal-only `Classes.getPatternInfos(...)` restored
- keyed plural default behavior in `Function.execute(...)` restored
- runtime implementation overload choice locked with regression coverage

## Do Next

- Continue `Part 1A` only if you can reproduce a concrete parser/loader mismatch.
- Prefer tight `Part 1B` gaps in `Variables`, `Classes`, `config`, `log`, or adjacent helpers.
- Keep Stage 8 package counts unchanged unless you actually audit another package.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun GameTests.

## Guardrails

- Do not claim parity complete.
- Do not re-expand these docs with long logs.
- Do not revive the 5-lane parallel doc expansion.
- Preserve exact counts and exact verification outcomes.
