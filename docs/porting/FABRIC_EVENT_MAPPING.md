# Fabric Event Mapping

Last condensed: 2026-03-12
Last full verification: 2026-03-12

## Snapshot

- Audited `Evt*.java` classes: `45`
- Runtime-backed/live-hooked `Evt*.java` classes: `24`
- Synthetic-handle-only or partial `Evt*.java` classes: `16`
- Non-runtime/manual/scheduled/internal `Evt*.java` classes: `5`
- Exact-path parity in `ch/njol/skript/events` against upstream `e6ec744`: upstream `43 / 43` present, plus Fabric-only `EvtBreeding` and `EvtBucketCatch`
- Legacy tracked live rows below are runtime-backed but not exhaustive.
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks` passed with `262 / 262`

## Evt Audit Basis

- Class classification is conservative:
  - a class stays in the `synthetic-handle-only` bucket if it still exposes registered patterns without a real runtime caller, even when one sub-pattern already has a live path
- `non-runtime/manual/scheduled/internal` means the syntax is intentionally driven by scheduler, loader, or Skript lifecycle code instead of a Minecraft/Fabric hook

## Runtime-Backed `Evt*.java`

`24` classes are backed by real runtime entrypoints today:

- `EvtBeaconEffect`
- `EvtBeaconToggle`
- `EvtBookEdit`
- `EvtBookSign`
- `EvtBreeding`
- `EvtBucketCatch`
- `EvtClick`
- `EvtCommand`
- `EvtDamage`
- `EvtEntity`
- `EvtEntityShootBow`
- `EvtEntityTransform`
- `EvtExperienceChange`
- `EvtExperienceSpawn`
- `EvtGrow`
- `EvtHealing`
- `EvtLevel`
- `EvtMove`
- `EvtPlantGrowth`
- `EvtPlayerChunkEnter`
- `EvtPlayerCommandSend`
- `EvtResourcePackResponse`
- `EvtSpectate`
- `EvtTeleport`

## Synthetic Or Partial `Evt*.java`

`16` classes are still unhooked, helper-driven, or only partially live-backed:

| Class | Status | Evidence |
| --- | --- | --- |
| `EvtBlock` | partial only; real callers exist for `break` and `place`, but `burn`/`fade`/`form`/`drop` still have no live caller | `SkriptFabricEventBridge.dispatchBlockBreak(...)`, `SkriptFabricEventBridge.dispatchBlockPlace(...)`, helper-dispatch coverage in `SkriptFabricMixedRuntimeBackfillGameTest.activeMixedRuntimeEventsExecuteRealScript(...)` |
| `EvtEntityBlockChange` | compat bridge exists, but no runtime caller | `SkriptFabricEventBridge.dispatchEntityBlockChange(...)`, `SkriptFabricEventGameTest.entityBlockChangeCompatBridgeExecutesLoadedScript(...)` |
| `EvtEntityTarget` | handle-only; no runtime bridge caller | `EvtEntityTarget.getEventClasses()`, `EventCompatibilityTest.entityTarget...` unit coverage only |
| `EvtFirework` | handle-only; no runtime bridge caller | `EvtFirework.getEventClasses()`, `EventCompatibilityTest.firework...` unit coverage only |
| `EvtFirstJoin` | handle-only; no runtime bridge caller | `EvtFirstJoin.getEventClasses()`, `FabricPlayerEventHandles.FirstJoin` has no dispatch site |
| `EvtGameMode` | handle-only; no runtime bridge caller | `EvtGameMode.getEventClasses()`, `FabricEventCompatHandles.GameMode` has no dispatch site |
| `EvtHarvestBlock` | handle-only; no runtime bridge caller | `EvtHarvestBlock.getEventClasses()`, `FabricEventCompatHandles.HarvestBlock` has no dispatch site |
| `EvtItem` | partial only; `SPAWN` is live from entity load, other item actions still lack live callers | `SkriptFabricEventBridge.dispatchEntityLoad(...)`, `SkriptFabricEventBridge.dispatchCompatItem(...)`, helper-dispatch coverage in `SkriptFabricMixedRuntimeBackfillGameTest.activeMixedRuntimeEventsExecuteRealScript(...)` |
| `EvtLeash` | handle-only; no live bridge caller | `EvtLeash.getEventClasses()`, helper-only backfill in `custom_context_backfill.sk` / `SkriptFabricMixedRuntimeBackfillGameTest` |
| `EvtMoveOn` | handle-only; no runtime bridge caller | `EvtMoveOn.getEventClasses()`, `FabricEventCompatHandles.MoveOn` has no dispatch site |
| `EvtPlayerArmorChange` | handle-only; no runtime bridge caller | `EvtPlayerArmorChange.getEventClasses()`, `FabricEventCompatHandles.PlayerArmorChange` has no dispatch site |
| `EvtPortal` | handle-only; no runtime bridge caller | `EvtPortal.getEventClasses()`, `FabricEventCompatHandles.Portal` has no dispatch site |
| `EvtPressurePlate` | compat bridge exists, but no runtime caller | `SkriptFabricEventBridge.dispatchPressurePlate(...)`, `SkriptFabricEventGameTest.pressurePlateCompatBridgeExecutesLoadedScript(...)` |
| `EvtVehicleCollision` | compat bridge exists, but no runtime caller | `SkriptFabricEventBridge.dispatchVehicleCollision(...)`, `SkriptFabricEventGameTest.vehicleCollisionCompatBridgeExecutesLoadedScript(...)` |
| `EvtWeatherChange` | handle-only; no runtime bridge caller | `EvtWeatherChange.getEventClasses()`, `FabricEventCompatHandles.WeatherChange` has no dispatch site |
| `EvtWorld` | handle-only; no runtime bridge caller | `EvtWorld.getEventClasses()`, `FabricEventCompatHandles.World` has no dispatch site |

## Non-Runtime / Manual / Scheduled / Internal `Evt*.java`

`5` classes are intentionally not Minecraft/Fabric hook-backed:

| Class | Driver | Evidence |
| --- | --- | --- |
| `EvtAtTime` | scheduled tick handle | `EvtAtTime.check(...)` on `FabricScheduledTickHandle`, `SkriptFabricEventBridge.dispatchServerTick(...)` |
| `EvtPeriodical` | scheduled tick handle | `EvtPeriodical.check(...)` on `FabricScheduledTickHandle`, `SkriptFabricEventBridge.dispatchServerTick(...)` |
| `EvtRealTime` | Java timer + manual trigger execution | `EvtRealTime.schedule(...)`, `EvtRealTime.execute(...)` |
| `EvtScript` | script load/unload lifecycle | `EvtScript.postLoad()`, `EvtScript.unload()` |
| `EvtSkript` | Skript start/stop lifecycle | `EvtSkript.onSkriptStart()`, `EvtSkript.onSkriptStop()` |

## Active Rows

| Syntax | Backing |
| --- | --- |
| `on gametest` | GameTest helper injection |
| `on server tick` | `ServerTickEvents.END_SERVER_TICK` |
| `on block break` | `PlayerBlockBreakEvents.AFTER` |
| `on use block` | `UseBlockCallback.EVENT` |
| `on use entity` | `UseEntityCallback.EVENT` |
| `on use item` | `UseItemCallback.EVENT` |
| `on attack entity` | `AttackEntityCallback.EVENT` |
| `on damage` | `ServerLivingEntityEvents.ALLOW_DAMAGE` |
| `on breeding` | `Animal.finalizeSpawnChildFromBreeding` mixin path |
| `on bucket catch` | `Bucketable.bucketMobPickup(...)` mixin path |
| `on love mode enter` | `Animal.setInLove(...)` transition |
| `on brewing start` | `BrewingStandBlockEntity.serverTick` start branch |
| `on brewing complete` | `BrewingStandBlockEntity.serverTick` brew completion |
| `on brewing fuel` | `BrewingStandBlockEntity.serverTick` fuel consume point |
| `on entity potion effect` | `LivingEntity` add/update/remove callbacks plus cause tracking |
| `on fishing` | `FishingHook` lifecycle mixins |
| `on loot generate` | `LootTable#getRandomItems(LootContext)` return path |
| `on player input` | `ServerGamePacketListenerImpl.handlePlayerInput` |
| `on resource pack response` | `ServerCommonPacketListenerImpl.handleResourcePackResponse` |
| `on player move` | `ServerGamePacketListenerImpl.handleMovePlayer` |
| `on player chunk enter` | `ServerGamePacketListenerImpl.handleMovePlayer` chunk boundary check |
| `on command` | `Commands.performPrefixedCommand(CommandSourceStack, String)` |
| `on teleport` | `Entity.teleportTo(...)` mixin path |
| `on player start/swap/stop spectating` | `ServerPlayer.setCamera(Entity)` mixin path |
| `on level change` | `ServerPlayer.giveExperienceLevels(int)` delta bridge |
| `on experience decrease` | `ServerPlayer.giveExperiencePoints(int)` delta bridge |
| `on sending of the server command list` | `Commands.sendCommands(ServerPlayer)` |
| `on fuel burn` | `AbstractFurnaceBlockEntity.serverTick` fuel-ignite path |
| `on smelting start` | `AbstractFurnaceBlockEntity.serverTick` cook-start path |
| `on furnace smelt` | `AbstractFurnaceBlockEntity.serverTick` recipe-complete path |
| `on furnace extract` | `FurnaceResultSlot.checkTakeAchievements` |

## Open Parity Note

- Remaining cross-cutting gap is not limited to dispatch:
  - ambiguous bare item-id compare, for example `event-item is wheat`
  - `Evt*.java` exact-path parity is complete against upstream, but `16` local event classes still depend on synthetic or partial backends
