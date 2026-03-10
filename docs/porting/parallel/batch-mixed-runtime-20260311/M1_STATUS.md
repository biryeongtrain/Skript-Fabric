# M1 Status

## Scope

- `conditions/**`
- concrete event runtime producers / event-handle glue only for assigned targets
- narrow tests for assigned conditions/events

## Assigned Targets

- missing conditions `15`:
  - `CondChatColors`
  - `CondChatFiltering`
  - `CondChatVisibility`
  - `CondElytraBoostConsume`
  - `CondFromMobSpawner`
  - `CondHasClientWeather`
  - `CondHasMetadata`
  - `CondHasResourcePack`
  - `CondIsEnchanted`
  - `CondIsPluginEnabled`
  - `CondIsSkriptCommand`
  - `CondIsSlimeChunk`
  - `CondIsSpawnable`
  - `CondLeashed`
  - `CondResourcePack`
- runtime activation / expansion `9`:
  - `EvtEntityBlockChange`
  - `EvtGrow`
  - `EvtPlantGrowth`
  - `EvtPressurePlate`
  - `EvtVehicleCollision`
  - broaden `EvtBlock`
  - broaden `EvtItem`
  - broaden `EvtEntity`
  - broaden `EvtHealing`

## Landed Classes

- conditions:
  - `CondChatColors`
  - `CondChatFiltering`
  - `CondChatVisibility`
  - `CondElytraBoostConsume`
  - `CondFromMobSpawner`
  - `CondHasClientWeather`
  - `CondHasMetadata`
  - `CondHasResourcePack`
  - `CondIsEnchanted`
  - `CondIsPluginEnabled`
  - `CondIsSkriptCommand`
  - `CondIsSlimeChunk`
  - `CondIsSpawnable`
  - `CondLeashed`
  - `CondResourcePack`
- runtime event activation / bridge:
  - `EvtEntityBlockChange`
  - `EvtGrow`
  - `EvtPlantGrowth`
  - `EvtPressurePlate`
  - `EvtVehicleCollision`
  - adjacent bridge expansion for `EvtBlock`, `EvtItem`, `EvtEntity`, `EvtHealing`
- runtime glue:
  - `FabricPlayerClientState`
  - `ServerGamePacketListenerImplMixin` resource-pack response tracking
  - block / crop runtime producer glue already present in-lane for `dispatchBlockPlace`, `dispatchGrow`, `dispatchPlantGrowth`, `dispatchEntityBlockChange`, `dispatchPressurePlate`

## Runtime-Eligible Classes

- active through `SkriptFabricAdditionalSyntax.register()` on this branch:
  - all 15 assigned conditions above via `forceInitialize(...)`
  - `EvtEntityBlockChange`, `EvtGrow`, `EvtPlantGrowth`, `EvtPressurePlate`, `EvtResourcePackResponse`, `EvtVehicleCollision`
- real runtime coverage in-lane:
  - `block_place_sets_block.sk`
  - `plant_growth_sets_blocks.sk`
  - `entity_block_change_marks_block.sk`
  - `pressure_plate_marks_blocks.sk`
  - `vehicle_collision_marks_block.sk`
- note:
  - `CondHasMetadata` is currently `%objects%`-based because there is no local `metadataholder` classinfo
  - `CondHasClientWeather` and `CondFromMobSpawner` currently rely on compatible reflection/fallbacks rather than dedicated Fabric-side state trackers
  - `CondIsEnchanted` is syntax-active but not upstream-complete because the local runtime lacks Bukkit-side `EnchantmentType` support
  - `CondIsSlimeChunk` is a reflection-backed placeholder against `chunk` typing; a proper chunk classinfo/runtime value source is still missing

## Bootstrap Registrations Needed

- none pending in forbidden canonical bootstrap for the landed parse/registration work
- branch already wires the assigned conditions/events through `SkriptFabricAdditionalSyntax`
- real producer attachment for `EvtEntityBlockChange` / `EvtPressurePlate` / `EvtVehicleCollision` still depends on the in-branch mixin/event-hook lane state and coordinator merge order
- keep `skript-fabric.mixins.json` entries for `BlockItemMixin`, `CropBlockMixin`, and `ServerGamePacketListenerImplMixin`

## Targeted Tests

- `./gradlew test --tests ch.njol.skript.conditions.ConditionM1CompatibilityTest --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --tests org.skriptlang.skript.fabric.runtime.MixedRuntimeSyntaxBatchTest --tests kim.biryeong.skriptFabricPort.gametest.SkriptFabricEventGameTest --no-daemon`
  - result: `BUILD SUCCESSFUL`
- `./gradlew compileGametestJava --no-daemon`
  - result: `BUILD SUCCESSFUL`
- condition coverage:
  - `ConditionM1CompatibilityTest`
  - parser/bootstrap coverage in `MixedRuntimeSyntaxBatchTest`
- event/runtime coverage:
  - `EventCompatibilityTest`
  - `EventBridgeBindingTest`
  - `MixedRuntimeSyntaxBatchTest`
  - GameTest source-set compile for `SkriptFabricEventGameTest`
  - real `.sk` resources:
    - `block_place_sets_block.sk`
    - `plant_growth_sets_blocks.sk`
    - `entity_block_change_marks_block.sk`
    - `pressure_plate_marks_blocks.sk`
    - `vehicle_collision_marks_block.sk`

## Blockers

- known semantic gaps:
  - `CondIsEnchanted` is only a partial Fabric approximation; upstream optional `with %-enchantmenttypes% [or better/worse]` semantics are not portable yet without local enchantment-type support
  - `CondHasMetadata` cannot use upstream `%metadataholders%` typing until a compatible classinfo exists
  - `CondHasClientWeather` / `CondFromMobSpawner` are fallback implementations; exact parity would need explicit Fabric-side producers/state
  - `CondIsSlimeChunk` cannot be made fully equivalent until chunk values/classinfo exist in the local runtime
  - `CondHasResourcePack` tracks loaded packs through `FabricPlayerClientState`; there is still no full upstream-style resource-pack object/state surface

## Merge Note

- safe to cherry-pick as one conventional commit for the M1 lane
- do not overwrite user/in-lane changes in existing event bridge files; this batch depends on pre-existing M1 event producer work already present in the branch
- highest-conflict files:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricEventBridge.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricEventGameTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/MixedRuntimeSyntaxBatchTest.java`
