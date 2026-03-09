# Codex Parallel Workflow

Last updated: 2026-03-09

## Goal

Run multiple Codex app sessions in parallel against this repository without file-scope collisions,
doc-count drift, or unverifiable parity claims.

This workflow is specifically for the current priority workstream:

- reduce the raw `ch/njol/skript` shortfall by closing missing upstream package bundles
- defer polish on already-landed user-visible syntax unless it directly blocks new upstream imports
- keep Stage 8 package-local Bukkit audit frozen unless explicitly reassigned

## Required Roles

- `Coordinator`: owns planning, lane assignment, merge order, canonical doc updates, and final verification
- `Lane A`: owns `classes`, `registrations`, and `patterns`
- `Lane B`: owns `config`, `util`, and `localization`
- `Lane C`: owns `variables`, `sections`, `structures`, `aliases`, and `literals`
- `Lane D`: owns remaining `lang`, `log`, and function/parser blocker imports
- `Lane E`: owns scaffolding closure for `expressions`, `conditions`, `effects`, `events`, and `entity`

Recommended operating shape is `Coordinator + 5 workers`.

If only three workers are available, run `Coordinator + Lane A + Lane B + Lane C` first.
If only four workers are available, add `Lane D` before `Lane E`.

## Mandatory Read Order For Every Session

1. `docs/porting/README.md`
2. `docs/porting/NEXT_AGENT_HANDOFF.md`
3. `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
4. `docs/porting/FABRIC_PORT_STAGES.md`
5. this file
6. `docs/porting/CODEX_PARALLEL_PROMPTS.md`
7. the assigned lane file under `docs/porting/parallel/`

## Worktree Setup

Do not run multiple Codex sessions inside the same working tree.
Use one Git worktree per lane.

Recommended commands from the main repo root:

```bash
git worktree add ../Skript-Fabric-port-lane-a -b codex/lane-a
git worktree add ../Skript-Fabric-port-lane-b -b codex/lane-b
git worktree add ../Skript-Fabric-port-lane-c -b codex/lane-c
git worktree add ../Skript-Fabric-port-lane-d -b codex/lane-d
git worktree add ../Skript-Fabric-port-lane-e -b codex/lane-e
```

Recommended lane paths:

- `Lane A`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-a`
- `Lane B`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-b`
- `Lane C`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-c`
- `Lane D`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-d`
- `Lane E`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-e`

Coordinator can stay on the main repo path:

- `/Users/qf/IdeaProjects/Skript-Fabric-port`

## File Ownership Matrix

| Lane | Primary Scope | Allowed Core Files | Avoid Touching | Lane Status File |
| --- | --- | --- | --- | --- |
| `Lane A` | package-bundle closure for parser/type registry core | `src/main/java/ch/njol/skript/classes/**`, `src/main/java/ch/njol/skript/registrations/**`, `src/main/java/ch/njol/skript/patterns/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_A_STATUS.md` |
| `Lane B` | package-bundle closure for support runtime and config | `src/main/java/ch/njol/skript/config/**`, `src/main/java/ch/njol/skript/util/**`, `src/main/java/ch/njol/skript/localization/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_B_STATUS.md` |
| `Lane C` | package-bundle closure for stateful script structures | `src/main/java/ch/njol/skript/variables/**`, `src/main/java/ch/njol/skript/sections/**`, `src/main/java/ch/njol/skript/structures/**`, `src/main/java/ch/njol/skript/aliases/**`, `src/main/java/ch/njol/skript/literals/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_C_STATUS.md` |
| `Lane D` | package-bundle closure for remaining language core | `src/main/java/ch/njol/skript/lang/**`, `src/main/java/ch/njol/skript/log/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_D_STATUS.md` |
| `Lane E` | bulk surface scaffolding for missing syntax packages | `src/main/java/ch/njol/skript/expressions/**`, `src/main/java/ch/njol/skript/conditions/**`, `src/main/java/ch/njol/skript/effects/**`, `src/main/java/ch/njol/skript/events/**`, `src/main/java/ch/njol/skript/entity/**`, tightly matching tests | org runtime syntax polish, canonical docs, other `ch/njol/skript` package bundles except minimal blocker glue | `docs/porting/parallel/LANE_E_STATUS.md` |
| `Coordinator` | merge, reconciliation, canonical docs, final verification | `docs/porting/*.md`, root pointer docs, integration fixes after merge | lane-owned feature work while workers are active | n/a |

## Shared Rules

- worker reasoning default is `medium`; raise a lane to `high` only for a confirmed hard blocker
- compare against local upstream snapshots only (`/tmp/skript-upstream-e6ec744-2`, `/tmp/upstream-skript`)
- do not browse the web for upstream comparisons
- use conventional-style commit messages without lane prefixes
- prioritize missing upstream classes over fixes to already-landed local syntax
- do not spend a lane on polish-only diffs unless that polish blocks new upstream class imports
- Do not edit the canonical docs under `docs/porting/*.md` from worker lanes.
- Do not edit another lane's status file.
- Do not change Stage 8 counts unless your lane actually changes that tracked matrix and the coordinator approved the reassignment.
- Do not revert unrelated dirty changes.
- Do not claim parity complete unless it is verified.
- If user-visible `.sk` behavior changes, add or update real `.sk` coverage and run the narrowest matching runtime tests; coordinator handles final `./gradlew build --rerun-tasks`.
- Work in package bundles, not one-off syntax slices.
- A lane may land multiple commits in one batch if they stay inside its owned bundle and keep moving the same closure track forward.
- Use one primary bundle and one fallback bundle inside the same ownership area; do not stop after a single small fix if more bundle-local imports remain unblocked.
- Surface-package lanes should prefer shared base classes, abstract helpers, and import-enabling scaffolding before leaf syntax classes.
- Record exact commands and exact counts in the lane status file.
- If a lane needs a file owned by another lane, stop and hand it back to the coordinator instead of freelancing into overlap.

## Worker Output Contract

Each worker must leave behind all of the following in its own branch/worktree:

1. code changes limited to the lane scope
2. as many upstream-backed class imports or bundle-local behavior closures as can be verified in that lane
3. test changes for the imported bundle behavior
4. one updated lane status file under `docs/porting/parallel/`
5. exact verification commands and results
6. a short merge note listing the files most likely to conflict
7. one or more conventional-style commits if code lands

Workers do not update:

- `docs/porting/PORTING_STATUS.md`
- `docs/porting/NEXT_AGENT_HANDOFF.md`
- `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
- `docs/porting/FABRIC_PORT_STAGES.md`
- `docs/porting/IMPLEMENTED_SYNTAX.md`
- root pointer docs

Those are coordinator-owned.

## Suggested Validation Split

- `Lane A`:
  - targeted classes/registrations/patterns compatibility tests first
- `Lane B`:
  - targeted config/util/localization compatibility tests first
- `Lane C`:
  - targeted variables/sections/structures compatibility tests first
- `Lane D`:
  - targeted lang/log/function/parser compatibility tests first
- `Lane E`:
  - targeted expressions/conditions/effects/events/entity scaffolding tests first
- `Coordinator` after merge:
  - `./gradlew build --rerun-tasks`

## Merge Order

Use this merge order unless the actual diff dictates otherwise:

1. `Lane A`
2. `Lane B`
3. `Lane C`
4. `Lane D`
5. `Lane E`
6. coordinator integration fixes
7. canonical doc update
8. final verification

Reasoning:

- `Lane A` defines parser/type registry surfaces other bundles depend on
- `Lane B` and `Lane C` mostly extend support/runtime infrastructure that should settle before deep lang and surface imports
- `Lane D` is the remaining language core and can depend on the lower bundles
- `Lane E` should merge last because surface-package scaffolding is most likely to consume earlier bundle work

## Coordinator Checklist

1. confirm each lane is on a separate worktree and branch
2. confirm each lane owns only its file scope
3. collect each lane's status file and diff summary
4. merge branches in the recommended order
5. resolve integration fallout in the coordinator worktree only
6. rerun full verification
7. update canonical docs with exact counts, exact scope, and exact test results
8. refresh root pointer headlines if the headline numbers changed

## Stop Conditions

A worker should stop and return to the coordinator if any of the following happen:

- a required edit overlaps another active lane's owned file set
- the lane discovers a design decision that changes another lane's scope boundary
- the lane cannot verify a user-visible behavior change with tests or real `.sk`
- the lane sees unexpected modifications in files it is actively editing and cannot explain them

## Current Recommended Lane Assignment

- `Lane A`: close `classes` / `registrations` / `patterns`
- `Lane B`: close `config` / `util` / `localization`
- `Lane C`: close `variables` / `sections` / `structures` / `aliases` / `literals`
- `Lane D`: close remaining `lang` / `log` blocker imports
- `Lane E`: build import-enabling scaffolding for `expressions` / `conditions` / `effects` / `events` / `entity`
