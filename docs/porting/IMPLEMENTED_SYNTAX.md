# Implemented Syntax Inventory

Last updated: 2026-03-08

This document is a maintenance readme for the currently active Skript-on-Fabric syntax surface.

It is:

- a summary of what is currently registered in the Fabric runtime
- a guide to what is actually verified today
- a pointer map for future event/backend work

It is not:

- a parity claim against Bukkit/Paper
- a complete alias dump for every pattern variant
- a promise that every registered event already has a real Fabric/Mojang backend

## Snapshot

- Source-level condition port: `28 / 28`
- Source-level expression port: `84 / 84`
- Source-level effect port: `24 / 24`
- Verified Fabric GameTests: `197 / 197`
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks`
  - `./gradlew build --rerun-tasks`

## Stage 8 Audit Snapshot

- `145c3c9` package-local classes audited: `23 / 214`
- Package-local parity-complete slice:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Package-local classes still remaining: `191 / 214`
- Cross-cutting Stage 8 gap outside those packages:
  - generic compare for ambiguous bare item ids is not parity-complete yet, for example `event-item is wheat`
- Separate upstream core audit now also active:
  - local `ch/njol/skript`: `128`
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - active closure slices: `Part 1A: lang parser/runtime closure`, `Part 1B: dependency closure`

Primary registration sources:

- [SkriptFabricBootstrap.java](src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java)
- [SkriptFabricAdditionalSyntax.java](src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java)
- [SkriptFabricAdditionalEffects.java](src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java)

Related tracking docs:

- [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)

## Events

### Runtime-backed and verified

| Syntax family | Representative syntax | Main payload |
| --- | --- | --- |
| GameTest | `on gametest` | `GameTestHelper` handle for runtime verification |
| Server tick | `on server tick` | server/world |
| Block break | `on block break` | `event-block`, `event-player` |
| Use block | `on use block` | `event-block`, `event-player` |
| Use entity | `on use entity` | `event-entity`, `event-player` |
| Use item | `on use item` | `event-item`, `event-player` |
| Attack entity | `on attack entity` | attacked entity, player |
| Damage | `on damage` | damaged entity, `event-damage source` |
| Breeding | `on breeding`, `on breeding of cow` | breeding mother, father, offspring, breeder, `event-item`; live coverage confirms non-empty breeding `event-item`, and direct event-handle/expression checks confirm the captured bred-with item resolves as wheat |
| Bucket catch | `on bucket catch`, `on bucket catching of pufferfish` | bucketed entity, `event-player` |
| Love mode enter | `on love mode enter` | `event-entity`, `event-player` when available |
| Brewing start | `on brewing start` | brewing stand block, brewing time |
| Brewing complete | `on brewing complete`, `on brewing complete for potion`, `on brewing complete for speed` | brewing stand block, brewed result list |
| Brewing fuel | `on brewing fuel`, `on brewing fuel of blaze_powder` | brewing stand block, fuel consume state |
| Entity potion effect | `on entity potion effect of poison added/changed/removed/cleared`, `on entity potion effect of minecraft:poison added`, `on entity potion effect of poison added due to potion drink`, `on entity potion effect of poison added due to area effect cloud`, `on entity potion effect of poison added due to food`, `on entity potion effect of poison cleared due to milk`, `on entity potion effect of speed added due to beacon`, `on entity potion effect of conduit_power added due to conduit`, `on entity potion effect of poison added/cleared due to command`, `on entity potion effect of poison added due to attack`, `on entity potion effect of poison added due to arrow`, `on entity potion effect of poison added due to unknown`, `on entity potion effect of poison added due to potion splash`, `on entity potion effect due to plugin`, `on entity potion effect due to death` | `event-entity`, typed action headers, `event-potion effect`, `past event-potion effect`, action/amplifier payload expressions; status-effect type filters and expressions are registry-backed, `bare id` values default to `minecraft`, explicit namespaces are preserved, and cause-filter forms are live-verified for every retained supported cause value: `potion drink`, `area effect cloud`, `food`, `milk`, `beacon`, `conduit`, `command`, `attack`, `arrow`, `unknown`, `potion splash`, `totem`, `wither rose`, `conversion`, `axolotl`, `warden`, `spider spawn`, `villager trade`, `expiration`, `dolphin`, `turtle helmet`, `illusion`, `plugin`, and `death`; deprecated-unused `PATROL_CAPTAIN` was intentionally dropped from the supported surface |
| Fishing | `on fishing`, `on fishing line cast`, `on fish caught`, `on entity hooked`, `on bobber hit ground`, `on fish approaching`, `on fish bite`, `on fish escape`, `on fishing rod reel in`, `on fishing state change` | hook entity, state, lure/open-water state, event entity when available |
| Loot generate | `on loot generate` | loot table, generated loot, looter/looted entity, loot location |
| Player input | `on player input`, `on input key press`, `on forward movement key release`, `on sneak key toggle` | current and previous input state, any-key and keyed toggle/press/release headers |
| Furnace burn | `on fuel burn` | furnace block, source item, burned fuel |
| Furnace smelting start | `on smelting start` | furnace block, source item, fuel, furnace times |
| Furnace smelt | `on furnace smelt` | furnace block, smelted item, result |
| Furnace extract | `on furnace extract` | furnace block, extracted item, player |

### Registered, but not runtime-backed yet

None in the current Fabric registration set.

### Known event-syntax gaps

- No currently tracked runtime-backed event-syntax gaps remain in the active Fabric target surface.
- Generic status-effect type parsing is registry-backed: `bare id` values default to `minecraft`, explicit namespaces are preserved, and real `.sk` coverage now includes `minecraft:poison`.
- Package-local Stage 8 parity-complete slice now covers `breeding (12 / 12)`, `input (5 / 5)`, and `interactions (6 / 6)`.
- A cross-cutting base-surface gap still remains outside those packages: ambiguous bare item-id equality through generic compare, for example `event-item is wheat`.

## Conditions

## Core Language Runtime

- `options:` structure replacement is live in real `.sk` files
- `options:` now load through a validator-backed path that accepts both runtime `EntryNode` trees and raw simple `key: value` entries
- local `SectionNode` now provides case-insensitive mapped lookup, ordered replace/remove, parent-reassignment, and entry-conversion map synchronization through `NodeMap`
- the active runtime script parser now strips inline comments, preserves quoted `#`, unescapes doubled `##`, and skips `###` block comments through `Node.splitLine(...)`
- live `.sk` coverage now includes comment-aware loader parsing for commented section headers, commented option entries, quoted hashes, and block-commented invalid syntax
- `{...}` variable expressions are parsed directly by `SkriptParser`
- `ParserInstance` now owns a `HintManager`, and parse-time local variable type hints can narrow simple local variables away from generic `%object%` requests while rejecting incompatible typed lookups
- variables default to case-insensitive storage and lookup
- list-variable `set` copies keyed list sources into reindexed numeric target slots instead of preserving source keys
- prefix/list iteration now uses natural numeric ordering, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set`
- list variables now expose the legacy loop aliases `index`, `var`, `variable`, and `value`
- list-variable predicate checks now use upstream-style all-values `getAnd()` semantics instead of collapsing back to a single-value/default-expression path
- quoted string literals remain strings in generic `%object%` contexts during live script loading
- `SkriptParser` now supports minimal raw regex captures for registered syntax patterns like `if <.+>`, plus the minimal leading `implicit:` tag needed by registered conditional sections
- `SkriptParser` now routes matching through the shared `patterns` package and receives general parse tags plus XOR marks through `ParseResult.mark` on the current compatibility surface, including the current bare leading `:` auto-tag derivation path
- `PatternCompiler` / `SkriptPattern` now support placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦`
- `PatternCompiler` now also builds a lightweight `PatternElement` graph, and `SkriptPattern` now exposes `countTypes()`, `countNonNullTypes()`, and `getElements(...)` for the current upstream-introspection compatibility surface
- the shared matcher now keeps omitted optional raw-regex captures and unmatched alternation regex branches from failing `ParseResult` construction
- `SkriptParser` now preserves required whitespace around omitted inline optional groups and inline alternation branches for the currently verified natural-script surface, which keeps live forms like `%objects% can be equipped on[to] entities`, `%objects% will lose durability when injured`, and `make %entities% not breedable` green again
- chained `if / else if / else` sections execute in real `.sk` files, including grouped outer parentheses around conditions, and now load through the normal registered `Section` path instead of a dedicated `Statement` fallback
- `parse if` and `else parse if` now evaluate at parse time, skip child loading when false, and are live-verified both for normal chain execution and for skipped invalid bodies
- multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run` execute in real `.sk` files
- implicit conditional sections such as `%condition%:` execute in real `.sk` files through the same registered `SecIf` path
- `input`, typed `%classinfo% input`, and `input index` resolve directly in active `InputSource` context
- registered pure `Section` nodes now load through `ScriptLoader.loadItems(...)` instead of being dropped into statement-only fallback
- `ScriptLoader` section-node fallback now restores the more specific retained section-versus-statement diagnostic when both parse paths fail
- `ScriptLoader.loadItems(...)` and `parseSectionTriggerItem(...)` now manage section and temporary non-section hint scopes, so failed section parses clear temporary hints while successful section loads can propagate, freeze, or merge hints through the active stop-flow path
- stopping statements now make `ScriptLoader` emit the upstream-style unreachable-code warning behind `ScriptWarning.UNREACHABLE_CODE` suppression, and real `.sk` coverage verifies that the later line never executes
- nested `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` results now propagate through `TriggerItem.walk(...)`, and registered sections now surface stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
- plain conditions used as section headers now report a specific ownership error instead of silently returning a body-less condition item
- plain effects with section-managing expressions now receive their `SectionNode` through `Effect.parse(...)`, so real lines like `set {_component} to a blank equippable component:` execute their section body and propagate local-variable mutations back to the outer event scope
- section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed locals instead of assuming typed runtime arrays
- shared literal-pattern class lookups now honor the same stable class-info ordering used by superclass resolution when multiple class infos register the same alias
- when a section expression swaps the current event type, outer event payloads should be captured into locals before entering the section body; the currently verified real `.sk` paths do this for custom damage source, potion effect, loot context, and blank equippable component sections
- `Statement.parse(...)` now retains specific parse errors across nested parser scopes, so valid effects or function calls used as sections keep their ownership diagnostic instead of falling through to a generic `Can't understand this section` fallback

### Base

- `is empty`
  - representative forms: `%itemstack% is empty`, `%slot% is empty`, `%inventory% is empty`
- `is named`
  - representative forms: `%entity% is named`, `%itemstack% is named`
- generic comparison
  - representative forms: `%objects% is %objects%`
  - current audit note: ambiguous bare item-id equality is not parity-complete yet

### Breeding

- `can age`
- `can breed`
- `is adult`
- `is baby`
- `is in love`

### Brewing

- `brewing stand will consume fuel`

### Damage source

- `scales damage with difficulty`
- `was indirectly caused`

### Fishing

- `lure enchantment bonus is applied`
- `entity is in open water`

### Input

- `player is pressing key`
  - supports current and past-state forms

### Displays and interactions

- text display has drop shadow
- text display is visible through blocks
- entity is responsive

### Loot tables

- block/entity has loot table
- block/entity is lootable

### Potion

- living entity has potion effect
- entity is poisoned
- potion effect is ambient
- potion effect is instant
- potion effect has icon
- potion effect has particles

### Tags

- object is tagged as/with another object

### Equippable component

- loses durability when injured
- can be dispensed
- can be equipped on entities
- can be sheared off
- can swap equipment on right click

## Expressions

The expression port is source-complete at `84 / 84`.
The list below groups the active syntax by domain and calls out the representative forms that are useful during maintenance.

### Base runtime payload

- `event-block`
- `event-player`
- `event-entity`
- `event-item`
- `event-damage source`

### Base mapped types and utilities

- mapped core types are available through registered class infos:
  - player
  - world
  - entity
  - location
  - block
  - item stack
  - inventory
  - vector
  - slot
  - timespan
  - quaternion

### Breeding

- `love time of %entities%`
- `breeding mother`
- `breeding father`
- `bred offspring`
- `breeder`

### Brewing

- `brewing fuel level of %blocks%`
- `brewing fuel slot`
- `brewing ingredient slot`
- `brewing result slot`
- `brewing time of %blocks%`
- `brewing results`

### Furnace

- furnace event items
  - representative forms: `smelted item`, `extracted item`, `smelting item`, `burned fuel`
- furnace slots
  - representative forms: input/fuel/result slot expressions from `ExprFurnaceSlot`
- furnace times
  - representative forms:
    - `cook time`
    - `cooking time`
    - `total cook time`
    - `total cooking time`
    - `fuel burn time`
    - each also supports `of %blocks%`

### Damage source

- `causing entity`
- `direct entity`
- `source location`
- `damage location`
- `food exhaustion`
- `damage type`
- `created damage source`
- `a custom damage source`

### Fishing

- `fishing hook`
- `hooked entity`
- `minimum / maximum fishing wait time`
- `fishing bite time`
- `minimum / maximum fishing approach angle`

### Input

- `current input keys of %players%`
  - supports current and past-state forms in both direct event evaluation and real `.sk` fixtures

### Interactions

- `interaction width of %entities%`
- `interaction height of %entities%`
- `last player to attack/interact/click %entities%`
- `last attack/interact/click time of %entities%`
  - original natural forms such as `last time %entity% was attacked/interacted with/clicked on` are live-covered

### Displays

- generic display state
  - `billboard`
  - `brightness override`
  - `display height`
  - `display width`
  - `shadow radius`
  - `shadow strength`
  - `view range`
- interpolation and teleport timing
  - `interpolation delay`
  - `interpolation duration`
  - `teleport duration`
- transformations
  - `left rotation`
  - `right rotation`
  - `transformation scale`
  - `transformation translation`
- item display
  - `item display transform`
- text display
  - `text alignment`
  - `line width`
  - `text opacity`
  - `glow color override`
  - `text of %entities%`

### Loot tables and loot context

- `loot table of event-entity / event-block / %entities% / %blocks%`
- `loot table seed of ...`
- `loot tables from %strings%`
- `loot`
- `loot items of %objects%`
- `loot context`
- `a loot context at %locations%`
- `looted entity`
- `loot location`
- `looter`
- `loot luck`

### Potion

- `active potion effects of %entities%`
- `specific potion effect of %entities%`
- `potion duration`
- `potion amplifier`
- `potion category`
- `event-potion effect`
- `past event-potion effect`
- `event-potion effect action`
  - registered for entity potion events, but real-script parity coverage is still partial

### Particles and misc

- particle data expressions
  - `particle count`
  - `particle distribution`
  - `particle offset`
  - `particle speed`
  - dust/item particle builders
- rotation helpers
  - quaternion axis
  - quaternion angle
  - rotated vectors/entities/objects
- item/entity helpers
  - `item of %entities%`

### Equippable component

- `equippable component of %objects%`
- `a blank equippable component`
- `camera overlay`
- `allowed entities`
- `equip sound`
- `equipped model/asset id`
- `shear sound`
- `equipment slot`
- `item component copy`

### Tags

- tags-of-type style expressions are active through the `tags` package registration path

## Effects

### Base/runtime utility

- test-only verification helpers
  - `set test block at ...`
  - `set test block for %block% ...`
  - `set test block above %block% ...`
  - `set test block at %location% ...`
  - `set test block under player %player% ...`
  - `set test name of entity %entity% ...`
  - `set test name of item %itemstack% ...`
- generic changer effect
  - `set %object% to %object%`
  - `add %object% to %object%`
  - `remove %object% from %object%`
  - `reset %object%`
  - `delete %object%`
- fishing approach-angle setter

### Breeding

- lock/unlock age
- prevent/allow aging
- make breedable / sterilize
- make adult / make baby

### Brewing

- make brewing stand consume fuel
- prevent brewing stand from consuming fuel

### Displays and interactions

- add/remove text display drop shadow
- make entity visible through blocks
- make entity responsive / unresponsive
- rotate objects/entities/locations

### Fishing

- apply/remove lure enchantment bonus
- reel/pull in hooked entity

### Loot tables

- generate loot of/using a loot table in a target

### Potion

- apply/grant potion effects
- poison / cure poison
- ambient toggle
- icon toggle
- infinite/permanent toggle
- particle toggle

### Particles

- play/show/draw particles or game effects at locations or on entities

### Tags

- register custom item/block/entity tags

### Equippable component

- toggle damage-on-hurt
- toggle dispensable
- toggle equip-on-interact
- toggle shearable
- toggle swap-equipment

## Statements and sections

There is no large separate user-facing statement inventory being tracked here beyond the currently registered effects, expressions, conditions, and events.

The notable helper syntax that behaves like a builder/context entry point today is:

- `a loot context at %locations%`

The currently supported control-flow section syntax is:

- `if <condition>:`

The current loader/runtime also supports:

- generic registered `Section` parsing for section-backed trigger items

## What This Document Does Not Guarantee

- Registered event syntax is not automatically runtime-backed.
- Registered syntax is not automatically parity-complete with the original Bukkit implementation.
- Some syntax families are verified only by parser/unit coverage, while others are verified end-to-end by real `.sk` + Fabric GameTest.

## Immediate Known Gaps

- Tracked Stage 5 event/backend implementation gaps are closed in the current Fabric target surface.
- Deprecated-unused `PATROL_CAPTAIN` was intentionally dropped instead of emulated.
- Full Stage 8 parity audit is not complete yet.
