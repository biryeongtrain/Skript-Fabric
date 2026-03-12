# Fabric Event Mapping

Last condensed: 2026-03-12
Last full verification: 2026-03-12

## Snapshot

- Active event rows: `41`
- Tracked live rows below are runtime-backed; other implemented event syntaxes may still be synthetic-handle-only.
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests ch.njol.skript.expressions.ExpressionEventContextBundleCompatibilityTest --tests ch.njol.skript.conditions.ConditionSyntaxS1CompatibilityTest` passed
  - `build/junit.xml` recorded `respawn_producer_executes_real_script`, `piglin_barter_event_executes_real_script`, and `player_egg_throw_producer_executes_real_script` as passing GameTests
  - full `./gradlew runGameTest --rerun-tasks` is currently blocked only by the existing `ExprNumbers` GameTest failure; `267 / 268` passed

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
| `on entity block change` | `Sheep.ate()` mixin path |
| `on gamemode change` | `ServerPlayerGameMode.changeGameModeForPlayer(GameType)` |
| `on respawn` | `PlayerList.respawn(ServerPlayer, boolean, Entity.RemovalReason)` |
| `on teleport` | `Entity.teleportTo(...)` mixin path |
| `on player start/swap/stop spectating` | `ServerPlayer.setCamera(Entity)` mixin path |
| `on weather change` | `ServerLevel.setWeatherParameters(...)` |
| `on pressure plate` | `BasePressurePlateBlock.onEntityCollision` and `TripWireBlock.onEntityCollision` |
| `on vehicle collision` | `AbstractMinecart.push(Entity)` non-minecart path |
| `on firework explosion` | `FireworkRocketEntity.explodeAndRemove(ServerLevel)` |
| `on unleash` | `Leashable.dropLeash(Entity, boolean, boolean)` mixin path |
| `on player egg throw` | `ThrownEgg.onHit(HitResult)` mixin path |
| `on piglin barter` | `PiglinAi.stopHoldingOffHandItem(ServerLevel, Piglin, boolean)` mixin path |
| `on level change` | `ServerPlayer.giveExperienceLevels(int)` delta bridge |
| `on experience decrease` | `ServerPlayer.giveExperiencePoints(int)` delta bridge |
| `on sending of the server command list` | `Commands.sendCommands(ServerPlayer)` |
| `on fuel burn` | `AbstractFurnaceBlockEntity.serverTick` fuel-ignite path |
| `on smelting start` | `AbstractFurnaceBlockEntity.serverTick` cook-start path |
| `on furnace smelt` | `AbstractFurnaceBlockEntity.serverTick` recipe-complete path |
| `on furnace extract` | `FurnaceResultSlot.checkTakeAchievements` |

## Open Parity Note

- `Evt*.java` runtime audit:
  - runtime-backed: `33 / 45`
  - synthetic/partial: `7 / 45`
  - non-runtime/manual: `5 / 45`
- Remaining synthetic/partial event syntax focus:
  - `EvtBlock`
  - `EvtItem`
  - `EvtEntityTarget`
  - `EvtFirstJoin`
  - `EvtHarvestBlock`
  - `EvtLeash` (`unleash` live; `leash` and `player unleash` remain partial)
  - `EvtMoveOn`
  - `EvtPlayerArmorChange`
  - `EvtPortal`
  - `EvtWorld`
- Remaining cross-cutting gap is not limited to dispatch:
  - ambiguous bare item-id compare, for example `event-item is wheat`
