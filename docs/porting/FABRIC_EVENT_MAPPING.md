# Fabric Event Mapping

Last updated: 2026-03-10

This file tracks the current event bridge status.
It is not a parity claim.

Current bridge counts:

- active event mapping rows: `33`
- latest runtime verification: `230 / 230` Fabric GameTests on 2026-03-10

## Current bridge slice

| Active syntax | Source hook | Mojang/Fabric backing | Status | Notes |
| --- | --- | --- | --- | --- |
| `on gametest` | test-only manual dispatch | `GameTestHelper` handle injected by GameTest code | active | verification-only hook for `.sk` runtime tests |
| `on server tick` | Fabric lifecycle event | `ServerTickEvents.END_SERVER_TICK` | active | first automatic Fabric -> Skript runtime event bridge |
| `on block break` | Fabric player interaction event | `PlayerBlockBreakEvents.AFTER` | active | carries Mojang block-break context into Skript runtime |
| `on beacon effect` | mixin callback | `BeaconBlockEntity` effect application path | active | active through the beacon block-entity effect-apply hook; supports primary/secondary headers plus effect-type filters |
| `on beacon toggle` | mixin callback | `BeaconBlockEntity` activation/deactivation path | active | active through the beacon block-entity toggle hook |
| `on block` | compat bridge dispatch | current `PlayerBlockBreakEvents.AFTER` compat producer | active | partial; current active backing only covers break/mining-style forms |
| `on book edit` | mixin callback | `ServerGamePacketListenerImpl.updateBookContents` | active | dispatches before/after book item payloads on writable-book edits |
| `on book sign` | mixin callback | `ServerGamePacketListenerImpl.signBook` | active | dispatches the signed book payload after sign completion |
| `on click` | Fabric interaction callbacks / compat bridge | left/right interaction bridge into `FabricEventCompatHandles.Click` | active | covers left/right click plus clicked target/tool filters |
| `on breeding` | mixin callback | `Animal.finalizeSpawnChildFromBreeding` | active | verified by real `.sk` + GameTest, including offspring entity-type filter forms and non-empty breeding `event-item`; carries mother/father/offspring plus breeder as both `breeder` and `event-player` when available, and exposes the captured bred-with item as `event-item`; exact ambiguous bare-id compare forms such as `event-item is wheat` remain tracked under the Stage 8 base-compare audit rather than as an event-dispatch gap |
| `on bucket catch` | mixin callback | `Bucketable.bucketMobPickup(Player, InteractionHand, T)` | active | verified by real `.sk` + GameTest, including entity-filter forms and nested `if future event-item ...` sections; carries bucketed entity, original bucket, future entity bucket, and bucketing player into the runtime |
| `on love mode enter` | mixin callback | `Animal.setInLove(Player)` / `Animal.setInLoveTime(int)` zero-to-positive transition | active | verified by real `.sk` + GameTest; carries entering animal as `event-entity` and loving player as `event-player` when available |
| `on brewing start` | mixin callback | `BrewingStandBlockEntity.serverTick` start-brew branch after `ingredient` assignment | active | verified by real `.sk` + GameTest; `brewing time of event-block` changer persists from the same brewing-start tick |
| `on brewing complete` | mixin callback | `BrewingStandBlockEntity.serverTick` successful `doBrew` path | active | verified by real `.sk` + GameTest, including item and potion-effect filter forms; exposes mutable `brewing results` and brewing-stand block context |
| `on brewing fuel` | mixin callback | `BrewingStandBlockEntity.serverTick` fuel-consume point | active | verified by real `.sk` + GameTest, including fuel item-filter forms; carries brewing-stand block context and consume state |
| `on damage` | Fabric living-entity event | `ServerLivingEntityEvents.ALLOW_DAMAGE` | active | carries damaged entity and Mojang `DamageSource` payload into Skript runtime |
| `on attack entity` | Fabric player interaction event | `AttackEntityCallback.EVENT` | active | carries attacked entity and player payload into Skript runtime |
| `on entity potion effect [modification]` | mixin callback | `LivingEntity` effect add/update/remove callbacks plus cause tracking from consumables, block-entities, command, projectile/cloud, entity removal, Skript-driven effect mutations, and attack/source-null paths | active | verified by real `.sk` + GameTest for added/changed/removed/cleared action headers, registry-backed type-filter forms (`bare id` defaults to `minecraft`, explicit namespaces are preserved, and `minecraft:poison` is live-covered), and `due to %potioncauses%` filtering for every retained supported cause value: `potion drink`, `area effect cloud`, `food`, `milk`, `beacon`, `conduit`, `command`, `attack`, `arrow`, `unknown`, `potion splash`, `totem`, `wither rose`, `conversion`, `axolotl`, `warden`, `spider spawn`, `villager trade`, `expiration`, `dolphin`, `turtle helmet`, `illusion`, `plugin`, and `death`; deprecated-unused `PATROL_CAPTAIN` was intentionally dropped from the supported surface |
| `on entity` | compat bridge dispatch | entity lifecycle bridge on load + post-death dispatch | active | partial; current active backing only covers spawn/death lifecycle forms |
| `on entity transform` | compat bridge dispatch | compat transform producer into `FabricEventCompatHandles.EntityTransform` | active | active with coarse string reason support |
| `on experience spawn` | compat bridge dispatch | experience-orb load bridge | active | active for experience-orb spawn forms |
| `on fishing` | mixin callback | `FishingHook` constructor / hit / state / retrieve lifecycle | active | verified by real `.sk` + GameTest for plain `on fishing` plus original cast/caught/entity-hook/in-ground/lured/bite/escape/reel-in/state-change variants; carries hook entity, state, and event entity when available |
| `on fuel burn` | mixin callback | `AbstractFurnaceBlockEntity.serverTick` fuel-ignite path | active | verified by real `.sk` + GameTest, including item-filter forms; carries furnace block, source item, and fuel item |
| `on smelting start` | mixin callback | `AbstractFurnaceBlockEntity.serverTick` first cooking-progress increment | active | verified by real `.sk` + GameTest, including item-filter forms; `total cook time` changer now persists through the same furnace tick |
| `on furnace smelt` | mixin callback | `AbstractFurnaceBlockEntity.serverTick` after successful recipe craft | active | verified by real `.sk` + GameTest, including item-filter forms; carries smelt result item |
| `on furnace extract` | mixin callback | `FurnaceResultSlot.checkTakeAchievements` | active | verified by real `.sk` + GameTest, including item-filter forms; carries extracting player and extracted result count |
| `on healing` | mixin callback | `LivingEntity#heal` callback bridge | active | active with coarse reason text and heal amount payload |
| `on item` | compat bridge dispatch | item-entity load bridge into `FabricEventCompatHandles.Item` | active | partial; current active backing only covers item-spawn forms |
| `on loot generate` | mixin callback | `LootTable#getRandomItems(LootContext)` return path | active | verified by real `.sk` + GameTest; mutable generated loot list is applied back to the live chest-fill path |
| `on player input` | mixin callback | `ServerGamePacketListenerImpl.handlePlayerInput` | active | verified by real `.sk` + GameTest for plain `on player input` and original toggle/press/release forms, including `input key` any-key headers and key-filter variants |
| `on use block` | Fabric player interaction event | `UseBlockCallback.EVENT` | active | carries clicked block and player payload into Skript runtime |
| `on use entity` | Fabric player interaction event | `UseEntityCallback.EVENT` | active | carries interacted entity and player payload into Skript runtime |
| `on use item` | Fabric player interaction event | `UseItemCallback.EVENT` | active | carries held item and player payload into Skript runtime |

## Known unbacked or partial event syntax

- Missing event families from the currently tracked original package-local Bukkit surface:
  - none
- Partial parity in existing families:
  - `EvtBlock` is active but currently break-backed only
  - `EvtItem` is active but currently spawn-backed only
  - `EvtEntity` is active but currently lifecycle spawn/death-backed only
  - `EvtEntityTransform` and `EvtHealing` are active but still use coarse reason support
  - dedicated live producers are still missing for `EvtEntityBlockChange`, `EvtGrow`, `EvtPlantGrowth`, `EvtPressurePlate`, and `EvtVehicleCollision`
  - one cross-cutting Stage 8 gap remains outside event mapping in base generic-compare handling for ambiguous bare item ids such as `event-item is wheat`
- Deprecated residue intentionally not carried over:
  - `PATROL_CAPTAIN` potion cause (modern vanilla/Paper no longer uses it)

## Bukkit parity mapping

No Bukkit event class has been declared parity-complete yet.
Stage 5 is only started when a real Fabric event enters the Skript runtime without manual dispatch.
