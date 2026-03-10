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

- mixed-runtime registered in `SkriptFabricAdditionalSyntax` in this lane:
  - `ExprArrowKnockbackStrength`
  - `ExprArrowPierceLevel`
  - `ExprBarterDrops`
  - `ExprClicked`
  - `ExprDrops`
  - `ExprExplosionBlockYield`
  - `ExprExplosionYield`
  - `ExprExplosiveYield`
  - `ExprFertilizedBlocks`
  - `ExprHanging`
  - `ExprLastSpawnedEntity`
- landed but not registered in mixed runtime in this lane:
  - `ExprBeaconEffects`
  - `ExprBeaconRange`
  - `ExprBeaconTier`
  - `ExprDropsOfBlock`
  - `ExprEntityAttribute`
  - `ExprFireworkEffect`
  - `ExprLastDeathLocation`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`

## Bootstrap Registrations Needed

- no `SkriptFabricBootstrap.java` edits were made in this lane by rule
- if coordinator still wants cold-start bootstrap parity, mirror the mixed-runtime registrations for:
  - `ExprArrowKnockbackStrength`
  - `ExprArrowPierceLevel`
  - `ExprBarterDrops`
  - `ExprClicked`
  - `ExprDrops`
  - `ExprExplosionBlockYield`
  - `ExprExplosionYield`
  - `ExprExplosiveYield`
  - `ExprFertilizedBlocks`
  - `ExprHanging`
  - `ExprLastSpawnedEntity`
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

- `./gradlew compileGametestJava --rerun-tasks`
  - passed
- `./gradlew isolatedMixedRuntimeSyntaxBatchTest --rerun-tasks`
  - passed
- `./gradlew test --tests ...`
  - Gradle reported `No tests found for given includes` for the new package-private compatibility classes in this repo setup
- representative real `.sk` coverage added but not executed in this lane:
  - `src/gametest/resources/skript/gametest/expression/mixed_runtime_event_payload_bundle.sk`
  - `SkriptFabricMixedRuntimeBackfillGameTest#eventPayloadBundleExecutesRealScript`

## Blockers

- no `SkriptFabricBootstrap.java` edits in lane, so bootstrap-owned activation remains coordinator work
- several event-payload expressions currently rely on newly added compat payload classes, but the live Fabric runtime does not dispatch those payloads yet:
  - `ExprDrops`
  - `ExprExplosionYield`
  - `ExprFertilizedBlocks`
  - `ExprHanging`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`
- not all landed expressions were registered in `SkriptFabricAdditionalSyntax` in this lane:
  - `ExprBeaconEffects`
  - `ExprBeaconRange`
  - `ExprBeaconTier`
  - `ExprDropsOfBlock`
  - `ExprEntityAttribute`
  - `ExprFireworkEffect`
  - `ExprLastDeathLocation`
  - `ExprRespawnLocation`
  - `ExprRespawnReason`
- `ExprClicked` lands only the click-event block/entity surface, not the upstream inventory-click variants
- `ExprLastDeathLocation` currently resolves only online players from the active server context
- `ExprBeaconEffects`, `ExprBeaconRange`, and parts of `ExprExplosiveYield` use reflection against Minecraft internals and need coordinator/runtime confirmation before claiming parity

## Merge Note

- likely conflict files:
  - `src/main/java/ch/njol/skript/events/FabricEventCompatHandles.java`
  - `src/main/java/ch/njol/skript/effects/FabricEffectEventHandles.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricMixedRuntimeBackfillGameTest.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionEventPayloadBundleCompatibilityTest.java`
- lane avoided bootstrap edits and left live Fabric event producer wiring / GameTest execution follow-up to coordinator merge
