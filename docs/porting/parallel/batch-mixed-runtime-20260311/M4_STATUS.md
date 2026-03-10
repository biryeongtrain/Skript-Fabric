# M4 Status

## Scope

- `expressions/**` event-payload/combat/entity bundle only
- narrow tests for assigned expressions

## Assigned Targets

- `20` expressions:
  - `ExprArrowKnockbackStrength`
  - `ExprArrowPierceLevel`
  - `ExprBarterDrops`
  - `ExprBeaconEffects`
  - `ExprBeaconRange`
  - `ExprBeaconTier`
  - `ExprClicked`
  - `ExprDrops`
  - `ExprDropsOfBlock`
  - `ExprEntityAttribute`
  - `ExprExplosionBlockYield`
  - `ExprExplosionYield`
  - `ExprExplosiveYield`
  - `ExprFertilizedBlocks`
  - `ExprFireworkEffect`
  - `ExprHanging`
  - `ExprLastDeathLocation`
  - `ExprLastSpawnedEntity`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`

## Landed Classes

- expressions `20`:
  - `ExprArrowKnockbackStrength`
  - `ExprArrowPierceLevel`
  - `ExprBarterDrops`
  - `ExprBeaconEffects`
  - `ExprBeaconRange`
  - `ExprBeaconTier`
  - `ExprClicked`
  - `ExprDrops`
  - `ExprDropsOfBlock`
  - `ExprEntityAttribute`
  - `ExprExplosionBlockYield`
  - `ExprExplosionYield`
  - `ExprExplosiveYield`
  - `ExprFertilizedBlocks`
  - `ExprFireworkEffect`
  - `ExprHanging`
  - `ExprLastDeathLocation`
  - `ExprLastSpawnedEntity`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`
- minimal compat glue:
  - `ExpressionHandleSupport`
  - `FabricFireworkEffect`
  - expanded `FabricEventCompatHandles` payload records for `PiglinBarter`, `Explosion`, `BlockFertilize`, `PlayerRespawn`
  - expanded `FabricEffectEventHandles` payload classes for `PlayerRespawn`, `EntityDeath`, `ExplosionPrime`, `HangingBreak`, `HangingPlace`
- targeted tests:
  - `ExpressionMixedRuntimeM4CompatibilityTest`
  - `ExpressionEventPayloadBundleCompatibilityTest`

## Runtime-Eligible Classes

- import-ready after coordinator bootstrap wiring:
  - `ExprArrowKnockbackStrength`
  - `ExprArrowPierceLevel`
  - `ExprBarterDrops`
  - `ExprBeaconEffects`
  - `ExprBeaconRange`
  - `ExprBeaconTier`
  - `ExprClicked`
  - `ExprDrops`
  - `ExprDropsOfBlock`
  - `ExprEntityAttribute`
  - `ExprExplosionBlockYield`
  - `ExprExplosionYield`
  - `ExprExplosiveYield`
  - `ExprFertilizedBlocks`
  - `ExprFireworkEffect`
  - `ExprHanging`
  - `ExprLastDeathLocation`
  - `ExprLastSpawnedEntity`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`

## Bootstrap Registrations Needed

- `SkriptFabricBootstrap` force-init / registration pass for the `20` expressions above
- classinfo prerequisites before live parsing:
  - `projectile`
  - `attributetype`
  - `fireworktype`
  - `fireworkeffect`
- live runtime producers still needed before several event expressions become user-visible in GameTests:
  - death payload handle for `ExprDrops`
  - respawn payload handle for `ExprRespawnLocation` / `ExprRespawnReason`
  - explosion-prime payload handle for `ExprExplosionYield`
  - hanging break/place payload handle for `ExprHanging`
  - fertilize payload handle for `ExprFertilizedBlocks`

## Targeted Tests

- `./gradlew test --tests 'ch.njol.skript.expressions.*CompatibilityTest' --rerun-tasks`
- result: passed

## Blockers

- no `SkriptFabricBootstrap.java` edits in lane, so everything above remains import-only until coordinator registration
- several event-payload expressions currently rely on newly added compat payload classes, but the live Fabric runtime does not dispatch those payloads yet:
  - `ExprDrops`
  - `ExprExplosionYield`
  - `ExprFertilizedBlocks`
  - `ExprHanging`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`
- `ExprClicked` lands only the click-event block/entity surface, not the upstream inventory-click variants
- `ExprLastDeathLocation` currently resolves only online players from the active server context
- `ExprBeaconEffects`, `ExprBeaconRange`, and parts of `ExprExplosiveYield` use reflection against Minecraft internals and need coordinator/runtime confirmation before claiming parity

## Merge Note

- likely conflict files:
  - `src/main/java/ch/njol/skript/events/FabricEventCompatHandles.java`
  - `src/main/java/ch/njol/skript/effects/FabricEffectEventHandles.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionEventPayloadBundleCompatibilityTest.java`
- lane avoided bootstrap edits and left coordinator-owned runtime activation / GameTest follow-up untouched
