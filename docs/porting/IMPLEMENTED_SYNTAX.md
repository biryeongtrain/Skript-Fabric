# Implemented Syntax Inventory

Last updated: 2026-03-11

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
- Verified Fabric GameTests: `245 / 245`
- Latest full verification:
  - `./gradlew build --rerun-tasks`
  - build path executed `runGameTest`
- Recent verified additions:
  - latest verified worker-harvest expression batch now reduces the raw upstream shortfall to `845 / 1189`:
    - newly imported expressions `26`:
      - inventory/item: `ExprItemFlags`
      - parser/queue: `ExprCaughtErrors`, `ExprDequeuedQueue`, `ExprEventExpression`, `ExprFilter`, `ExprFunction`, `ExprKeyed`, `ExprLoopIteration`, `ExprPercent`, `ExprQueue`, `ExprQueueStartEnd`, `ExprRecursive`, `ExprRepeat`, `ExprRound`, `ExprSets`
      - world/property: `ExprChunkX`, `ExprChunkZ`, `ExprHumidity`, `ExprLocation`, `ExprLocationAt`, `ExprLocationOf`, `ExprRedstoneBlockPower`, `ExprSeaLevel`, `ExprSeed`, `ExprSimulationDistance`, `ExprSpawn`
    - representative syntax from this batch:
      - inventory/item:
        - `item flags of %itemtypes%`
        - `set item flags of %itemtypes% to %strings%`
      - parser/queue:
        - `last caught runtime errors`
        - `dequeued %queue%`, `unrolled %queue%`
        - `event-%classinfo%`
        - `%objects% where [<condition>]`
        - `the function named %string%`, `functions named %strings%`, `the functions from %script%`
        - `keyed %objects%`
        - `loop-iteration`, `loop-iteration-2`
        - `%number% percent of %numbers%`
        - `a queue`, `a queue of %objects%`
        - `start of %queue%`, `end of %queue%`
        - `recursive %objects%`
        - `%strings% repeated %integer% times`
        - `rounded %numbers%`, `floored %numbers%`, `%numbers% rounded up`
        - `every %classinfo%`, `all of the %classinfo%`
      - world/property:
        - `chunk x-coordinate of %chunks%`, `chunk z-coordinate of %chunks%`
        - `humidity of %blocks%`
        - `event-location`, `location north of %location%`
        - `location at x = %number%, y = %number%, and z = %number% [in %world%]`
        - `location of %locations/blocks/entities/chunks%`
        - `redstone power of %blocks%`
        - `sea level of %worlds%`
        - `seed of %worlds%`
        - `simulation distance of %worlds%`
        - `spawn location of %worlds%`
    - targeted verification:
      - `isolatedExpressionSyntaxS2CompatibilityTest`
      - `ExpressionItemCompatibilityTest`
      - `ExpressionBlockWorldLocationCompatibilityTest`
      - `ExpressionMixedRuntimeM6CompatibilityTest`
    - active-runtime note:
      - coordinator intentionally kept these new expressions import-only in this batch
      - the world/property slice was not left active because representative real `.sk` GameTest coverage is still missing
  - previous mixed-runtime coordinator batch now reduces the raw upstream shortfall to `727 / 1189`:
    - live-activated events `11`: `EvtBeaconEffect`, `EvtBeaconToggle`, `EvtBlock`, `EvtBookEdit`, `EvtBookSign`, `EvtClick`, `EvtEntity`, `EvtEntityTransform`, `EvtExperienceSpawn`, `EvtHealing`, `EvtItem`
    - newly imported conditions `10`: `CondCancelled`, `CondDamageCause`, `CondEntityUnload`, `CondIncendiary`, `CondItemDespawn`, `CondIsPreferredTool`, `CondIsSedated`, `CondLeashWillDrop`, `CondRespawnLocation`, `CondScriptLoaded`
    - newly imported expressions `20`: `ExprAffectedEntities`, `ExprBarterInput`, `ExprConsumedItem`, `ExprExperienceCooldownChangeReason`, `ExprExplodedBlocks`, `ExprHatchingNumber`, `ExprHatchingType`, `ExprHealAmount`, `ExprLastAttacker`, `ExprLeashHolder`, `ExprLevel`, `ExprMaxDurability`, `ExprMaxHealth`, `ExprMaxItemUseTime`, `ExprMaxStack`, `ExprNoDamageTicks`, `ExprItemOwner`, `ExprItemThrower`, `ExprRawName`, `ExprSpeed`
    - newly imported effects `19` unique: `EffColorItems`, `EffEnchant`, `EffEquip`, `EffDrop`, `EffHealth`, `EffTeleport`, `EffWakeupSleep`, `EffFireworkLaunch`, `EffElytraBoostConsume`, `EffExplosion`, `EffTree`, `EffEntityVisibility`, `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`, `EffCopy`, `EffSort`, `EffToggle`, `EffExceptionDebug`
  - active runtime registration from that batch now covers conditions `10`, expressions `20`, effects `4`, and events `11` through `SkriptFabricBootstrap`
  - the blocked mixed-batch effect remainder stays import-only for now:
    - `EffColorItems`, `EffEnchant`, `EffEquip`, `EffDrop`, `EffHealth`, `EffTeleport`, `EffWakeupSleep`, `EffFireworkLaunch`, `EffElytraBoostConsume`, `EffExplosion`, `EffTree`, `EffEntityVisibility`, `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`
  - coordinator stabilization in the batch:
    - `ExpressionEventContextBundleCompatibilityTest` now runs as an isolated registry-sensitive suite
    - `MixedRuntimeSyntaxBatchTest` was added as a dedicated isolated runtime parser suite
    - the healing bridge now carries the numeric heal amount through the compat handle
  - latest verified active runtime inventory/container closure now also adds 10 upstream `ch/njol/skript` classes:
    - `expressions`: `ExprChestInventory`, `ExprEnderChest`, `ExprInventory`, `ExprInventoryInfo`, `ExprInventorySlot`, `ExprItemsIn`, `ExprFirstEmptySlot`
    - `conditions`: `CondContains`, `CondItemInHand`, `CondIsWearing`
  - this inventory/container slice is registered on `SkriptFabricBootstrap` and covered by `ExpressionInventoryCompatibilityTest`, `ConditionInventoryCompatibilityTest`, and `InventorySyntaxTest`
  - latest verified import bundle now also adds 100 upstream `ch/njol/skript` classes:
    - `expressions`: `ExprAmountOfItems`, `ExprItemAmount`, `ExprItemWithLore`, `ExprItemWithTooltip`, `ExprItemWithCustomModelData`, `ExprItemWithEnchantmentGlint`, `ExprItems`, `ExprExactItem`, `ExprActiveItem`, `ExprAge`, `ExprAllayJukebox`, `ExprArrowsStuck`, `ExprBeehiveFlower`, `ExprBeehiveHoneyLevel`, `ExprBreakSpeed`, `ExprCreeperMaxFuseTicks`, `ExprDomestication`, `ExprDuplicateCooldown`, `ExprEntityItemUseTime`, `ExprEntityOwner`, `ExprEntitySize`, `ExprExperienceCooldown`, `ExprEyeLocation`, `ExprFoodLevel`, `ExprGlidingState`, `ExprHealth`, `ExprNoDamageTime`, `ExprPortalCooldown`, `ExprRemainingAir`
    - `conditions`: `CondCanHold`, `CondCanSee`, `CondGlowingText`, `CondIsLoaded`, `CondIsPathfinding`, `CondIsRiding`, `CondIsRinging`, `CondIsSaddled`, `CondIsStackable`, `CondIsWithin`, `CondPlayedBefore`, `CondTooltip`, `CondWithinRadius`
    - `effects`: `EffAllayCanDuplicate`, `EffAllayDuplicate`, `EffBan`, `EffBlockUpdate`, `EffBreakNaturally`, `EffCancelCooldown`, `EffCancelDrops`, `EffCancelEvent`, `EffCancelItemUse`, `EffCharge`, `EffCommand`, `EffDancing`, `EffDropLeash`, `EffExplodeCreeper`, `EffFireResistant`, `EffGoatHorns`, `EffGoatRam`, `EffHidePlayerFromServerList`, `EffIncendiary`, `EffItemDespawn`, `EffKnockback`, `EffLidState`, `EffLightning`, `EffLoadServerIcon`, `EffLook`, `EffMakeEggHatch`, `EffOpenBook`, `EffOpenInventory`, `EffPlayerInfoVisibility`, `EffPush`, `EffPvP`, `EffRing`, `EffSendBlockChange`, `EffStopServer`, `EffSwingHand`, `EffTooltip`, `EffWardenDisturbance`, `EffWorldLoad`, `EffWorldSave`
    - `events/helpers`: `EvtBookEdit`, `EvtBookSign`, `EvtClick`, `EvtEntityShootBow`, `EvtEntityTarget`, `EvtEntityTransform`, `EvtExperienceSpawn`, `EvtFirework`, `EvtGameMode`, `EvtHarvestBlock`, `EvtHealing`, `EvtLeash`, `EvtMoveOn`, `EvtPlayerArmorChange`, `EvtPortal`, `EvtResourcePackResponse`, `EvtWeatherChange`, `EvtWorld`, plus local helper scaffolds `FabricEggThrowEventHandle`, `FabricEntityUnleashEventHandle`, `FabricExplosionPrimeEventHandle`, `EventClassInfoRegistrar`, `EventSyntaxRegistry`, and `FabricEventCompatHandles`
  - these landed as parser/unit-verified compatibility imports and shortfall reduction; they are not all bootstrapped into the active Fabric runtime registration set yet
  - imported syntax classes in this batch preserve upstream `ch.njol.skript.doc.*` annotations where present
  - latest verified runtime-surface additions underneath that import layer still include 27 upstream-backed conditions:
    - `CondCanFly`, `CondCanPickUpItems`, `CondHasScoreboardTag`, `CondIsBlocking`, `CondIsClimbing`, `CondIsFlying`, `CondIsGliding`, `CondIsHandRaised`, `CondIsLeftHanded`, `CondIsOnGround`, `CondIsRiptiding`, `CondIsSleeping`, `CondIsSneaking`, `CondIsSwimming`, `CondIsTamed`, `CondIsBlock`, `CondIsBlockRedstonePowered`, `CondIsCommandBlockConditional`, `CondIsEdible`, `CondIsFlammable`, `CondIsInfinite`, `CondIsInteractable`, `CondIsOccluding`, `CondIsPassable`, `CondIsSolid`, `CondIsTransparent`, `CondIsVectorNormalized`
  - latest verified runtime-surface additions now also include 20 upstream-backed effects:
    - `EffCustomName`, `EffEating`, `EffHandedness`, `EffIgnite`, `EffLeash`, `EffMakeFly`, `EffPlayingDead`, `EffShear`, `EffTame`, `EffToggleCanPickUpItems`, `EffActionBar`, `EffBroadcast`, `EffKick`, `EffMessage`, `EffOp`, `EffPlaySound`, `EffResetTitle`, `EffSendResourcePack`, `EffSendTitle`, `EffStopSound`
  - the earlier runtime-surface additions `ExprRandomCharacter` and `ExprTimes`, registered on the Fabric bootstrap and covered by `RandomExpressionSyntaxTest`, remain merged underneath this batch
  - latest verified support-surface additions behind that unchanged `230 / 230` GameTest baseline now also include `CondAI`, `CondCompare`, `CondIsAlive`, `CondIsBurning`, `CondIsEmpty`, `CondIsInvisible`, `CondIsInvulnerable`, `CondIsSilent`, `CondIsSprinting`, `ExprGlowing`, and `ExprRandom`
  - `ExprRandom` was intentionally left off the active runtime inventory after verification showed the `%*classinfo%` parse path still misresolves `"string"` through the item-type path during init
  - the latest verified ancillary upstream-core additions also still include the lightweight `doc` annotation bundle and the non-scheduler `update` manifest/checker bundle; these reduce raw shortfall only and do not change the active runtime syntax inventory
  - the earlier Lane E helper bundles (`CondPermission`, `CondIsDivisibleBy`, `CondMinecraftVersion`, `CondIsUsingFeature`, `ExprARGB`, `ExprAngle`, `ExprDebugInfo`, `ExprHash`, `ExprTimespanDetails`, `ExprAmount`, `ExprFormatDate`, `ExprIndices`, `ExprInverse`) remain merged underneath it
  - the experimental variable storage backend / `FlatFileStorage` slice was intentionally excluded from the final green batch after runtime regression

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
  - local `ch/njol/skript`: `727`
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - current shortfall: `367`
  - active closure slices: `Part 1A: lang parser/runtime closure`, `Part 1B: dependency closure`
  - latest shortfall-focused closure is the worker-harvest import-only expression batch that adds 26 more upstream expressions on top of the earlier mixed-runtime coordinator bundle

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
| Beacon effect | `on primary beacon effect`, `on secondary beacon effect of speed` | beacon block position, primary/secondary flag, effect type |
| Beacon toggle | `on beacon toggle`, `on beacon activation`, `on beacon deactivation` | beacon block position, activation state |
| Block compatibility | `on block breaking of stone` | compat block handle; currently active only through break-backed dispatch |
| Book edit | `on book edit` | player plus before/after book item payload |
| Book sign | `on book signing` | player plus signed book payload |
| Click | `on right click with stick on stone`, `on left click` | click type, clicked entity/block, held tool |
| Entity compatibility | `on spawning of zombie`, `on death of zombie` | entity lifecycle handle; currently active only for spawn/death |
| Entity transform | `on zombie transforming due to "curing"` | transforming entity plus coarse reason string |
| Experience spawn | `on experience orb spawn` | spawned experience amount |
| Healing | `on healing of zombie by "magic"` | healed entity, coarse reason string, heal amount |
| Item compatibility | `on item spawn of stick` | compat item handle; currently active only for item-spawn dispatch |

### Imported compatibility events not yet bootstrapped into the active runtime

- command / command list send
  - representative forms: `command %strings%`, `send[ing] [of [the]] [server] command[s] list`
- player lifecycle and level
  - representative forms: `first join`, `player level up`, `player level down`, `player experience increase`, `player experience decrease`
- movement and world transitions
  - representative forms: `player move`, `player rotate`, `player enters a chunk`, `%entitytypes% teleport`
- spectating
  - representative forms: `player start spectating [of %-*entitydatas%]`, `player stop spectating`, `player swap spectating`
- player/entity/world compatibility events
  - representative forms: `gamemode change [to %gamemode%]`, `portal`, `resource pack accepted/declined/failed`, `weather change [to %-strings%]`, `world load/save/init/unload`
- combat and interaction compatibility events
  - representative forms: `entity shoot[ing] [a] bow`, `entity target`, `firework explode`, `player leash/unleash`, `armor change`, `move on %itemtypes%`, `block harvest`
- remaining concrete Fabric hook candidates from the imported event batch
  - representative forms: `entity block change`, `grow[th] from %itemtypes/blockdatas%`, `grow[th] into %structuretypes/itemtypes/blockdatas%`, `plant grow[th]`, `pressure plate`, `vehicle collision [(with|of) ...]`, `vehicle block collision`, `vehicle entity collision`

### Known event-syntax gaps

- Partial active rows that still need broader backing:
  - `EvtBlock` is currently break-backed only
  - `EvtItem` is currently item-spawn-backed only
  - `EvtEntity` is currently lifecycle spawn/death-backed only
  - `EvtEntityTransform` and `EvtHealing` still use coarse reason strings
- Still missing dedicated live producers for the imported event classes:
  - `EvtEntityBlockChange`, `EvtGrow`, `EvtPlantGrowth`, `EvtPressurePlate`, `EvtVehicleCollision`
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
- upstream-prefixed variable forms such as `var {x}`, `variable {x}`, and `the variable {x}` are also parsed as variable expressions
- `ParserInstance` now owns a `HintManager`, and parse-time local variable type hints can narrow simple local variables away from generic `%object%` requests while rejecting incompatible typed lookups
- variable-name validation now ignores `*` inside paired `%...%` spans, so dynamic forms such as `result::%{source::*}%` parse again while invalid outer list markers still fail
- exact built-in `set {_value} to ...` lines now publish parse-time local-variable hints for later sibling lines through `EffChange`
- omitted non-optional placeholders can now fall back to exact `ClassInfo` default expressions when the parser did not register a narrower default
- `Classes` now also exposes exact registered default expressions through `getDefaultExpression(String)` and `getDefaultExpression(Class<?>)`
- the legacy Java class-info compatibility path now restores `object`, numeric wrappers/primitives, `boolean`, `string`, and `uuid` parsing without Bukkit or Yggdrasil dependencies
- variables default to case-insensitive storage and lookup
- list-variable `set` copies keyed list sources into reindexed numeric target slots instead of preserving source keys
- prefix/list iteration now uses natural numeric ordering, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set`
- raw list-variable reads through `Variables.getVariable("name::*", ...)` now reconstruct nested maps, including upstream-style `null` sentinel parent entries when a direct parent value and descendants coexist
- list variables now expose the legacy loop aliases `index`, `var`, `variable`, and `value`
- list-variable predicate checks now use upstream-style all-values `getAnd()` semantics instead of collapsing back to a single-value/default-expression path
- quoted string literals remain strings in generic `%object%` contexts during live script loading
- `%timespan%` parsing now accepts natural durations, command short forms such as `1.5h`, clock forms such as `1:02:03`, and localized `forever` / `eternity` values on the active compatibility path
- exact Patbox-style placeholders such as `%player:name%` now resolve through Patbox `TextPlaceholderAPI` on active message/name string paths when live event context exists
- `FunctionRegistry` now prefers local script signatures and functions before global fallback, so compatible global overloads no longer make valid local calls ambiguous
- statement fallback now keeps earlier higher-quality parse diagnostics instead of replacing them with lower-quality later plain-statement failures on the same syntax line
- `SkriptParser` now supports minimal raw regex captures for registered syntax patterns like `if <.+>`, plus the minimal leading `implicit:` tag needed by registered conditional sections
- `SkriptParser` now routes matching through the shared `patterns` package and receives general parse tags plus XOR marks through `ParseResult.mark` on the current compatibility surface, including the current bare leading `:` auto-tag derivation path
- `SkriptParser.ParseResult.tags` and the shared matcher now preserve duplicate parse tags in encounter order instead of collapsing them into a unique set
- `PatternCompiler` / `SkriptPattern` now support placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦`
- `PatternCompiler` now also builds a lightweight `PatternElement` graph, and `SkriptPattern` now exposes `countTypes()`, `countNonNullTypes()`, and `getElements(...)` for the current upstream-introspection compatibility surface
- grouped `PatternElement` nodes now preserve string/combinations parity through `toFullString()`, `getCombinations(...)`, and `getAllCombinations()`, and malformed grouped patterns now wrap through `MalformedPatternException`
- `PatternCompiler` now also preserves placeholder-local parse flags (`*` / `~`), leading optional markers, plural metadata, and `@time`, and `SkriptPattern` now applies placeholder-local parse flags plus time through the shared matcher while leaving plurality metadata non-enforcing on the current green corpus
- the shared matcher now keeps omitted optional raw-regex captures and unmatched alternation regex branches from failing `ParseResult` construction
- `SkriptParser` now preserves required whitespace around omitted inline optional groups and inline alternation branches for the currently verified natural-script surface, which keeps live forms like `%objects% can be equipped on[to] entities`, `%objects% will lose durability when injured`, and `make %entities% not breedable` green again
- chained `if / else if / else` sections execute in real `.sk` files, including grouped outer parentheses around conditions, and now load through the normal registered `Section` path instead of a dedicated `Statement` fallback
- `parse if` and `else parse if` now evaluate at parse time, skip child loading when false, and are live-verified both for normal chain execution and for skipped invalid bodies
- multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run` execute in real `.sk` files
- implicit conditional sections such as `%condition%:` execute in real `.sk` files through the same registered `SecIf` path
- `input`, typed `%classinfo% input`, and `input index` resolve directly in active `InputSource` context
- registered pure `Section` nodes now load through `ScriptLoader.loadItems(...)` instead of being dropped into statement-only fallback
- `ScriptLoader` section-node fallback now restores the more specific retained section-versus-statement diagnostic when both parse paths fail
- `ScriptLoader.parseSectionTriggerItem(...)` now keeps temporary local-variable hints when a section line fails `Section.parse(...)` but succeeds through statement fallback, and that path is live-covered by `statement_fallback_section_hint_test_block.sk`
- `ScriptLoader.loadItems(...)` and `parseSectionTriggerItem(...)` now manage section and temporary non-section hint scopes, so failed section parses clear temporary hints while successful section loads can propagate, freeze, or merge hints through the active stop-flow path
- section-only hint-scope clearing now removes the targeted section frame instead of leaving copied hints behind for later local-variable parsing
- stopping statements now make `ScriptLoader` emit the upstream-style unreachable-code warning behind `ScriptWarning.UNREACHABLE_CODE` suppression, and real `.sk` coverage verifies that the later line never executes
- nested `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` results now propagate through `TriggerItem.walk(...)`, and registered sections now surface stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
- plain conditions used as section headers now report a specific ownership error instead of silently returning a body-less condition item
- `Statement.parse(...)` now lets a later same-pattern plain statement win after earlier effect/condition init failures, while restoring the best prior specific error if no statement ultimately matches
- `Statement.parse(...)` now clears inherited outer section ownership on plain statement parses, so nested function/effect/condition arguments no longer accidentally inherit an enclosing expression section
- section-line effect parsing now resets section ownership between effect candidates, so a failed section-claiming effect candidate cannot let a later literal effect parse the same section line incorrectly
- plain effects with section-managing expressions now receive their `SectionNode` through `Effect.parse(...)`, so real lines like `set {_component} to a blank equippable component:` execute their section body and propagate local-variable mutations back to the outer event scope
- real `.sk` coverage now also includes a nested plain-effect argument inside an outer expression section
- section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed locals instead of assuming typed runtime arrays
- shared literal-pattern class lookups now honor the same stable class-info ordering used by superclass resolution when multiple class infos register the same alias
- `Classes.parse(...)` now falls back through registered converters after direct parser lookup, and `UnparsedLiteral` conversion can now reuse that path for converter-backed source types
- `Classes.parse(...)` now also clears stale direct-parser failures before later parser or converter fallback success, so successful fallback does not leak earlier parser diagnostics
- successful section-line fallback from `Section.parse(...)` to `Statement.parse(...)` now preserves specific non-default section diagnostics instead of collapsing them to only the generic section error
- when a section expression swaps the current event type, outer event payloads should be captured into locals before entering the section body; the currently verified real `.sk` paths do this for custom damage source, potion effect, loot context, and blank equippable component sections
- `Statement.parse(...)` now retains specific parse errors across nested parser scopes, so valid effects or function calls used as sections keep their ownership diagnostic instead of falling through to a generic `Can't understand this section` fallback
- real `.sk` coverage now also includes statement fallback after failed effect parse through `ambiguous loader syntax`
- locked runtime GameTests now clear Skript variables before and after each body so real `.sk` verification stays isolated across suite order without changing production variable semantics

### Base

- `is empty`
  - representative forms: `%itemstack% is empty`, `%slot% is empty`, `%inventory% is empty`
- `is alive`
  - representative forms: `%entities% are alive`, `%entities% are dead`
- `is named`
  - representative forms: `%entity% is named`, `%itemstack% is named`
- `is silent`
  - representative forms: `%entities% are silent`, `%entities% are not silent`
- `is invulnerable`
  - representative forms: `%entities% are invulnerable`, `%entities% are invincible`, `%entities% are vulnerable`
- `has permission`
  - representative forms: `%players% has permission %strings%`, `%players% does not have permission %strings%`
  - Fabric backend note: checks route through the official LuckPerms API/provider path exposed by LuckPerms Fabric and preserve upstream `skript.*` wildcard fallback semantics
- chance condition
  - representative forms: `chance of 50%`, `chance of 0.25`, `chance of 25% failed`
- generic comparison
  - representative forms: `%objects% is %objects%`
  - current audit note: ambiguous bare item-id equality is not parity-complete yet
- contains
  - representative forms: `%inventories% contain %itemtypes%`, `%strings% contain %strings%`, `%objects% contain %objects%`

### Event and script state

- event cancellation
  - representative forms: `event is cancelled`, `event is not cancelled`
- damage cause filter
  - representative forms: `damage was caused by "fire"`, `damage was not caused by "magic"`
- script load state
  - representative forms: `script "example.sk" is loaded`, `scripts are loaded`

### Server and account state

- banned / IP-banned checks
  - representative forms: `%offlineplayers% are banned`, `%players% are IP-banned`, `"127.0.0.1" is banned`
- online / offline / connected checks
  - representative forms: `%offlineplayers% are online`, `%offlineplayers% are offline`, `%offlineplayers% are connected`
- operator and whitelist checks
  - representative forms: `%offlineplayers% are operators`, `%offlineplayers% are whitelisted`, `the server is whitelisted`, `the server whitelist is enforced`
- PvP state
  - representative forms: `PvP is enabled`, `PvP is disabled in %worlds%`

### Entity, item, and world state

- environment and movement state
  - representative forms: `%entities% are wet`, `%entities% are in water`, `%entities% are in lava`, `%entities% are in a bubble column`, `%entities% are in rain`, `%entities% are frozen`, `%livingentities% are dashing`
- control and visibility state
  - representative forms: `%livingentities% are charging a fireball`, `%entities%' custom names are visible`, `custom name of %entities% is visible`
- item and block state
  - representative forms: `%players% have cooldown on %itemtypes%`, `%players% have %itemtypes% on cooldown`, `%itemtypes% are unbreakable`, `respawn anchors work in %worlds%`, `%itemtypes% are the preferred tool for %blocks%`, `%blocks% are sedated`
- type, fuel, and storage state
  - representative forms: `[the] entity storage of %blocks% is full`, `%itemtypes% are furnace fuel`, `%itemstacks/entities% are of type %itemtypes/entitydatas%`, `%blocks% are resonating`, `%itemtypes% have enchantment glint overridden`, `%itemtypes% are forced to glint`, `%itemtypes% are forced to not glint`
- leash, taming, and damageability state
  - representative forms: `%livingentities% are leashed`, `%livingentities% are tameable`, `%livingentities% are sheared`, `the leash will drop`
- equipment state
  - representative forms: `%livingentities% are holding %itemtypes%`, `%livingentities% are holding %itemtypes% in off-hand`, `%livingentities% are wearing %itemtypes%`
- event-restricted state
  - representative forms: `the egg will hatch`, `the egg won't hatch`, `respawn location is a bed`, `respawn location is a respawn anchor`, `the event-explosion is incendiary`
- despawn and explosion state
  - representative forms: `%livingentities% can despawn on chunk unload`, `%itementities% will naturally despawn`, `%entities% are incendiary`

### Mob-specific state

- allay duplication
  - representative forms: `%livingentities% can duplicate`, `%livingentities% cannot duplicate`
- goat horns
  - representative forms: `%livingentities% have a horn`, `%livingentities% have a left horn`, `%livingentities% have a right horn`, `%livingentities% have both horns`
- creeper ignition
  - representative forms: `creeper %livingentities% is going to explode`, `creeper %livingentities% is in the ignition process`, `creeper %livingentities% is ignited`
- panda state
  - representative forms: `%livingentities% are on their backs`, `%livingentities% are rolling`, `%livingentities% are scared`, `%livingentities% are sneezing`
- other mob state
  - representative forms: `%livingentities% are playing dead`, `%livingentities% are screaming`, `%livingentities% are shivering`

### Imported compatibility checks not yet bootstrapped into the active runtime

- enderman / lid / ticking state
  - representative forms: `%livingentities% have been stared at`, `the lids of %blocks% are open`, `%entities% are ticking`
- custom model / sight / validity state
  - representative forms: `%itemtypes% have custom model data`, `%livingentities% have line of sight to %entities/locations%`, `%entities/scripts% are valid`
- movement / persistence / charge state
  - representative forms: `%livingentities% are dancing`, `%livingentities% are eating`, `%livingentities% are jumping`, `%entities/blocks% are persistent`, `%entities% are charged`, `%itemtypes% are fire resistant`
- position, loading, and ownership checks
  - representative forms: `%entities/blocks% are loaded`, `%entities% are riding`, `%livingentities% are pathfinding`, `%livingentities% are saddled`, `%players% have played before`
- inventory and tooltip checks
  - representative forms: `%inventories% can hold %itemtypes%`, `%itemstacks% are stackable`, `%itemtypes% have [a] hidden tooltip`, `%objects% have glowing text`
- spatial checks
  - representative forms: `%locations/entities% are within %number% blocks around %location%`, `%locations/entities% are within %location% and %location%`, `%entities% can see %entities/locations%`

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
- mixed-runtime event payload additions
  - representative forms: `affected entities`, `barter input`, `consumed item`, `experience cooldown change reason`, `exploded blocks`, `hatching number`, `hatching entity type`, `heal amount`

### Base mapped types and utilities

- mapped core types are available through registered class infos:
  - player
  - world
  - entity
  - location
  - block
  - item stack
  - material
  - inventory
  - vector
  - slot
  - timespan
  - quaternion
  - `%material%` / `%materials%` now resolve again through user-pattern classinfo aliases on the active compatibility path
- random-number helpers
  - representative forms: `a random integer between 1 and 3`, `5 random numbers from 0 to 1`
- random-character / times helpers
  - representative forms: `3 random alphanumeric characters between "0" and "C"`, `3 times`, `once`, `twice`, `thrice`
- amount / inverse helpers
  - representative forms: `amount of %objects%`, `the inverse of %booleans%`
- date / index helpers
  - representative forms: `%dates% formatted as "yyyy-MM-dd"`, `sorted indices of %objects%`, `indices of %objects%`

### Entity and player state

- AI, gravity, and flight state
  - representative forms: `AI of %livingentities%`, `gravity of %entities%`, `flight mode of %players%`
- combat and survival state
  - representative forms: `attack cooldown of %players%`, `last damage of %livingentities%`, `exhaustion of %players%`
- movement and world-interaction state
  - representative forms: `fall distance of %entities%`, `level progress of %players%`
- fire and freezing timers
  - representative forms: `burning time of %entities%`, `maximum burning time of %entities%`, `freeze time of %entities%`, `maximum freeze time of %entities%`
- damage and healing event context
  - representative forms: `attacked`, `victim`, `attacker`, `damager`, `damage`, `final damage`, `damage cause`, `heal reason`, `spawned experience`, `last damage cause of %livingentities%`
- dropped-item ownership and raw identity
  - representative forms: `uuid of the dropped item owner of %itementities%`, `uuid of the dropped item thrower of %itementities%`, `minecraft name of %itemtypes%`
- combat/property follow-up state
  - representative forms: `last attacker of %livingentities%`, `leash holder of %livingentities%`, `xp level of %players%`, `walk speed of %players%`, `flight speed of %players%`
- durability and survival caps
  - representative forms: `maximum durability of %itemtypes/itemstacks/slots%`, `maximum health of %livingentities%`, `maximum item use time of %itemtypes/itemstacks%`, `maximum stack size of %itemtypes/inventories%`, `invulnerability ticks of %livingentities%`

### Inventory and container state

- custom inventories
  - representative forms: `chest inventory named %string% with %number% rows`, `chest inventory with %number% rows`, `chest inventory named %string%`
- player inventories
  - representative forms: `inventory of %players%`, `ender chest of %players%`
- inventory metadata
  - representative forms: `holder of %inventories%`, `amount of rows of %inventories%`, `amount of slots of %inventories%`
- inventory slots and contents
  - representative forms: `slot %numbers% of %inventory%`, `first empty slot in %inventories%`, `items in %inventories%`, `%itemtypes% in %inventories%`
- command and cooldown state
  - representative forms: `command of %blocks/entities%`, `command block command of %blocks/entities%`, `item cooldown of %itemtypes% for %players%`, `%players%' item cooldown for %itemtypes%`

### Imported compatibility expressions not yet bootstrapped into the active runtime

- book and item metadata
  - representative forms: `author of %itemtypes%`, `pages of %itemtypes%`, `title of %itemtypes%`, `custom model data of %itemtypes%`
- item flag metadata
  - representative forms: `item flags of %itemtypes%`, `set item flags of %itemtypes% to %strings%`
- durability and charge state
  - representative forms: `durability of %slots/itemtypes%`, `remaining durability of %itemtypes%`, `charges of %itemtypes%`, `damaged item from %itemtypes%`
- block / brushable / entity item payload
  - representative forms: `hardness of %blocks/itemtypes%`, `brushable item of %blocks%`, `egg of %entities%`
- item transformations and inventory expressions
  - representative forms: `amount of %itemtypes% in %inventories%`, `exact %itemtypes%`, `%itemtypes% with lore %strings%`, `%itemtypes% with hidden tooltip`, `%itemtypes% with custom model data %objects%`, `all items`
- parser/runtime diagnostics
  - representative forms: `last caught runtime errors`, `event-%classinfo%`
- queue construction and traversal
  - representative forms: `a queue`, `a queue of %objects%`, `dequeued %queue%`, `unrolled %queue%`, `start of %queue%`, `end of %queue%`
- reflection, filtering, and recursive helpers
  - representative forms: `%objects% where [<condition>]`, `the function named %string%`, `functions named %strings%`, `the functions from %script%`, `keyed %objects%`, `recursive %objects%`
- loop and aggregate helpers
  - representative forms: `loop-iteration`, `loop-iteration-2`, `%number% percent of %numbers%`, `%strings% repeated %integer% times`, `rounded %numbers%`, `floored %numbers%`, `%numbers% rounded up`, `every %classinfo%`, `all of the %classinfo%`
- entity property and cooldown state
  - representative forms: `active item of %livingentities%`, `owner of %entities%`, `size of %entities%`, `health of %livingentities%`, `remaining air of %entities%`, `portal cooldown of %entities%`, `xp pickup cooldown of %players%`
- block, mob, and timer state
  - representative forms: `age of %entities/blocks%`, `honey level of %blocks%`, `flower of %beehives%`, `jukebox of %allays%`, `arrows stuck in %livingentities%`, `maximum fuse time of %creepers%`, `domestication of %livingentities%`, `break speed of %players%`
- world coordinate and environment state
  - representative forms: `chunk x-coordinate of %chunks%`, `chunk z-coordinate of %chunks%`, `humidity of %blocks%`, `redstone power of %blocks%`
- world location builders and lookups
  - representative forms: `event-location`, `location north of %location%`, `location at x = %number%, y = %number%, and z = %number% [in %world%]`, `location of %locations/blocks/entities/chunks%`
- world metadata
  - representative forms: `sea level of %worlds%`, `seed of %worlds%`, `simulation distance of %worlds%`, `spawn location of %worlds%`

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
- mixed-runtime collection and debug helpers
  - representative forms: `copy %objects% into %objects%`, `sort %objects%`, `sort %objects% in descending order`, `toggle %blocks/booleans%`, `cause exception`
- base entity control
  - `kill %entities%`
  - `feed [the] %players%`
  - `feed [the] %players% by %-number% [beef[s]]`
  - `silence %entities%`
  - `unsilence %entities%`
  - `make %entities% silent`
  - `make %entities% not silent`
  - `make %entities% invulnerable`
  - `make %entities% invincible`
  - `make %entities% vulnerable`
  - `make %livingentities% invisible`
  - `make %livingentities% not visible`
  - `make %livingentities% visible`
  - `make %livingentities% not invisible`
- panda rolling toggle
  - representative forms: `make %livingentities% start rolling`, `force %livingentities% to stop rolling`
- strider shivering toggle
  - representative forms: `make %livingentities% start shivering`, `force %livingentities% to stop shivering`
- syntax-core mutation and diagnostics
  - representative forms: `apply [%-number%] bone meals to %blocks%`, `detonate %entities%`, `make %entities% not despawnable on chunk unload`, `force %itemtypes% to glint`, `replace %strings% in %objects%`, `log %strings% [(to|in) [file[s]] %-strings%]`, `run %task% [async]`, `suppress [the] ... warnings`, `suppress type hints [for] %strings%`, `expand world border of %players/worlds% by/to %number% [over %-timespan%]`
- fishing approach-angle setter

### Imported compatibility effects not yet bootstrapped into the active runtime

- mixed-runtime blocked effect remainder
  - representative forms: `color %itemtypes% %colors%`, `enchant %itemtypes% with %enchantments%`, `equip %livingentities% with %itemtypes%`, `drop %objects%`, `set health of %livingentities% to %number%`, `teleport %entities%`, `wake up %players%`, `launch %itemtypes% as a firework`, `consume elytra boost`, `create explosion`, `grow tree`, `make %entities% visible/invisible`, `clear entity storage of %blocks%`, `insert %entities% into entity storage of %blocks%`, `release entity storage of %blocks%`

- movement and vehicle control
  - representative forms: `make %livingentities% teleport randomly`, `make %livingentities% pathfind towards %livingentity/location%`, `make %entities% ride %entity/entitydata%`, `eject passengers of %entities%`
- server / player control
  - representative forms: `enforce [the] whitelist`, `unenforce [the] whitelist`, `force %players% to respawn`, `allow flight for %players%`, `disallow flight for %players%`
- state and presentation control
  - representative forms: `make command block[s] %blocks% conditional`, `make %objects% have glowing text`, `make %entities/blocks% persistent`, `transform {list::*} with %objects%`, `zombify %livingentities%`, `unzombify %livingentities% after %-timespan%`
- server, world, and UI control
  - representative forms: `ban %offlineplayers/strings% [for %-timespan%] [because %-string%]`, `cancel [the] event`, `cancel [the] drops`, `cancel item use`, `stop server`, `restart server`, `load %worlds%`, `save %worlds%`, `load [the] server icon from %string%`
- player inventory and presentation control
  - representative forms: `open %inventories% for %players%`, `open %itemtypes% for %players%`, `hide %players% from [the] [server] [player] list`, `show %players% in [the] player info list`, `make %players% look at %locations/entities%`, `make %itemtypes% hide/show [the] tooltip`
  - Fabric backend note: `EffOpenInventory` and `FabricInventory` now open through Patbox `sgui`, with the backing `Container` bridged into redirected SGUI slots instead of the earlier vanilla `SimpleMenuProvider` path
- entity and block manipulation
  - representative forms: `make %livingentities% duplicate`, `make %livingentities% ram %entities/locations%`, `make %creepers% explode`, `make %livingentities% swing [their] [main/off] hand`, `push %entities% [in] %-vector%`, `knock %entities% back by %number%`, `break %blocks% naturally [using %-itemtype%]`, `ring %blocks%`, `show %blocks% as %itemtypes% to %players%`
- script / proxy / death-state compatibility effects
  - representative forms: `connect %players% to proxy server %string%`, `transfer %players% to server %string% [on port %number%]`, `keep inventory`, `keep experience`, `make %players% say %strings%`, `reload script %strings%`, `enable script file %strings%`, `disable script file %strings%`, `unload script file %strings%`

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
- `using [[the] experiment] <name>`

The currently supported control-flow section syntax is:

- `if <condition>:`
- `else if <condition>:`
- `else:`
- `parse if <condition>:`
- `else parse if <condition>:`
- `if any:`
- `if all:`
- `else if any:`
- `else if all:`
- `then:`
- `then run:`
- implicit `%condition%:` section headers

The current loader/runtime also supports:

- generic registered `Section` parsing for section-backed trigger items
- shared registered conditional-section loading through `SecIf` plus the adjacent `SecConditional` compatibility path

## What This Document Does Not Guarantee

- Registered event syntax is not automatically runtime-backed.
- Registered syntax is not automatically parity-complete with the original Bukkit implementation.
- Some syntax families are verified only by parser/unit coverage, while others are verified end-to-end by real `.sk` + Fabric GameTest.

## Immediate Known Gaps

- Several imported event classes are now active but still partial, and `EvtEntityBlockChange`, `EvtGrow`, `EvtPlantGrowth`, `EvtPressurePlate`, and `EvtVehicleCollision` still need dedicated Fabric producers.
- Deprecated-unused `PATROL_CAPTAIN` was intentionally dropped instead of emulated.
- Full Stage 8 parity audit is not complete yet.
