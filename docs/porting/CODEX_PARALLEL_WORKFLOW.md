# Codex Parallel Workflow

Last condensed: 2026-03-11

## Goal

Run multiple Codex sessions without file collisions or doc drift.

Current priority:

- upstream `ch/njol/skript` closure first
- keep Stage 8 counts frozen unless the coordinator explicitly reassigns that work

## Mandatory Read Order

1. `docs/porting/README.md`
2. `docs/porting/NEXT_AGENT_HANDOFF.md`
3. `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
4. this file
5. `docs/porting/CODEX_PARALLEL_PROMPTS.md`
6. your assigned lane file under `docs/porting/parallel/`

## Worktrees

- Coordinator: `/Users/qf/.codex/worktrees/c842/Skript-Fabric-port`
- Lane A: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-a`
- Lane B: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-b`
- Lane C: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-c`

Use one worktree per lane. Do not share a worktree across active sessions.

## Ownership Matrix

| Role | Owns | Avoid |
| --- | --- | --- |
| `Coordinator` | merges, reconciliation, canonical docs, final verification | lane-owned feature work while lanes are active |
| `Lane A` | `Statement`, `ScriptLoader`, `log/**`, `ScriptLoaderCompatibilityTest` | parser core, variables/classes, canonical docs |
| `Lane B` | `SkriptParser`, parser-facing tests, pattern/default-value closure | loader/statement, variables/classes, canonical docs |
| `Lane C` | variables/classes/config/structures and adjacent function-runtime dependency work | loader/statement, parser core, canonical docs |

`Lane D` and `Lane E` are historical extra splits, not the default workflow.

## Shared Rules

- Workers do not edit canonical docs under `docs/porting/*.md`.
- Workers update only their own lane file under `docs/porting/parallel/`.
- Keep exact commands and exact results in the lane file.
- If user-visible `.sk` behavior changes, add real `.sk` coverage and rerun `runGameTest`.
- If a change overlaps another lane's scope, stop and hand it back to the coordinator.

## Merge Order

1. `Lane C`
2. `Lane B`
3. `Lane A`
4. coordinator integration fixes
5. canonical doc refresh
6. final verification
