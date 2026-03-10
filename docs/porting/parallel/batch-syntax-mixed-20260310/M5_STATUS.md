landed classes
- `EffColorItems`
- `EffEnchant`
- `EffEquip`
- `EffDrop`
- `EffHealth`
- `EffTeleport`
- `EffWakeupSleep`
- `EffFireworkLaunch`
- `EffElytraBoostConsume`
- `EffExplosion`
- `EffTree`
- `EffEntityVisibility`
- `EffectMixedImportCompatibilityTest`

runtime-eligible classes
- `EffColorItems` parser-capable import; runtime mutation still needs Fabric item color component wiring
- `EffEnchant` parser-capable import; runtime enchantment application still needs enchantment compat wiring
- `EffEquip` parser-capable import
- `EffHealth` parser-capable import with partial living-entity / slot mutation
- `EffFireworkLaunch` parser-capable import; runtime spawn wiring still needs firework-effect conversion

bootstrap registrations needed
- `EffColorItems.register()`
- `EffEnchant.register()`
- `EffEquip.register()`
- `EffDrop.register()`
- `EffHealth.register()`
- `EffTeleport.register()`
- `EffWakeupSleep.register()`
- `EffFireworkLaunch.register()`
- `EffElytraBoostConsume.register()`
- `EffExplosion.register()`
- `EffTree.register()`
- `EffEntityVisibility.register()`
- classinfo/runtime glue still needed before bootstrap activation for `%color%`, `%enchantmenttypes%`, `%fireworkeffects%`, `%directions%`, `%structuretype%`, and any teleport-flag / elytra-boost event surfaces

targeted tests
- attempted exact class filter: `./gradlew test --tests ch.njol.skript.effects.EffectMixedImportCompatibilityTest --rerun-tasks`
  - Gradle completed compilation but reported `No tests found for given includes`
- passed: `./gradlew test --tests 'ch.njol.skript.effects.*' --rerun-tasks`

blockers
- missing local `Direction` compat blocks `EffDrop`, `EffTeleport`, `EffWakeupSleep`, and `EffExplosion`
- missing local `StructureType` compat blocks `EffTree`
- no active Fabric event/runtime surface yet for elytra-boost firework consumption or per-viewer entity visibility
- color, enchantment, and firework-effect runtime mutation still need broader compat wiring before bootstrap registration is safe

merge-note
- likely conflict surface is `src/main/java/ch/njol/skript/effects/**`
- no bootstrap file edits
