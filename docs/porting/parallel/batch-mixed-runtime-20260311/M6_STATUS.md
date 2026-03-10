# M6 Status

## Scope

- `effects/**`
- event/effect runtime glue only for assigned active targets
- narrow tests for assigned effects

## Assigned Targets

- missing effect classes `5`:
  - `Delay`
  - `EffChange`
  - `EffDoIf`
  - `EffReturn`
  - `IndeterminateDelay`
- currently import-only effect activation / closure `15`:
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
  - `EffClearEntityStorage`
  - `EffInsertEntityStorage`
  - `EffReleaseEntityStorage`

## Landed Classes

- missing-class backfills landed:
  - `Delay`
  - `EffChange`
  - `EffDoIf`
  - `EffReturn`
  - `IndeterminateDelay`
- import-only targets landed to executable runtime paths:
  - `EffEquip`
  - `EffHealth`
- helper/runtime glue landed for assigned scope:
  - `SkriptFabricTaskScheduler`
  - `Variables.removeLocals(...)`
  - `Variables.setLocalVariables(...)`
  - server-tick bridge hook in `SkriptFabricEventBridge`

## Runtime-Eligible Classes

- active in mixed runtime after this batch:
  - `Delay`
  - `EffDoIf`
  - `EffReturn`
  - `EffEquip`
  - `EffHealth`
- landed but not newly activated:
  - `EffChange`
  - `IndeterminateDelay`
- still import-only / not runtime-eligible in this batch:
  - `EffColorItems`
  - `EffEnchant`
  - `EffDrop`
  - `EffTeleport`
  - `EffWakeupSleep`
  - `EffFireworkLaunch`
  - `EffElytraBoostConsume`
  - `EffExplosion`
  - `EffTree`
  - `EffEntityVisibility`
  - `EffClearEntityStorage`
  - `EffInsertEntityStorage`
  - `EffReleaseEntityStorage`

## Bootstrap Registrations Needed

- no direct `SkriptFabricBootstrap.java` edit required
- registrations added in `SkriptFabricAdditionalEffects`:
  - `Delay`
  - `EffDoIf`
  - `EffEquip`
  - `EffHealth`
  - `EffReturn`

## Targeted Tests

- JUnit passed:
  - `./gradlew test --tests ch.njol.skript.effects.EffectCoreCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EffectSyntaxParsingTest --tests org.skriptlang.skript.fabric.runtime.EffectBindingTest --tests org.skriptlang.skript.fabric.runtime.MixedRuntimeSyntaxBatchTest --rerun-tasks`
- added representative real `.sk` GameTest resources:
  - `wait_one_tick_sets_block.sk`
  - `do_if_names_entity.sk`
  - `equip_entity_marks_block.sk`
  - `damage_entity_marks_block.sk`
  - `return_function_sets_block.sk`
- `runGameTest` is currently blocked before batch-local tests execute:
  - first rerun exposed/fixed local issues in `EffEquip` array handling and the delay GameTest sequence structure
  - latest rerun now fails at startup on missing mixin `kim.biryeong.skriptFabric.mixin.AbstractFurnaceBlockEntityMixin`

## Blockers

- full GameTest suite startup is blocked by unrelated missing mixin:
  - `skript-fabric.mixins.json:AbstractFurnaceBlockEntityMixin`
- remaining assigned import-only effects need separate runtime work beyond this batch:
  - color/enchant/drop/teleport/wakeup/firework/elytra/explosion/tree/entity-visibility/entity-storage effects
- `EffHealth` runtime activation intentionally excludes broader itemtype/FabricItemType mutation paths not yet proven in mixed runtime

## Merge Note

- likely merge touchpoints:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricEventBridge.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricEffectGameTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/EffectBindingTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/EffectSyntaxParsingTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/MixedRuntimeSyntaxBatchTest.java`
