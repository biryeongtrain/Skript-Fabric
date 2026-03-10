# M3 Status

## landed classes
- `ExprAffectedEntities`
- `ExprBarterInput`
- `ExprConsumedItem`
- `ExprExperienceCooldownChangeReason`
- `ExprExplodedBlocks`
- `ExprHatchingNumber`
- `ExprHatchingType`
- `ExprHealAmount`
- `ExprLastAttacker`
- `ExprLeashHolder`

## runtime-eligible classes
- `ExprAffectedEntities`
- `ExprBarterInput`
- `ExprConsumedItem`
- `ExprExperienceCooldownChangeReason`
- `ExprExplodedBlocks`
- `ExprHatchingNumber`
- `ExprHatchingType`
- `ExprHealAmount`
- `ExprLastAttacker`
- `ExprLeashHolder`
- note: `ExprHatchingNumber` and `ExprHatchingType` have mutable compat-handle support; the rest are read-only on the current local event handles

## bootstrap registrations needed
- add `forceInitialize(ch.njol.skript.expressions.ExprAffectedEntities.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprBarterInput.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprConsumedItem.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprExperienceCooldownChangeReason.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprExplodedBlocks.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprHatchingNumber.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprHatchingType.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprHealAmount.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprLastAttacker.class)`
- add `forceInitialize(ch.njol.skript.expressions.ExprLeashHolder.class)`

## targeted tests
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionEventContextBundleCompatibilityTest --tests ch.njol.skript.expressions.ExpressionCombatContextCompatibilityTest --tests ch.njol.skript.conditions.ConditionSyntaxS1CompatibilityTest --rerun-tasks`
  - passed

## blockers
- `ExprBeaconEffects`, `ExprBeaconRange`, and `ExprBeaconTier` were not landed in this slice; they need block-entity level/effect/range access that is not exposed in-lane yet
- `ExprDrops` remains blocked on a death/harvest drop payload surface instead of the current effect-only marker classes
- `ExprFertilizedBlocks` remains blocked because the upstream `BlockStateBlock` surface is not present locally

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/events/FabricEventCompatHandles.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/FabricEggThrowEventHandle.java`
  - `src/main/java/ch/njol/skript/expressions/*.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionCombatContextCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/conditions/ConditionSyntaxS1CompatibilityTest.java`
