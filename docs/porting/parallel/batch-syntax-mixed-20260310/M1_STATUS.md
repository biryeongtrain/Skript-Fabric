# M1 Status

- landed classes:
  - `FabricEventCompatHandles` runtime-backed compat expansion for `BeaconEffect`, `BeaconToggle`, `Block`, `BookEdit`, `Click`, `EntityLifecycle`, `EntityTransform`, `Healing`, `Item`
  - `SkriptFabricEventBridge` producers for beacon effect/toggle, click, entity spawn/death, experience spawn, item spawn, entity transform, book edit/sign, healing
  - mixins `BeaconPotionCauseMixin`, `BookEditMixin`, `LivingEntityHealingMixin`
  - targeted tests `EventCompatibilityTest`, `EventBridgeBindingTest`

- runtime-eligible classes:
  - primary bundle:
    - `EvtBeaconEffect`
    - `EvtBeaconToggle`
    - `EvtBlock` via existing block-break callback only
    - `EvtEntity` via entity load + after-death callbacks
    - `EvtItem` via item-entity load callback for spawn only
  - fallback bundle:
    - `EvtBookEdit`
    - `EvtBookSign`
    - `EvtClick`
    - `EvtEntityTransform`
    - `EvtExperienceSpawn`
    - `EvtHealing`

- bootstrap registrations needed:
  - `EvtBeaconEffect.register()`
  - `EvtBeaconToggle.register()`
  - `EvtBlock.register()`
  - `EvtEntity.register()`
  - `EvtItem.register()`
  - `EvtBookEdit.register()`
  - `EvtBookSign.register()`
  - `EvtClick.register()`
  - `EvtEntityTransform.register()`
  - `EvtExperienceSpawn.register()`
  - `EvtHealing.register()`

- targeted tests:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.EventBridgeBindingTest --rerun-tasks`
  - result: passed

- blockers:
  - `EvtEntityBlockChange` still has no lane-local live producers for Enderman, Sheep, Silverfish, or falling-block transitions; that needs dedicated entity/block mixin hooks beyond the current narrow bridge pass.
  - `EvtGrow` and `EvtPlantGrowth` still need real growth-source hooks across crop random ticks, bonemeal paths, and structure growth; no single Fabric callback in-lane covers that bundle.
  - `EvtPressurePlate` still needs direct block-state transition hooks for both pressure plates and tripwire; there is no native Fabric callback already present in this tree.
  - `EvtVehicleCollision` still needs vehicle-specific collision hooks; no existing Fabric callback in this branch exposes block/entity collision details for vehicles.
  - `EvtBlock` is only partially live here: the bridge now covers block break, but burn/place/fade/form/drop remain unhooked.
  - `EvtItem` is only partially live here: the bridge now covers item spawn, but drop/pickup/consume/despawn/merge/inventory variants remain unhooked.
  - `EvtEntityTransform` and `EvtHealing` are live with coarse reasons only; Fabric exposes conversion/heal entry points, but not the fuller upstream reason taxonomy on this lane.

- merge-note:
  - likely conflicts:
    - `src/main/java/ch/njol/skript/events/FabricEventCompatHandles.java`
    - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricEventBridge.java`
    - `src/main/java/kim/biryeong/skriptFabric/mixin/BeaconPotionCauseMixin.java`
    - `src/main/resources/skript-fabric.mixins.json`
    - `src/test/java/ch/njol/skript/events/EventCompatibilityTest.java`
