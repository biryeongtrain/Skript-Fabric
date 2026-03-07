# Fabric Event Mapping

Last updated: 2026-03-06

This file tracks the current event bridge status.
It is not a parity claim.

## Current bridge slice

| Active syntax | Source hook | Mojang/Fabric backing | Status | Notes |
| --- | --- | --- | --- | --- |
| `on gametest` | test-only manual dispatch | `GameTestHelper` handle injected by GameTest code | active | verification-only hook for `.sk` runtime tests |
| `on server tick` | Fabric lifecycle event | `ServerTickEvents.END_SERVER_TICK` | active | first automatic Fabric -> Skript runtime event bridge |
| `on block break` | Fabric player interaction event | `PlayerBlockBreakEvents.AFTER` | active | carries Mojang block-break context into Skript runtime |
| `on brewing fuel` | mixin callback | `BrewingStandBlockEntity.serverTick` fuel-consume point | active | carries brewing-stand block context and consume state |
| `on damage` | Fabric living-entity event | `ServerLivingEntityEvents.ALLOW_DAMAGE` | active | carries damaged entity and Mojang `DamageSource` payload into Skript runtime |
| `on attack entity` | Fabric player interaction event | `AttackEntityCallback.EVENT` | active | carries attacked entity and player payload into Skript runtime |
| `on fishing` | mixin callback | `FishingHook.retrieve` / fishing hook state | active | carries hook entity plus lure/open-water state |
| `on player input` | mixin callback | `ServerGamePacketListenerImpl.handlePlayerInput` | active | carries current and previous Mojang `Input` state |
| `on use block` | Fabric player interaction event | `UseBlockCallback.EVENT` | active | carries clicked block and player payload into Skript runtime |
| `on use entity` | Fabric player interaction event | `UseEntityCallback.EVENT` | active | carries interacted entity and player payload into Skript runtime |
| `on use item` | Fabric player interaction event | `UseItemCallback.EVENT` | active | carries held item and player payload into Skript runtime |

## Bukkit parity mapping

No Bukkit event class has been declared parity-complete yet.
Stage 5 is only started when a real Fabric event enters the Skript runtime without manual dispatch.
