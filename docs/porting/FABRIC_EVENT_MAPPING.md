# Fabric Event Mapping

Last condensed: 2026-03-13
Last full verification: 2026-03-13

## Snapshot

- Active event rows: `75`
- Tracked live rows below are runtime-backed; other implemented event syntaxes may still be synthetic-handle-only.
- Latest verification:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --tests org.skriptlang.skript.fabric.runtime.WorldLifecycleRuntimeTest --tests org.skriptlang.skript.fabric.runtime.ItemLifecycleRuntimeTest --tests org.skriptlang.skript.fabric.runtime.InventoryMoveRuntimeTest --warning-mode none --console=plain` passed
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.HarvestBlockRuntimeTest --warning-mode none --console=plain` passed
  - `./gradlew runGameTest --rerun-tasks --warning-mode none --console=plain` completed `309` GameTests with only the known baseline failure `skript_fabric_expression_cycle_isyntax1game_test_expr_numbers_executes_real_script`

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
| `on area cloud effect` | `AreaEffectCloud.tick(ServerLevel, BlockPos, BlockState)` apply-effects path |
| `on breeding` | `Animal.finalizeSpawnChildFromBreeding` mixin path |
| `on bucket catch` | `Bucketable.bucketMobPickup(...)` mixin path |
| `on love mode enter` | `Animal.setInLove(...)` transition |
| `on brewing start` | `BrewingStandBlockEntity.serverTick` start branch |
| `on brewing complete` | `BrewingStandBlockEntity.serverTick` brew completion |
| `on brewing fuel` | `BrewingStandBlockEntity.serverTick` fuel consume point |
| `on block fertilize` | `BoneMealItem.applyGrowth(...)` mixin path |
| `on block burn` | `FireBlock.checkBurnOut(Level, BlockPos, int, RandomSource, int, Direction)` |
| `on block fade` | `IceBlock.randomTick(BlockState, ServerLevel, BlockPos, RandomSource)` |
| `on block form` | `ServerLevel.setBlockAndUpdate(BlockPos, BlockState)` form-marked path |
| `on block drop` | `ServerPlayerGameMode.destroyBlock(BlockPos)` dropped-resource path |
| `on entity potion effect` | `LivingEntity` add/update/remove callbacks plus cause tracking |
| `on fishing` | `FishingHook` lifecycle mixins |
| `on loot generate` | `LootTable#getRandomItems(LootContext)` return path |
| `on player input` | `ServerGamePacketListenerImpl.handlePlayerInput` |
| `on resource pack response` | `ServerCommonPacketListenerImpl.handleResourcePackResponse` |
| `on player move` | `ServerGamePacketListenerImpl.handleMovePlayer` |
| `on player chunk enter` | `ServerGamePacketListenerImpl.handleMovePlayer` chunk boundary check |
| `on command` | `Commands.performPrefixedCommand(CommandSourceStack, String)` |
| `on entity block change` | `Sheep.ate()` mixin path |
| `on [entity] target` / `on [entity] un-target` | `Mob.setTarget(LivingEntity)` mixin path |
| `on gamemode change` | `ServerPlayerGameMode.changeGameModeForPlayer(GameType)` |
| `on first join` | `PlayerList.placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie)` |
| `on armor change` / slot-specific armor change | `LivingEntity.onEquipItem(EquipmentSlot, ItemStack, ItemStack)` |
| `on respawn` | `PlayerList.respawn(ServerPlayer, boolean, Entity.RemovalReason)` |
| `on teleport` | `Entity.teleportTo(...)` mixin path |
| `on player start/swap/stop spectating` | `ServerPlayer.setCamera(Entity)` mixin path |
| `on [player] portal` / `on entity portal` | `Entity.handlePortal()` mixin path |
| `on weather change` | `ServerLevel.setWeatherParameters(...)` |
| `on world saving` | `ServerLevel.save(ProgressListener, boolean, boolean)` |
| `on (step|walk) (on|over) %itemtypes%` | `ServerGamePacketListenerImpl.handleMovePlayer` accepted-move path with support-block delta check |
| `on pressure plate` | `BasePressurePlateBlock.onEntityCollision` and `TripWireBlock.onEntityCollision` |
| `on vehicle collision` | `AbstractMinecart.push(Entity)` non-minecart path |
| `on firework explosion` | `FireworkRocketEntity.explodeAndRemove(ServerLevel)` |
| `on explode` | `ServerExplosion.explode()` mixin path with mutable exploded blocks / yield feedback |
| `on explosion prime` | `Creeper.explodeCreeper()` mixin path with mutable radius feedback |
| `on leash` / `on player leashing` | `Leashable.setLeashedTo(Entity, Entity, boolean)` mixin path |
| `on unleash` / `on player unleashing` | `Leashable.dropLeash(Entity, boolean, boolean)` mixin path |
| `on dispense` | `DispenserBlock.dispenseFrom(ServerLevel, BlockState, BlockPos)` |
| `on drop` / `on entity drop` | `ServerPlayer.drop(ItemStack, boolean, boolean)` and `Entity.spawnAtLocation(ServerLevel, ItemStack)` |
| `on prepare craft` | `CraftingMenu.slotsChanged(Container)` |
| `on craft` | `ResultSlot.onTake(Player, ItemStack)` |
| `on pickup` / `on entity pickup` | `ItemEntity.playerTouch(Player)` and `LivingEntity.onItemPickup(ItemEntity)` |
| `on consume` | `ItemStack.finishUsingItem(Level, LivingEntity)` |
| `on item despawn` | `ItemEntity.tick()` age-expiry path |
| `on item merge` | `ItemEntity.mergeWithNeighbours()` |
| `on inventory item move` | `HopperBlockEntity.ejectItems(Level, BlockPos, HopperBlockEntity)` transfer path |
| `on stonecutting` | `StonecutterMenu.quickMoveStack(Player, int)` |
| `on player egg throw` | `ThrownEgg.onHit(HitResult)` mixin path |
| `on piglin barter` | `PiglinAi.stopHoldingOffHandItem(ServerLevel, Piglin, boolean)` mixin path |
| `on level change` | `ServerPlayer.giveExperienceLevels(int)` delta bridge |
| `on experience decrease` | `ServerPlayer.giveExperiencePoints(int)` delta bridge |
| `on player experience cooldown change` | `ExperienceOrb.playerTouch(Player)` pickup-delay delta |
| `on sending of the server command list` | `Commands.sendCommands(ServerPlayer)` |
| `on fuel burn` | `AbstractFurnaceBlockEntity.serverTick` fuel-ignite path |
| `on smelting start` | `AbstractFurnaceBlockEntity.serverTick` cook-start path |
| `on furnace smelt` | `AbstractFurnaceBlockEntity.serverTick` recipe-complete path |
| `on furnace extract` | `FurnaceResultSlot.checkTakeAchievements` |
| `on world initialization` | `MinecraftServer.createLevels(ChunkProgressListener)` one-shot mixin path |
| `on world unloading` | `ServerWorldEvents.UNLOAD` |
| `on world loading` | `ServerWorldEvents.LOAD` |

## Open Parity Note

- `Evt*.java` runtime audit:
  - runtime-backed: `48 / 53`
  - synthetic/partial: `0 / 53`
  - non-runtime/manual: `5 / 53`
- Runtime-backed closure is complete for event-hook families; only non-runtime/manual `Evt*.java` remain outside the live bucket.
- Remaining event-facing synthetic alias:
  - `gametest hanging break`
- Remaining cross-cutting gap is not limited to dispatch:
  - ambiguous bare item-id compare, for example `event-item is wheat`
