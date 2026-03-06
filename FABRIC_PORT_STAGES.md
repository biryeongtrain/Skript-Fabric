# Fabric Port Stages

Last updated: 2026-03-06

## Goal

Port this Bukkit/Paper-based Skript codebase to Fabric while preserving behavior.

This does **not** mean "make it compile by deleting features".
It means:

- remove Bukkit/Paper runtime dependencies from the active Fabric source path
- map Bukkit-facing concepts to Mojang/Fabric equivalents
- preserve the role of existing syntax classes
- validate behavior by executing real `.sk` files through Fabric GameTest

## Current measured state

- `src/main/java/org/skriptlang/skript/bukkit`: 218 classes
- direct `org.bukkit` / Paper references in `src/main/java`: 3657 hits
- clean `./gradlew compileJava --rerun-tasks`: fails immediately on missing `org.bukkit.*`

## Execution policy

Stages are executed in order.
Each stage must leave the repository in a verifiable state before the next stage starts.

## Stage 1: Restore Fabric baseline

Objective:

- remove the currently active broken Bukkit-heavy source tree from the main build path
- restore the known-good Fabric baseline from the sibling `../Skript-Fabric` repository
- keep this repository identity (`Skript-Fabric-port`) in metadata

Deliverables:

- active `src` matches the compilable Fabric baseline
- build metadata points at this repository id/name
- clean `./gradlew compileJava` or `./gradlew build` passes

Acceptance:

- no active `org.bukkit.*` compile errors remain

Status: `completed`

## Stage 2: Add executable script runtime harness

Objective:

- add a real script file loader for `.sk` files into the Fabric baseline
- add a runtime registry bootstrap path so syntax modules can register themselves
- ensure triggers can execute against a Mojang-backed event context

Deliverables:

- load script files from disk/resources into runtime objects
- bootstrap method invoked during mod initialization and tests
- first real trigger execution path from loaded script to effect execution

Acceptance:

- a minimal script file can be loaded and executed in-process

Status: `completed`

## Stage 3: Establish core Bukkit-to-Mojang type mappings

Objective:

- define the foundational replacements for Bukkit-facing core types

Initial mapping targets:

- `Player` -> `ServerPlayer`
- `Entity` -> `net.minecraft.world.entity.Entity`
- `World` -> `ServerLevel`
- `Location` -> Mojang position wrapper or project-owned adapter over `ServerLevel` + `Vec3` / `BlockPos`
- `Block` -> project-owned adapter over `ServerLevel` + `BlockPos` + `BlockState`
- `ItemStack` -> `net.minecraft.world.item.ItemStack`
- `Inventory` -> project-owned adapter over `Container`
- `Vector` -> `Vec3`

Deliverables:

- project-owned compatibility/adaptation layer
- parsers / class infos for the mapped core types
- replacement event context accessors

Acceptance:

- mapped core types are usable from syntax parsing and runtime execution

Status: `completed`

## Stage 4: Port base type/info layer

Objective:

- port `org/skriptlang/skript/bukkit/base/types`
- keep class roles intact while swapping runtime backing to Mojang/Fabric-compatible types

Deliverables:

- Fabric equivalents for all class info registrations under the base type package

Acceptance:

- type parsing and stringification work under GameTests

Status: `completed`

## Stage 5: Port event backend

Objective:

- replace Bukkit event classes and listeners with Fabric/Mojang event hooks
- preserve event semantics as closely as practical

Deliverables:

- Fabric event bridge
- per-event mapping matrix documenting Bukkit source event -> Fabric/Mojang trigger
- executable event registration path

Acceptance:

- representative event scripts fire through GameTests

Status: `in_progress`

## Stage 6: Port conditions, expressions, effects package-by-package

Objective:

- port all syntax classes without deleting them or changing their purpose

Priority order:

1. `org/skriptlang/skript/bukkit/base`
2. `org/skriptlang/skript/bukkit/entity`
3. `org/skriptlang/skript/bukkit/potion`
4. `org/skriptlang/skript/bukkit/damagesource`
5. `org/skriptlang/skript/bukkit/displays`
6. `org/skriptlang/skript/bukkit/particles`
7. `org/skriptlang/skript/bukkit/loottables`
8. `org/skriptlang/skript/bukkit/furnace`
9. `org/skriptlang/skript/bukkit/brewing`
10. `org/skriptlang/skript/bukkit/breeding`
11. `org/skriptlang/skript/bukkit/fishing`
12. `org/skriptlang/skript/bukkit/interactions`
13. `org/skriptlang/skript/bukkit/input`
14. `org/skriptlang/skript/bukkit/itemcomponents`
15. `org/skriptlang/skript/bukkit/tags`
16. `org/skriptlang/skript/bukkit/misc`

Deliverables:

- package remains present
- class names and roles remain present
- implementation backed by Mojang/Fabric, not Bukkit

Acceptance:

- each package has dedicated runtime tests and GameTests

Status: `pending`

## Stage 7: Fabric GameTest verification suite

Objective:

- validate with real `.sk` files, not just unit tests

Deliverables:

- `fabric-gametest` entrypoint
- test world fixtures
- `.sk` script fixtures per package
- assertions on actual world/entity/item state

Acceptance:

- GameTests load scripts, execute them, and verify state transitions

Status: `pending`

## Stage 8: Parity audit

Objective:

- compare active Fabric implementation against the original Bukkit behavior

Deliverables:

- coverage matrix for every class under `org/skriptlang/skript/bukkit`
- list of exact parity gaps, if any

Acceptance:

- no silent omissions

Status: `pending`

## Notes

- “100% complete” is only true when Stage 8 is satisfied.
- Until then, each stage should report exact completed scope and exact remaining scope.

## Verification Log

- 2026-03-06: `./gradlew build` passed after Stage 1 baseline restore.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed with a real `.sk` fixture that executed `on gametest` and changed world state.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after adding initial Mojang-backed core type adapters and `ClassInfo` registrations for player, world, entity, location, block, item stack, inventory, and vector.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed with `%location%` consumed directly by script syntax, proving mapped types are usable from parse-time through runtime execution.
- 2026-03-06: `./gradlew compileJava compileGametestJava --rerun-tasks` passed after restoring all `org/skriptlang/skript/bukkit/base/types` class info files and adding Mojang-backed replacements for missing type wrappers.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `ServerTickEvents.END_SERVER_TICK` into the Skript runtime and executing an `on server tick` script without manual dispatch.
- 2026-03-06: `./gradlew build` passed after serializing GameTest access to the shared `SkriptRuntime`, removing cross-test flakiness from event-bridge verification.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `PlayerBlockBreakEvents.AFTER` into the Skript runtime and executing a real `.sk` file from a mock player block break.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after restoring modern expression registration, exposing `event-block` / `event-player`, and consuming both inside real `.sk` fixtures.
- 2026-03-06: `./gradlew build` passed after wiring `UseBlockCallback.EVENT` into the Skript runtime and validating `on use block` through GameTests.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `UseEntityCallback.EVENT`, exposing `event-entity`, and mutating a real `ArmorStand` through a loaded `.sk` file.
- 2026-03-06: `./gradlew build` passed with 7 Fabric GameTests, including automatic server-tick and block-break bridge coverage.

## Stage 3 Completed Scope

- Mojang-backed adapters added for location, block, inventory, and item type
- `ClassInfo` registrations added for player, world, entity, location, block, item stack, inventory, item type, nameable, offline player, slot, and vector
- property handlers wired for `name`, `display name`, `contains`, `amount`, `is empty`, and `wxyz`
- `%location%` now parses in scripts and is consumed by a real effect path that mutates world state under Fabric GameTest

Remaining parity work after Stage 3:

- richer non-test-world location/world resolution semantics
- broader event-scoped accessors exposed as reusable syntax
- package-level syntax parity outside the core mapped types

## Stage 4 Completed Scope

- every tracked class under `org/skriptlang/skript/bukkit/base/types` has an active Fabric/Mojang-backed counterpart in the source tree
- missing type wrappers reintroduced for item type, nameable, offline player, slot, and world
- GameTests cover parsers and property handlers across the restored base type layer

Remaining parity work after Stage 4:

- behavior expansion from type registration into the higher-level condition/expression/effect packages

## Stage 5 Current Scope

Completed in this slice:

- first automatic Fabric event bridge added through `ServerTickEvents.END_SERVER_TICK`
- new `on server tick` syntax added and routed into runtime dispatch
- GameTest coverage added for event-driven `.sk` execution without manual dispatch
- initial event mapping matrix documented in `FABRIC_EVENT_MAPPING.md`
- `PlayerBlockBreakEvents.AFTER` now bridges into the runtime through a Mojang-backed block break handle
- GameTest coverage added for mock-player block breaking that executes a real `.sk` file

Still missing before Stage 5 can be called complete:

- Bukkit event by event mapping onto Mojang/Fabric hooks
- event payload parity for player/entity/block/inventory contexts
- representative mapped events beyond lifecycle tick and block break hooks
