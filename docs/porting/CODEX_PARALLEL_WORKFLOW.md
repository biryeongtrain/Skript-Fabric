# Codex Parallel Workflow

Last updated: 2026-03-09

## Goal

Run multiple Codex app sessions in parallel against this repository without file-scope collisions,
doc-count drift, or unverifiable parity claims.

This workflow is specifically for the current priority workstream:

- upstream `ch/njol/skript` closure first
- Stage 8 package-local Bukkit audit frozen unless explicitly reassigned

## Required Roles

- `Coordinator`: owns planning, lane assignment, merge order, canonical doc updates, and final verification
- `Lane A`: owns `Statement` / `ScriptLoader` / parse-log orchestration
- `Lane B`: owns `SkriptParser` / `patterns` / parser tag-mark parity
- `Lane C`: owns `Variables` / `Classes` / `config` / `structures`
- `Lane D`: owns `lang/function` runtime and default-parameter parity
- `Lane E`: owns parser/runtime bridge triage, `InputSource` / `ParserInstance` parity, and upstream diff-driven reproductions that stay outside A-D ownership

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
| `Lane A` | statement loading, script loading, parse-log flow | `src/main/java/ch/njol/skript/lang/Statement.java`, `src/main/java/ch/njol/skript/ScriptLoader.java`, `src/main/java/ch/njol/skript/log/**`, `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java` | `SkriptParser.java`, `Variables.java`, `Classes.java`, canonical docs | `docs/porting/parallel/LANE_A_STATUS.md` |
| `Lane B` | parser flow, marks/tags, pattern handling | `src/main/java/ch/njol/skript/lang/SkriptParser.java`, `src/main/java/ch/njol/skript/patterns/**`, parser-facing tests | `Statement.java`, `ScriptLoader.java`, `Variables.java`, canonical docs | `docs/porting/parallel/LANE_B_STATUS.md` |
| `Lane C` | variables, classes, config, structures | `src/main/java/ch/njol/skript/variables/**`, `src/main/java/ch/njol/skript/registrations/Classes.java`, `src/main/java/ch/njol/skript/config/**`, `src/main/java/ch/njol/skript/structures/**`, matching tests | `Statement.java`, `SkriptParser.java`, canonical docs | `docs/porting/parallel/LANE_C_STATUS.md` |
| `Lane D` | function runtime, overload/default-parameter parity | `src/main/java/ch/njol/skript/lang/function/**`, `src/main/java/ch/njol/skript/lang/DefaultExpression.java`, `src/main/java/ch/njol/skript/lang/DefaultExpressionUtils.java`, `src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java`, matching function/parser-default tests | `SkriptParser.java`, `Statement.java`, `ScriptLoader.java`, `Variables.java`, canonical docs | `docs/porting/parallel/LANE_D_STATUS.md` |
| `Lane E` | parser/runtime bridges and upstream diff-driven reproductions | `src/main/java/ch/njol/skript/lang/InputSource.java`, `src/main/java/ch/njol/skript/lang/parser/ParserInstance.java`, `src/main/java/ch/njol/skript/expressions/ExprInput.java`, `src/main/java/ch/njol/skript/lang/TriggerItem.java`, `src/main/java/ch/njol/skript/lang/TriggerSection.java`, tightly matching tests | `Statement.java`, `SkriptParser.java`, `Variables.java`, `Classes.java`, canonical docs | `docs/porting/parallel/LANE_E_STATUS.md` |
| `Coordinator` | merge, reconciliation, canonical docs, final verification | `docs/porting/*.md`, root pointer docs, integration fixes after merge | lane-owned feature work while workers are active | n/a |

## Shared Rules

- Do not edit the canonical docs under `docs/porting/*.md` from worker lanes.
- Do not edit another lane's status file.
- Do not change Stage 8 counts unless your lane actually changes that tracked matrix and the coordinator approved the reassignment.
- Do not revert unrelated dirty changes.
- Do not claim parity complete unless it is verified.
- If user-visible `.sk` behavior changes, add or update real `.sk` coverage and run GameTest coverage appropriate to the change.
- Record exact commands and exact counts in the lane status file.
- If a lane needs a file owned by another lane, stop and hand it back to the coordinator instead of freelancing into overlap.

## Worker Output Contract

Each worker must leave behind all of the following in its own branch/worktree:

1. code changes limited to the lane scope
2. test changes for the new behavior
3. one updated lane status file under `docs/porting/parallel/`
4. exact verification commands and results
5. a short merge note listing the files most likely to conflict

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
  - targeted unit tests first
  - run `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - if user-visible `.sk` semantics changed, also run `./gradlew runGameTest --rerun-tasks`
- `Lane B`:
  - targeted parser tests first
  - run parser-focused `./gradlew test --tests ... --rerun-tasks`
  - if live `.sk` parsing changed, also run `./gradlew runGameTest --rerun-tasks`
- `Lane C`:
  - targeted unit tests for `variables`, `classes`, `config`, `structures`
  - if `options:` or other live `.sk` structure behavior changed, also run `./gradlew runGameTest --rerun-tasks`
- `Lane D`:
  - targeted function/default-parameter tests first
  - run `./gradlew test --tests 'ch.njol.skript.lang.function.*' --rerun-tasks`
  - if omitted/default-expression behavior changes in live `.sk`, also run `./gradlew runGameTest --rerun-tasks`
- `Lane E`:
  - targeted bridge/runtime tests first
  - run the narrowest matching parser/input/runtime tests
  - if live `.sk` input/bridge behavior changes, also run `./gradlew runGameTest --rerun-tasks`
- `Coordinator` after merge:
  - `./gradlew runGameTest --rerun-tasks`
  - `./gradlew build --rerun-tasks`

## Merge Order

Use this merge order unless the actual diff dictates otherwise:

1. `Lane C`
2. `Lane B`
3. `Lane D`
4. `Lane E`
5. `Lane A`
6. coordinator integration fixes
7. canonical doc update
8. final verification

Reasoning:

- `Lane C` tends to touch lower-level config / structure helpers
- `Lane B` tends to sit above that in parser flow
- `Lane D` depends on parser/class behavior but normally avoids statement/loading overlap
- `Lane E` is intentionally narrow and should merge after parser/class/function slices have settled
- `Lane A` tends to consume parser, function, and config behavior in final statement/loading orchestration

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

- `Lane A`: continue `Part 1A` on `Statement` / `ScriptLoader` orchestration and loader hint flow
- `Lane B`: continue `Part 1A` on richer `SkriptParser` tag / mark / pattern parity
- `Lane C`: continue `Part 1B` on `Variables`, `Classes`, and the remaining `config` / `structures` behavior gaps
- `Lane D`: continue `Part 1A` on `lang/function` runtime, overload, and default-parameter parity
- `Lane E`: continue upstream diff-driven reproductions in parser/runtime bridge files that stay outside A-D ownership
