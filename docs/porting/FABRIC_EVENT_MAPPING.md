# Fabric Event Mapping

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Snapshot

- Active event rows: `22`
- No tracked event family is currently registered without a live Fabric/Mojang backend.
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
  - `./gradlew build --rerun-tasks` passed

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
| `on fuel burn` | `AbstractFurnaceBlockEntity.serverTick` fuel-ignite path |
| `on smelting start` | `AbstractFurnaceBlockEntity.serverTick` cook-start path |
| `on furnace smelt` | `AbstractFurnaceBlockEntity.serverTick` recipe-complete path |
| `on furnace extract` | `FurnaceResultSlot.checkTakeAchievements` |

## Open Parity Note

- Remaining cross-cutting gap is not an event-dispatch gap:
  - ambiguous bare item-id compare, for example `event-item is wheat`
