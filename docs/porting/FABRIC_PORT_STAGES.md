# Fabric Port Stages

Last updated: 2026-03-08

## Goal

Port this Bukkit/Paper-based Skript codebase to Fabric while preserving behavior.

This does **not** mean "make it compile by deleting features".
It means:

- remove Bukkit/Paper runtime dependencies from the active Fabric source path
- map Bukkit-facing concepts to Mojang/Fabric equivalents
- preserve the role of existing syntax classes
- validate behavior by executing real `.sk` files through Fabric GameTest

## Current measured state

- `src/main/java/org/skriptlang/skript/bukkit`: 199 classes
- direct `org.bukkit` / Paper references in `src/main/java`: 0 hits
- clean `./gradlew compileJava --rerun-tasks`: passes

## Execution policy

Stages are executed in order.
Each stage must leave the repository in a verifiable state before the next stage starts.

## Parallel Core Workstream

The next immediate priority is not another Stage 8 package-local Bukkit slice.
The user explicitly reprioritized the broader upstream `ch/njol/skript` surface first.

Current measured baseline for that workstream:

- upstream `ch/njol/skript` snapshot `e6ec744`: `1189` Java files
- local `ch/njol/skript`: `128` Java files
- detailed matrix and part tracker: [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- active implementation slices: `Part 2: missing user-visible syntax import`, `Part 1A: lang parser/runtime closure`, `Part 1B: dependency closure`
- currently landed core slices:
  - `ExprInput` current-value, typed `%classinfo% input`, and `input index` parsing in active `InputSource` context
  - `SkriptParser` now supports minimal raw-regex captures in registered syntax patterns like `if <.+>`, plus the minimal leading `implicit:` tag required by registered conditional sections
  - `SkriptParser` now also preserves required whitespace around omitted inline optional groups and inline alternation branches for the currently verified natural-script compatibility surface
  - `SecIf` now executes chained `if / else if / else` sections in real `.sk` files and loads through the registered `Section` path instead of a dedicated `Statement` fallback
  - `SecIf` now supports `parse if` / `else parse if`, skips body loading when parse-time conditions fail, and is covered by real `.sk` fixtures including invalid skipped bodies
  - `SecIf` now supports multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run`
  - `SecIf` now supports implicit conditional sections such as `%condition%:`
  - `Condition.parse(...)` now unwraps grouped outer parentheses for the current real-script `if` path
  - `options:` replacement is live through `EntryNode`, `StructOptions`, `ScriptLoader`, and the runtime script loader
  - `Node.remove()`, local `NodeMap`, and expanded `SectionNode` behavior now provide the case-insensitive lookup, ordered replace/remove, parent-reassignment, and entry-conversion map-sync semantics the config layer expects
  - `Skript.registerStructure(...)` now accepts `EntryValidator`, and `KeyValueEntryData` now accepts both `SimpleNode` and `EntryNode` inputs on the compatibility path
  - `StructOptions` now loads through a recursive validator-backed entry path, so runtime `EntryNode` trees and raw simple `key: value` nodes both work without losing `Invalid line in options` diagnostics
  - `ScriptLoader.loadItems(...)` now routes section nodes through registered `Section` parsing before statement fallback
  - `Classes.parse(...)` now clears stale direct-parser failures before later parser or converter fallback success, so successful fallback does not leak earlier parser diagnostics
  - `SkriptParser.ParseResult.tags` and the shared matcher now preserve duplicate parse tags in encounter order
  - `Statement.parse(...)` now lets a later same-pattern plain statement registration win after earlier effect/condition init failures, while restoring the best prior specific error if no statement ultimately matches
  - real base `.sk` coverage now also proves statement fallback after failed effect parse through `runtime.loadFromResource(...)`
  - `Statement.parse(...)` now routes effect parsing through `Effect.parse(...)`, and `Effect.parse(...)` now allows plain effects with section-managing expressions to own their section body instead of dropping it
  - `ScriptLoader` section-node fallback now restores the better retained section-versus-statement diagnostic, plain conditions no longer silently parse as section headers, stopping statements now emit unreachable-code warnings behind script-level warning suppression, and nested section-contained stop-trigger intent now propagates through loader/runtime while `stopSection` stays local
  - the shared compiled matcher now lives in `ch/njol/skript/patterns`, so `SkriptParser` and direct pattern compilation now share parse-tag/mark handling, including the current bare leading `:` auto-tag derivation surface and omitted optional/alternation raw-regex capture handling
  - `PatternCompiler` now also builds a lightweight `PatternElement` graph, and `SkriptPattern` now exposes `countTypes()`, `countNonNullTypes()`, and `getElements(...)`
  - `ParserInstance` now owns an upstream-style `HintManager`, and `Variable.newInstance(...)` now narrows simple local variables from known hints during parse-time local variable resolution
  - `ScriptLoader.loadItems(...)` and `parseSectionTriggerItem(...)` now manage section and temporary non-section hint scopes, so failed section parses roll back temporary hints while successful section loads can propagate, freeze, or merge hints through the active stop-flow path
  - `Classes.parse(...)` now falls back through registered converters after direct parser lookup, so converter-backed source types can satisfy requested class infos and unparsed-literal conversion paths
  - `PatternCompiler` now preserves placeholder-local `*` / `~`, leading optional markers, plural metadata, and `@time`, `TypePatternElement` now exposes that metadata, and `SkriptPattern` now applies placeholder-local parse flags plus `@time` through the shared matcher while leaving plurality metadata non-enforcing on the current green corpus
  - `Statement.parse(...)` now clears inherited outer section ownership on plain statement parses, so nested function/effect/condition arguments do not accidentally inherit an enclosing expression section; the GameTest suite now covers the equivalent outer-expression-section plain-effect path
  - `Variables` now uses natural numeric ordering for prefix/list iteration, and the base GameTest suite now verifies that `{source::2}` sorts ahead of `{source::10}`
  - list variables now also expose the legacy loop aliases `var`, `variable`, and `value`, and restore upstream all-values predicate-check semantics through `Variable.getAnd()` / `check(...)`
  - `ClassInfo` / `Classes` now close codename, literal-pattern, supertype-lookup, class-info ordering, and shared literal-match ordering behavior used by parser/runtime compatibility paths
  - variable expressions and case-insensitive variable storage now work
  - `Variables.withLocalVariables(...)` now copies nested section-event local-variable mutations back to the provider scope
  - keyed list-to-list `set` now reindexes source entries into numeric target slots instead of preserving source keys
  - `EffChange` now only forwards keyed deltas when the source expression explicitly recommends them
  - quoted string literals remain string literals in generic `%object%` contexts during live `.sk` loading
  - `VariableString` now routes `StringMode.MESSAGE` through Patbox `TextPlaceholderAPI`, and `TriggerItem.walk(...)` now scopes the active event through `CurrentSkriptEvent`, so exact `%namespace:path%` placeholders resolve on live message/name paths
  - `Variable.isValidVariableName(...)` now ignores `*` inside paired `%...%` spans, restoring dynamic variable-name forms such as `result::%{source::*}%`
  - `ClassInfo` now exposes default expressions, and `SkriptParser` now falls back to them for omitted non-optional placeholders when parser-scoped defaults are absent
  - `EffChange.init(...)` now publishes parse-time local-variable hints for the exact built-in `set %object% to %object%` path when it successfully targets a hintable local variable
  - base entity-state conditions now cover `%entities% are alive/dead`, `%entities% are silent`, and `%entities% are invulnerable/invincible`
  - base entity-control effects now cover `kill %entities%`, `silence %entities%`, `unsilence %entities%`, `make %entities% silent`, and `make %entities% (invulnerable|invincible|vulnerable)`
  - dedicated unit tests plus real `.sk` GameTests now verify those exact syntax families through the active Fabric bootstrap and live runtime
  - `Variables.getVariable("name::*", ...)` now reconstructs upstream-style nested list maps from the flat store, while `getVariablesWithPrefix(...)` keeps the current shallow direct-child behavior
  - `SkriptParser` now recognizes upstream-prefixed variable forms such as `var {x}`, `variable {x}`, and `the variable {x}`
  - `Statement.selectRetainedFailure(...)` now keeps earlier higher-quality effect/condition parse errors over later lower-quality plain-statement failures on the same syntax line
  - section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed local values instead of assuming typed runtime arrays
  - `Node.splitLine(...)` now strips inline comments, preserves quoted `#`, unescapes doubled `##`, and tracks `###` block comments for the active runtime parser
  - `SkriptRuntime.parseScript(...)` now uses that split logic, so trailing comments on section headers, option entries, conditions, and effects no longer break live `.sk` loading
  - `ParseLogHandler`, `SkriptLogger`, and `Statement.parse(...)` now retain specific parse errors across nested parser scopes, so valid effects used as sections keep their ownership diagnostic instead of falling through to a generic `Can't understand this section` fallback
  - locked runtime GameTests now clear Skript variables before and after each body through `Variables.clearAll()`, which keeps real `.sk` verification isolated from suite-order leakage without changing production variable semantics
  - targeted unit verification passed on 2026-03-08
  - `./gradlew runGameTest --rerun-tasks` passed on 2026-03-08 with `216 / 216`
  - `./gradlew build --rerun-tasks` passed on 2026-03-08, including the full Fabric GameTest task

This workstream runs in parallel with the Stage 5 and Stage 8 records below.
Do not treat it as satisfied until the dedicated audit document says so.

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

Current completed slices:

- base condition seed: `is empty`, `is named`
- breeding condition batch: `can age`, `can breed`, `is adult`, `is baby`, `is in love`
- brewing complete event batch: `on brewing complete`, including item/potion-effect filter forms
- brewing start event batch: `on brewing start`
- damage source condition batch: `scales damage with difficulty`, `was indirectly caused`
- brewing / fishing / input condition batch: `brewing stand will consume fuel`, `lure enchantment bonus is applied`, `entity is in open water`, `player is pressing key`
- breeding event batch: `on breeding`
- bucket catch event batch: `on bucket catch`
- love mode enter event batch: `on love mode enter`
- entity potion effect event batch: `on entity potion effect [modification]`
- furnace event batch: `on fuel burn`, `on smelting start`, `on furnace smelt`, `on furnace extract`
- loot generate event batch: `on loot generate`

Known remaining Stage 5 gaps:

- missing event families: none in the currently tracked package-local Bukkit surface
- partial parity in existing families: none in the current Fabric target surface
- intentionally dropped deprecated residue:
  - `PATROL_CAPTAIN` potion cause

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

Status: `in_progress`

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

Status: `in_progress`

Current completed slices:

- `fabric-gametest` runtime harness is active
- current real-script Fabric GameTest suite is green at `216 / 216`

## Stage 8: Parity audit

Objective:

- compare active Fabric implementation against the original Bukkit behavior

Deliverables:

- coverage matrix for every class under `org/skriptlang/skript/bukkit`
- list of exact parity gaps, if any

Acceptance:

- no silent omissions

Status: `in_progress`

Current audited Stage 8 slice:

- natural-script event/filter forms that depend on registry-backed plain-token parsing are now rechecked live for breeding, bucket catch, brewing complete, brewing fuel, furnace filtered event forms, and potion-effect id filters
- timed event payload parsing now accepts both `past ...` and `future ...` forms in the active runtime
- section-backed control flow now includes minimal `if <condition>:` support so live `.sk` parity fixtures can express original guarded event logic instead of flattening branches into synthetic test-only syntax
- `145c3c9` package-local audit matrix is now written for `23 / 214` classes (`10.7%`) across `breeding (12 / 12)`, `input (5 / 5)`, and `interactions (6 / 6)`
- `breeding` is rechecked live against the original module/event-value role, including breeding `event-item` presence in real `.sk` and direct handle/expression verification that the captured bred-with item resolves as wheat
- `input` is rechecked live against the original module/event-value role, including `past current input keys of event-player`
- `interactions` is rechecked live against the original module role, including original natural date/player forms such as `last time %entity% was interacted with`

Current audited package-local matrix:

| Package | `145c3c9` package-local classes | Status | Notes |
| --- | --- | --- | --- |
| `breeding` | `12 / 12` | package-local parity-complete | module, conditions, effects, event syntax, and expressions are covered; `EntityBreedEvent#getBredWith` parity is verified through real `.sk` event-item presence plus direct event-handle/expression checks |
| `input` | `5 / 5` | package-local parity-complete | enum, module, event syntax, condition, and expression are covered, including past-state input values |
| `interactions` | `6 / 6` | package-local parity-complete | module, condition, effect, dimensions expression, last-player expressions, and original last-date expressions are covered |

Still remaining before Stage 8 can be called complete:

- package-local coverage matrix progress is `23 / 214`, so `191 / 214` package-local classes still need audit
- top-level non-package Bukkit helpers outside that package-local matrix still remaining: `4`
- a cross-cutting base-surface gap remains outside the three completed packages: ambiguous bare item-id equality through generic compare is not parity-complete yet, for example `event-item is wheat`
- further audit slices still need to confirm parser/runtime behavior row-by-row outside the three completed packages and the earlier natural-script event/filter surface

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
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed after wiring `Animal.finalizeSpawnChildFromBreeding` into the Skript runtime and executing a real `.sk` breeding event script against live cow entities.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed at `181 / 181` after the `breeding` / `input` / `interactions` Stage 8 audit slice, including live coverage for breeding `event-item`, past current input keys, and original interaction player/date forms.
- 2026-03-07: `./gradlew build --rerun-tasks` passed after the same Stage 8 audit slice.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed after wiring `BrewingStandBlockEntity.serverTick` successful brew completion into the Skript runtime and executing a real `.sk` brewing complete script that cleared brewed results.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after porting the original `org/skriptlang/skript/bukkit/breeding/elements` condition batch and validating real `.sk` fixtures against Mojang cows for adult, baby, breed-ready, can-age, and in-love states.
- 2026-03-06: `./gradlew build` passed with `19` Fabric GameTests green after the breeding condition batch landed.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after restoring modern expression registration, exposing `event-block` / `event-player`, and consuming both inside real `.sk` fixtures.
- 2026-03-06: `./gradlew build` passed after wiring `UseBlockCallback.EVENT` into the Skript runtime and validating `on use block` through GameTests.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `UseEntityCallback.EVENT`, exposing `event-entity`, and mutating a real `ArmorStand` through a loaded `.sk` file.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `UseItemCallback.EVENT`, exposing `event-item`, and mutating a real held `ItemStack` through a loaded `.sk` file.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed after wiring `AttackEntityCallback.EVENT` and executing a real `.sk` file from an attack-entity callback with live entity/player payloads.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after moving shared base expressions/effects from `fabric/syntax` into `org/skriptlang/skript/bukkit/base`, starting Stage 6 with a package-local syntax layer.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after adding the first real base conditions (`is empty`, `is named`) and validating both true and false trigger flow through loaded `.sk` files.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after wiring `ServerLivingEntityEvents.ALLOW_DAMAGE`, exposing `event-damage source`, and validating the first `damagesource` condition batch through real `.sk` fixtures.
- 2026-03-06: all original Bukkit `Cond*.java` classes from commit `145c3c9` are now present in the active Fabric source tree: `28 / 28`, remaining source-level condition ports `0`.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed with `41` Fabric GameTests green after converting the latest condition fixtures to the runtime-supported plain-condition form.
- 2026-03-06: `./gradlew build` passed after restoring `InputSource.parseExpression()` compatibility while keeping `%objects%` plain-string parsing available for script syntax.
- 2026-03-06: `./gradlew build` passed with 7 Fabric GameTests, including automatic server-tick and block-break bridge coverage.
- 2026-03-06: the current original Bukkit `Expr*.java` slice is active in the Fabric source tree at `23 / 84`, remaining source-level expression ports `61`.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed with `45` Fabric GameTests green after adding `ExprBrewingSlot`, `ExprFishingHook`, `ExprFishingHookEntity`, and `ExprCurrentInputKeys` plus real `.sk` fixtures that consume them.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` passed with `51` Fabric GameTests green after adding damage-source payload expressions (`causing entity`, `direct entity`, `source location`, `damage location`, `food exhaustion`) plus `ExprBrewingFuelLevel` and validating them through loaded `.sk` files.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed with `55` Fabric GameTests green after restoring text-display expressions for `text alignment`, `line width`, and `text opacity`, plus a minimal generic comparison condition used to validate them from real `.sk` files.
- 2026-03-06: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed with `61` Fabric GameTests green after restoring generic display expressions for `billboard`, `brightness override`, `display height/width`, `shadow radius/strength`, and `view range`, including changer coverage against Mojang `Display` state.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `65` Fabric GameTests green after restoring `ExprDisplayInterpolation` and `ExprDisplayTeleportDuration`, plus a generic change effect path that executes `set/add/remove/reset/delete` against change-capable expressions from real `.sk` files.
- 2026-03-07: `./gradlew build` passed after validating that display interpolation and teleport-duration expressions mutate live Mojang `Display` state under Fabric GameTest.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after restoring `ExprLoveTime`, `ExprInteractionDimensions`, and `ExprLastInteractionPlayer`, plus interaction-state tracking for attack/use callbacks and real `.sk` fixtures that consume those expressions.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after restoring `ExprDisplayTransformationRotation`, `ExprDisplayTransformationScaleTranslation`, and `ExprItemDisplayTransform`, adding `Quaternionf` / `ItemDisplayContext` type parsing, and validating both direct changer behavior and real `.sk` comparison paths against live Mojang `Display` state.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after restoring `ExprLootTable`, `ExprLootTableFromString`, and `ExprLootTableSeed`, tightening parser precedence around `loot table seed of ...`, and validating both entity/block loot-table setters through live `.sk` fixtures.
- 2026-03-07: the current original Bukkit `Expr*.java` slice is active in the Fabric source tree at `38 / 84`, remaining source-level expression ports `46`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `80` Fabric GameTests green after the latest interaction and display expression batches.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `84` Fabric GameTests green after the loottable expression batch landed cleanly.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after restoring potion expressions for `active potion effects`, specific `potion effect`, `potion duration`, `potion amplifier`, and `potion effect type category`, including bare-token handling when Skript parses effect ids as offline-player names.
- 2026-03-07: the current original Bukkit `Expr*.java` slice is active in the Fabric source tree at `43 / 84`, remaining source-level expression ports `41`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `90` Fabric GameTests green after the potion expression batch landed cleanly.
- 2026-03-07: `./gradlew compileJava compileGametestJava --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build` all passed after registering the remaining expression/event batches and closing the expression port at source level.
- 2026-03-07: the original Bukkit `Expr*.java` class list from commit `145c3c9` is now source-complete in the active Fabric tree at `84 / 84`, remaining source-level expression ports `0`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build` passed after fixing equippable effect fixture syntax to target direct object holders, broadening loot-generation assertions to valid generated loot instead of a single fixed item, and closing the remaining effect verification failures.
- 2026-03-07: `./gradlew compileJava --rerun-tasks` passed with active `org.bukkit` / Paper direct references reduced to `0` in `src/main/java`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `120 / 120` Fabric GameTests green after wiring the furnace event family (`on fuel burn`, `on smelting start`, `on furnace smelt`, `on furnace extract`) and fixing furnace-time parser registration so plain `cook time` / `cooking time` forms both bind correctly.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `127 / 127` Fabric GameTests green after wiring `on loot generate` through the live loot-table return path and verifying filtered furnace event forms across burn/start/smelt/extract with real `.sk` fixtures.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `128 / 128` Fabric GameTests green after wiring `BrewingStandBlockEntity.serverTick` start-brew flow into `on brewing start` and verifying same-tick `brewing time` mutation from a real `.sk` fixture.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `130 / 130` Fabric GameTests green after fixing the live bucket-catch `.sk` fixture, verifying `event-player` resolution, and adding real filter coverage for `on bucket catching of pufferfish`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `134 / 134` Fabric GameTests green after wiring `on entity potion effect [modification]` through live add/update/remove callbacks; action-header dispatch is verified, but type-filter and event-payload parity still remain open.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `135 / 135` Fabric GameTests green after wiring `on love mode enter` through `Animal` love-mode transitions and verifying `event-entity` / `event-player` from a live breeding-item interaction.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `136 / 136` Fabric GameTests green after teaching `SkriptParser` to resolve `past ...` event payload expressions, verifying current/past potion payload access from real `.sk` headers, and narrowing the remaining potion-event gap to `due to %-potioncauses%`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `140 / 140` Fabric GameTests green after restoring breeding offspring-type filters and brewing fuel item filters, then validating both positive and negative paths from live breeding and brewing-stand callbacks.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `142 / 142` Fabric GameTests green after restoring brewing complete item and potion-effect filter forms, then validating both positive and negative filter paths from live brewing completion callbacks.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` passed with `145 / 145` Fabric GameTests green after restoring `on player input` plain-form compatibility and adding live toggle/press/release + key-filter coverage through `ServerGamePacketListenerImpl.handlePlayerInput`, reducing the remaining Stage 5 gaps to `2`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `149 / 149` Fabric GameTests green after restoring original `on fishing` state variants through live `FishingHook` lifecycle hooks and validating cast, state-change, entity-hook, in-ground, lured, bite, escape, caught-fish, and reel-in flows from real `.sk` fixtures, reducing the remaining Stage 5 gaps to `1`.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `152 / 152` Fabric GameTests green after restoring initial `on entity potion effect ... due to %potioncauses%` filtering, validating live `potion drink` and `area effect cloud` cause paths plus a non-matching negative filter, while keeping the broader potion-cause family partial.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `158 / 158` Fabric GameTests green after extending `on entity potion effect ... due to %potioncauses%` through live consumable, beacon, conduit, and `/effect` command paths; `food`, `milk`, `beacon`, `conduit`, and `command` are now verified alongside `potion drink` and `area effect cloud`, but the family remains partial at `7 / 25` original Bukkit cause values.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `162 / 162` Fabric GameTests green after restoring typed `potioncauses` parsing, fixing consumable cause-context leaks across mutable item consumption, and validating live `attack`, `arrow`, `unknown`, and `potion splash` cause paths; the potion-cause family remains partial at `11 / 25` original Bukkit cause values.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `175 / 175` Fabric GameTests green after tagging Skript-driven potion mutations as `plugin`, dispatching death-driven potion removal as `death`, and validating live `dolphin`, `turtle helmet`, `illusion`, `plugin`, and `death` cause paths; `on entity potion effect ... due to %potioncauses%` now has `24 / 25` original cause values live-verified, with only deprecated-unused `patrol captain` still unresolved.
- 2026-03-07: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `176 / 176` Fabric GameTests green after removing deprecated-unused `PATROL_CAPTAIN` from the supported potion-cause surface, switching potion-effect type matching to registry-safe comparison, and adding real `.sk` coverage for explicit namespaced effect ids such as `minecraft:poison`; tracked Stage 5 implementation gaps are now closed in the current Fabric target surface.
- 2026-03-07: Stage 8 parity audit is now `in_progress`; `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `176 / 176` Fabric GameTests green after adding minimal `if <condition>:` section execution, teaching timed event expressions to parse `future ...`, tightening registry-backed item/entity/potion token lookup for plain natural-script forms, and revalidating the live filtered bucket-catch path that uses `future event-item` inside nested `if` sections.
- 2026-03-08: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `188 / 188` Fabric GameTests green after extending `SecIf` with registered `parse if` / `else parse if`, proving parse-time false branches skip child loading, and adding real `.sk` coverage for both parse-time conditional chains and invalid skipped bodies.
- 2026-03-08: `./gradlew runGameTest --rerun-tasks` and `./gradlew build --rerun-tasks` passed with `190 / 190` Fabric GameTests green after extending `SecIf` with multiline `if any` / `if all` plus `then`, implicit conditional sections, minimal leading `implicit:` tag forwarding, and real `.sk` coverage for both multiline and implicit conditional chains.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed while closing the current parser natural-script gap for omitted inline optional whitespace and inline alternation branches, revalidating live forms such as `%objects% can be equipped on[to] entities`, `%objects% will lose durability when injured`, and `make %entities% not breedable` without changing the `190 / 190` suite size.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after closing plain-effect section ownership through `Effect.parse(...)`, aligning `Variables.withLocalVariables(...)` with upstream copy-back semantics, and adding real `.sk` coverage for `set {_component} to a blank equippable component:`; the active suite increased to `191 / 191`.
- 2026-03-08: `./gradlew test --tests org.skriptlang.skript.bukkit.potion.elements.PotionEntityObjectCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocationCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContextCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after making section-managed potion and loot-context expressions object-safe for object-backed locals, keeping custom damage source / potion effect / loot context section-local real `.sk` paths green; the active suite increased to `194 / 194`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after making parse-log retention real through `ParseLogHandler`, `SkriptLogger`, and `Statement.parse(...)`, preserving specific section-ownership diagnostics without adding a generic section fallback; the active suite remained `194 / 194`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after porting comment-aware line splitting into `Node.splitLine(...)` and wiring `SkriptRuntime.parseScript(...)` through it, restoring live `.sk` support for commented section headers, commented option entries, quoted hashes, and `###` block comments; the active suite increased to `195 / 195`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the parallel loader-diagnostics, shared pattern-matcher, and natural variable-ordering slices; the active suite increased to `196 / 196`.
- 2026-03-08: `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks`, `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the next parallel class-info ordering, empty auto-tag derivation, and loader unreachable-code warning slices; the active suite increased to `197 / 197`.
- 2026-03-08: `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the explicit literal-pattern ordering follow-up and section execution-intent propagation follow-up; the active suite remained `197 / 197`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the local-variable type-hint, lightweight pattern-element graph API, and loader hint-scope slices; the build path again executed the full Fabric GameTest suite successfully and the active suite remained `197 / 197`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the converter-backed class parsing, placeholder flag/time metadata, and plain-statement section-context slices; the active suite increased to `198 / 198`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging the parse-log-aware `Classes.parse(...)`, ordered duplicate parser-tag accumulation, and statement fallback after failed effect/condition init slices; the active suite increased to `199 / 199`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.lang.VariableStringCompatibilityTest --rerun-tasks`, `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after routing runtime message/name placeholders through Patbox `TextPlaceholderAPI`, adding a live `%player:name%` GameTest fixture, and clearing Skript variables before/after each locked runtime GameTest body to prevent suite-order leakage; the active suite increased to `203 / 203`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging raw list-variable map reads, prefixed variable expression parsing, and higher-quality statement fallback diagnostics; the active suite increased to `205 / 205`.
- 2026-03-08: `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after merging inner-expression variable-name validation, classinfo-backed omitted placeholder defaults, and built-in `EffChange` local hints; the active suite increased to `207 / 207`.
- 2026-03-08: `./gradlew test --tests org.skriptlang.skript.fabric.runtime.SilentSyntaxTest --tests org.skriptlang.skript.fabric.runtime.InvulnerableSyntaxTest --tests org.skriptlang.skript.fabric.runtime.AliveKillSyntaxTest --rerun-tasks`, `./gradlew runGameTest --rerun-tasks`, and `./gradlew build --rerun-tasks` all passed after importing base entity-state/control syntax for alive/dead, silent, invulnerable, kill, and related forms; the active suite increased to `216 / 216`.
- 2026-03-07: the original Bukkit `Eff*.java` class list from commit `145c3c9` is now source-complete in the active Fabric tree at `24 / 24`, remaining source-level effect ports `0`.
- 2026-03-07: the active Fabric GameTest suite now passes at `176 / 176` required tests.

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

## Stage 6 Current Scope

Completed in this slice:

- shared event payload expressions now live under `org/skriptlang/skript/bukkit/base/expressions`
- shared runtime validation effects now live under `org/skriptlang/skript/bukkit/base/effects`
- bootstrap now registers base package syntax from `org/skriptlang/skript/bukkit/base` instead of `fabric/syntax`
- existing GameTests still pass after the package move, proving no behavioral regression from the Stage 6 structure shift
- first real base conditions added under `org/skriptlang/skript/bukkit/base/conditions`
- `.sk` fixtures now exercise base conditions on `event-item` and `event-entity`
- dedicated GameTests cover runtime evaluation of base conditions for item stack, slot, inventory, and named entity state
- first original Bukkit expression slice added under `brewing`, `fishing`, and `input`
- dedicated GameTests now cover parsing and runtime execution for brewing slots, fishing hook payloads, hooked entities, and current input keys
- damage-source payload expressions restored for attacker, direct entity, source position, explicit damage position, and food exhaustion
- brewing stand fuel level expression restored through Mojang block-entity reflection and verified from a loaded `on use block` script
- text-display expressions restored for alignment, line width, and opacity, including changer behavior verified against Mojang `Display.TextDisplay`
- minimal generic comparison condition added so scalar/derived expressions can be asserted inside real `.sk` fixtures without introducing more test-only effects
- generic display expressions restored for billboard constraints, light override, display dimensions, shadow properties, and view range through Mojang `Display` reflection
- interaction expressions restored for mutable interaction hitbox dimensions, last click/attack player tracking, and love time on breedable animals
- display transformation expressions restored for translation, scale, and left/right rotation, with dedicated parsers and comparison support for `Vec3` and `Quaternionf`
- item display transform restored through Mojang `ItemDisplayContext`, including script-level comparison and changer coverage
- loottable expressions restored for entity/block loot-table access, direct string-to-loot-table parsing, and loot-table seed access with live setter verification
- remaining original Bukkit expression classes restored and registered under the Fabric bootstrap, including damage-source sections, loot-context sections, furnace expressions, tag expressions, particle expressions, equippable/item-component expressions, rotation/text helpers, breeding-event payloads, and potion-section expressions
- supplemental Fabric events registered for breeding, brewing start/complete, loot generate, and furnace-specific event kinds
- `./gradlew runGameTest --rerun-tasks` and `./gradlew build` pass with the fully-restored expression source set active

Still missing before Stage 6 can be called complete:

- original Bukkit-side non-type base syntax classes are still absent from the active source tree and must be reintroduced class-by-class
- base package condition coverage is still partial
- parity validation against the original Bukkit class list is still missing
- original Bukkit `Cond*.java` class list is now source-complete at `28 / 28`; remaining source-level condition ports: `0`
- original Bukkit `Expr*.java` class list is now source-complete at `84 / 84`; remaining source-level expression ports: `0`

## Stage 5 Current Scope

Completed in this slice:

- first automatic Fabric event bridge added through `ServerTickEvents.END_SERVER_TICK`
- new `on server tick` syntax added and routed into runtime dispatch
- GameTest coverage added for event-driven `.sk` execution without manual dispatch
- initial event mapping matrix documented in `FABRIC_EVENT_MAPPING.md`
- `PlayerBlockBreakEvents.AFTER` now bridges into the runtime through a Mojang-backed block break handle
- GameTest coverage added for mock-player block breaking that executes a real `.sk` file
- `UseBlockCallback.EVENT`, `UseEntityCallback.EVENT`, `UseItemCallback.EVENT`, and `AttackEntityCallback.EVENT` now bridge live player interaction payloads into the runtime
- mixin-backed `on brewing fuel`, `on fishing`, and `on player input` event contexts are now available for condition verification
- GameTests cover live `.sk` execution for brewing consume, fishing lure/open-water, and player input state
- later event slices now also bridge `on breeding`, `on bucket catch`, `on brewing start`, `on brewing complete`, `on loot generate`, and the full furnace event family through real `.sk` + Fabric GameTests
- later event slices now also bridge `on love mode enter`, `on entity potion effect [modification]`, initial potion-cause filters for `potion drink` / `area effect cloud`, original `on player input` toggle/press/release + key-filter forms, and original `on fishing` state variants through real `.sk` + Fabric GameTests

Tracked Stage 5 implementation gaps are currently closed in the active Fabric target surface.

- intentionally dropped deprecated residue:
  - `PATROL_CAPTAIN` potion cause
- full Stage 8 parity audit is still pending before any event class can be called parity-complete
