# Codex Parallel Workflow

Last updated: 2026-03-11

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
- `Lane D`: owns remaining `lang`, `log`, and function/parser dependency closure
- `Lane E`: owns `expressions` and `conditions`
- `Lane F`: owns `effects`, `events`, and `entity`

Recommended operating shape is `Coordinator + 6 workers`.

If only three workers are available, run `Coordinator + Lane A + Lane B + Lane C` first.
If only four workers are available, add `Lane D` before `Lane E`.
If only five workers are available, add `Lane E` before `Lane F`.

## Mandatory Read Order For Every Session

1. `docs/porting/README.md`
2. `docs/porting/NEXT_AGENT_HANDOFF.md`
3. `docs/porting/EXTERNAL_LIBRARY_GAPS.md`
4. `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
5. `docs/porting/FABRIC_PORT_STAGES.md`
6. this file
7. `docs/porting/CODEX_PARALLEL_PROMPTS.md`
8. the assigned lane file under `docs/porting/parallel/`

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
git worktree add ../Skript-Fabric-port-lane-f -b codex/lane-f
```

Recommended lane paths:

- `Lane A`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-a`
- `Lane B`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-b`
- `Lane C`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-c`
- `Lane D`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-d`
- `Lane E`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-e`
- `Lane F`: `/Users/qf/IdeaProjects/Skript-Fabric-port-lane-f`

Coordinator can stay on the main repo path:

- `/Users/qf/IdeaProjects/Skript-Fabric-port`

## File Ownership Matrix

| Lane | Primary Scope | Allowed Core Files | Avoid Touching | Lane Status File |
| --- | --- | --- | --- | --- |
| `Lane A` | package-bundle closure for parser/type registry core | `src/main/java/ch/njol/skript/classes/**`, `src/main/java/ch/njol/skript/registrations/**`, `src/main/java/ch/njol/skript/patterns/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_A_STATUS.md` |
| `Lane B` | package-bundle closure for support runtime and config | `src/main/java/ch/njol/skript/config/**`, `src/main/java/ch/njol/skript/util/**`, `src/main/java/ch/njol/skript/localization/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_B_STATUS.md` |
| `Lane C` | package-bundle closure for stateful script structures | `src/main/java/ch/njol/skript/variables/**`, `src/main/java/ch/njol/skript/sections/**`, `src/main/java/ch/njol/skript/structures/**`, `src/main/java/ch/njol/skript/aliases/**`, `src/main/java/ch/njol/skript/literals/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_C_STATUS.md` |
| `Lane D` | package-bundle closure for remaining language core | `src/main/java/ch/njol/skript/lang/**`, `src/main/java/ch/njol/skript/log/**`, tightly matching tests | other `ch/njol/skript` package bundles, canonical docs | `docs/porting/parallel/LANE_D_STATUS.md` |
| `Lane E` | bulk closure for expression and condition surfaces | `src/main/java/ch/njol/skript/expressions/**`, `src/main/java/ch/njol/skript/conditions/**`, tightly matching tests | `effects`, `events`, `entity`, org runtime syntax polish, canonical docs, other `ch/njol/skript` package bundles except minimal blocker glue | `docs/porting/parallel/LANE_E_STATUS.md` |
| `Lane F` | bulk closure for effects, events, and entity surfaces | `src/main/java/ch/njol/skript/effects/**`, `src/main/java/ch/njol/skript/events/**`, `src/main/java/ch/njol/skript/entity/**`, tightly matching tests | `expressions`, `conditions`, org runtime syntax polish, canonical docs, other `ch/njol/skript` package bundles except minimal blocker glue | `docs/porting/parallel/LANE_F_STATUS.md` |
| `Coordinator` | merge, reconciliation, canonical docs, final verification | `docs/porting/*.md`, root pointer docs, integration fixes after merge | lane-owned feature work while workers are active | n/a |

## Shared Rules

- worker reasoning default is `medium`; raise a lane to `high` only for a confirmed hard blocker
- compare against local upstream snapshots only (`/tmp/skript-upstream-e6ec744-2`, `/tmp/upstream-skript`)
- do not browse the web for upstream comparisons
- use conventional-style commit messages without lane prefixes
- prioritize missing upstream classes over fixes to already-landed local syntax
- do not spend a lane on polish-only diffs unless that polish blocks new upstream class imports
- do not stop a lane after the first small win; keep moving inside the owned bundle until it is clearly blocked or exhausted
- Do not edit the canonical docs under `docs/porting/*.md` from worker lanes.
- Do not edit another lane's status file.
- Do not change Stage 8 counts unless your lane actually changes that tracked matrix and the coordinator approved the reassignment.
- Do not revert unrelated dirty changes.
- Do not claim parity complete unless it is verified.
- If user-visible `.sk` behavior changes, add or update real `.sk` coverage and run the narrowest matching runtime tests; coordinator handles final `./gradlew build --rerun-tasks`.
- Import-only syntax closure may stop at parser/unit coverage, but any syntax that becomes active in the Fabric runtime through bootstrap registration or equivalent live activation must also gain representative real `.sk` GameTest coverage in the same batch.
- Parser/bootstrap-only tests are not sufficient for newly active runtime syntax.
- If a lane hits an external library or external API blocker that forces a delete, rollback, or skipped import, the worker records it in the lane status file and hands it back to the coordinator instead of silently removing it.
- Silent delete for external-library-driven blockers is forbidden.
- Coordinator must record every external-library-driven delete, rollback, or skipped import in `docs/porting/EXTERNAL_LIBRARY_GAPS.md`, including the suggested external library or alternative path.
- Work in package bundles, not one-off syntax slices.
- A lane may land multiple commits in one batch if they stay inside its owned bundle and keep moving the same closure track forward.
- Use one primary bundle and one fallback bundle inside the same ownership area, and if both still leave owned work open, continue into the next same-scope sub-bundle before declaring no-op.
- Keep pushing until one of these is true: the owned bundle is blocked, the owned bundle is exhausted, or the lane has landed at least `20` class-equivalent additions/restorations and preferably roughly `20-60`, or `2-4` verifiable commits in that batch.
- For syntax-heavy mixed-runtime cycles, the coordinator should assign at least `100` syntax-class or live-activation targets across the six workers when that much plausible missing or import-only surface still exists.
- Surface-package lanes should prefer shared base classes, abstract helpers, import-enabling scaffolding, and common runtime glue before leaf syntax classes.
- Record exact commands and exact counts in the lane status file.
- If a lane needs a file owned by another lane, stop and hand it back to the coordinator instead of freelancing into overlap.
- Before launching more workers, reuse or close stale unified exec sessions so the Codex app does not hit the 60-process pruning limit mid-batch.

## Worker Output Contract

Each worker must leave behind all of the following in its own branch/worktree:

1. code changes limited to the lane scope
2. as many upstream-backed class imports or bundle-local behavior closures as can be verified in that lane
3. test changes for the imported bundle behavior
4. if the lane makes syntax live in the runtime instead of leaving it import-only, representative real `.sk` GameTest coverage for that active syntax
5. one updated lane status file under `docs/porting/parallel/`
6. exact verification commands and results
7. a short merge note listing the files most likely to conflict
8. one or more conventional-style commits if code lands
9. enough owned-bundle progress that the lane did not stop at a trivial single-class win unless it was genuinely blocked

Workers do not update:

- `docs/porting/PORTING_STATUS.md`
- `docs/porting/NEXT_AGENT_HANDOFF.md`
- `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
- `docs/porting/EXTERNAL_LIBRARY_GAPS.md`
- `docs/porting/FABRIC_PORT_STAGES.md`
- `docs/porting/IMPLEMENTED_SYNTAX.md`
- root pointer docs

Those are coordinator-owned.

If a lane encountered an external-library or external-API blocker, its status file must
name the affected upstream class or syntax family and the attempted fallback.

## External Library Gap Ledger

- canonical file: `docs/porting/EXTERNAL_LIBRARY_GAPS.md`
- owner: `Coordinator`
- use it only for external-library or external-API blockers that forced one of these final decisions:
  - `deleted`
  - `reverted to import-only`
  - `not imported`
- each row should record:
  - `date / batch`
  - `upstream class or syntax family`
  - `local decision`
  - `missing library or API`
  - `attempted fallback / adaptation`
  - `suggested external library or alternative path`
  - `owner lane`
  - `commit / note`
  - `revisit trigger`

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
  - targeted expressions/conditions tests first
- `Lane F`:
  - targeted effects/events/entity tests first
- `Coordinator` after merge:
  - `./gradlew build --rerun-tasks`

## Merge Order

Use this merge order unless the actual diff dictates otherwise:

1. `Lane A`
2. `Lane B`
3. `Lane C`
4. `Lane D`
5. `Lane E`
6. `Lane F`
7. coordinator integration fixes
8. canonical doc update
9. final verification

Reasoning:

- `Lane A` defines parser/type registry surfaces other bundles depend on
- `Lane B` and `Lane C` mostly extend support/runtime infrastructure that should settle before deep lang and surface imports
- `Lane D` is the remaining language core and can depend on the lower bundles
- `Lane E` should merge after the dependency bundles because expressions and conditions consume earlier parser/runtime work
- `Lane F` should merge last because effects/events/entity are most likely to consume earlier surface and runtime work

## Coordinator Checklist

1. confirm each lane is on a separate worktree and branch
2. confirm each lane owns only its file scope
3. collect each lane's status file and diff summary
4. merge branches in the recommended order
5. resolve integration fallout in the coordinator worktree only
6. rerun full verification
7. update canonical docs with exact counts, exact scope, and exact test results
   coordinator must also update `docs/porting/IMPLEMENTED_SYNTAX.md` with the newly landed user-visible syntax as concrete inventory entries and representative forms, not headline-only notes
8. if the batch had any external-library-driven delete, rollback, or skipped import, update `docs/porting/EXTERNAL_LIBRARY_GAPS.md` with the final decision and suggested path forward
9. before closing the batch, confirm every newly active runtime syntax family has representative real `.sk` GameTest coverage; if that coverage is missing, add it in the coordinator pass or keep the syntax import-only instead of claiming it as active
10. refresh root pointer headlines if the headline numbers changed

## Stop Conditions

A worker should stop and return to the coordinator if any of the following happen:

- a required edit overlaps another active lane's owned file set
- the lane discovers a design decision that changes another lane's scope boundary
- the lane cannot verify a user-visible behavior change with tests or real `.sk`
- the lane wants to move syntax from import-only to active runtime but does not have representative real `.sk` GameTest coverage yet
- the lane sees unexpected modifications in files it is actively editing and cannot explain them

## Current Recommended Lane Assignment

- `Lane A`: close `classes` / `registrations` / `patterns`
- `Lane B`: close `config` / `util` / `localization`
- `Lane C`: close `variables` / `sections` / `structures` / `aliases` / `literals`
- `Lane D`: close remaining `lang` / `log` blocker imports
- `Lane E`: close larger `expressions` / `conditions` bundles
- `Lane F`: close larger `effects` / `events` / `entity` bundles
