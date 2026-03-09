# Codex Parallel Workflow

Last updated: 2026-03-09

## Goal

Run multiple Codex app sessions in parallel against this repository without file-scope collisions,
doc-count drift, or unverifiable parity claims.

This workflow is specifically for the current priority workstream:

- exact upstream user-visible syntax import on the existing Fabric-backed runtime
- one remaining mop-up lane for `lang-core`
- Stage 8 package-local Bukkit audit frozen unless explicitly reassigned

## Required Roles

- `Coordinator`: owns planning, lane assignment, merge order, canonical doc updates, and final verification
- `Lane A`: owns exact upstream `expressions` imports on existing Fabric-backed runtime paths
- `Lane B`: owns exact upstream `conditions` imports on existing Fabric-backed runtime paths
- `Lane C`: owns exact upstream `effects` imports on existing Fabric-backed runtime paths
- `Lane D`: owns selective exact upstream `events` imports plus tightly coupled event payload adapters
- `Lane E`: owns remaining `lang-core` mop-up, parser/runtime bridge parity, and upstream diff-driven reproductions that stay outside A-D ownership

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
| `Lane A` | expressions import | `src/main/java/org/skriptlang/skript/**/expressions/**`, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`, matching expression runtime tests and `.sk` resources | conditions, effects, events, canonical docs | `docs/porting/parallel/LANE_A_STATUS.md` |
| `Lane B` | conditions import | `src/main/java/org/skriptlang/skript/**/conditions/**`, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`, matching condition runtime tests and `.sk` resources | expressions, effects, events, canonical docs | `docs/porting/parallel/LANE_B_STATUS.md` |
| `Lane C` | effects import | `src/main/java/org/skriptlang/skript/**/effects/**`, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java`, matching effect runtime tests and `.sk` resources | expressions, conditions, events, canonical docs | `docs/porting/parallel/LANE_C_STATUS.md` |
| `Lane D` | events import and event payload adapters | `src/main/java/org/skriptlang/skript/fabric/syntax/event/**`, tightly coupled event payload expressions, `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`, matching event runtime tests and `.sk` resources | broader expressions, conditions, effects, canonical docs | `docs/porting/parallel/LANE_D_STATUS.md` |
| `Lane E` | lang-core mop-up and parser/runtime bridge parity | `src/main/java/ch/njol/skript/lang/**`, `src/main/java/ch/njol/skript/log/**`, `src/main/java/ch/njol/skript/variables/**`, `src/main/java/ch/njol/skript/registrations/Classes.java`, tightly matching tests | implementation-lane runtime syntax files, canonical docs | `docs/porting/parallel/LANE_E_STATUS.md` |
| `Coordinator` | merge, reconciliation, canonical docs, final verification | `docs/porting/*.md`, root pointer docs, integration fixes after merge | lane-owned feature work while workers are active | n/a |

## Shared Rules

- worker reasoning default is `medium`; raise a lane to `high` only for a confirmed hard blocker
- compare against local upstream snapshots only (`/tmp/skript-upstream-e6ec744-2`, `/tmp/upstream-skript`)
- do not browse the web for upstream comparisons
- use conventional-style commit messages without lane prefixes
- Do not edit the canonical docs under `docs/porting/*.md` from worker lanes.
- Do not edit another lane's status file.
- Do not change Stage 8 counts unless your lane actually changes that tracked matrix and the coordinator approved the reassignment.
- Do not revert unrelated dirty changes.
- Do not claim parity complete unless it is verified.
- If user-visible `.sk` behavior changes, add or update real `.sk` coverage and run the narrowest matching runtime tests; coordinator handles final `./gradlew build --rerun-tasks`.
- Implementation lanes must import exact upstream syntax forms, not approximate rewrites.
- Event lanes must stop if the backend payload or cancellation semantics are not already reproducible on Fabric.
- Record exact commands and exact counts in the lane status file.
- If a lane needs a file owned by another lane, stop and hand it back to the coordinator instead of freelancing into overlap.

## Worker Output Contract

Each worker must leave behind all of the following in its own branch/worktree:

1. code changes limited to the lane scope
2. test changes for the new behavior
3. one updated lane status file under `docs/porting/parallel/`
4. exact verification commands and results
5. a short merge note listing the files most likely to conflict
6. a conventional-style commit message if code lands, for example `fix(parser): require exact default type matches`

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
  - targeted expression runtime tests first
- `Lane B`:
  - targeted condition runtime tests first
- `Lane C`:
  - targeted effect runtime tests first
- `Lane D`:
  - targeted event runtime tests first
- `Lane E`:
  - targeted lang-core compatibility tests first
- `Coordinator` after merge:
  - `./gradlew build --rerun-tasks`

## Merge Order

Use this merge order unless the actual diff dictates otherwise:

1. `Lane D`
2. `Lane A`
3. `Lane B`
4. `Lane C`
5. `Lane E`
6. coordinator integration fixes
7. canonical doc update
8. final verification

Reasoning:

- `Lane D` can define event payload surfaces that expressions/conditions/effects consume
- `Lane A` often adds event-facing or entity-facing expressions used by later syntax slices
- `Lane B` and `Lane C` mostly stay in separate condition/effect families
- `Lane E` should merge last because it is cleanup and regression-guard work

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

- `Lane A`: import exact upstream expressions backed by existing Fabric runtime data
- `Lane B`: import exact upstream conditions backed by existing Fabric runtime data
- `Lane C`: import exact upstream effects backed by existing Fabric runtime data
- `Lane D`: import exact upstream events for existing Fabric-backed event families and their payload adapters
- `Lane E`: keep closing remaining `lang-core` and parser/runtime bridge long-tail mismatches
