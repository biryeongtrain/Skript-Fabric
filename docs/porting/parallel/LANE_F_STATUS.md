# Lane F Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- finished the remaining requested entity leaf bundle with a mixed exact/class-backed import that fits the current Fabric entity registry shape
- added `ClassEntityData` plus restored `BoatData`, `BoatChestData`, `DroppedItemData`, `FallingBlockData`, `MinecartData`, `MooshroomData`, `StriderData`, `ThrownPotionData`, and `XpOrbData`
- widened `EntityDataRegistry.fromClass(...)` so lane-owned custom entity wrappers win over broad `SimpleEntityData` supertypes when the class matches exactly
- tightened `EntityCompatibilityTest` so parser and class-based lookup now cover the newly restored entity names and wrappers
- used the fallback bundle on a small effects cluster that binds to existing mapped entity handles only: `EffPandaOnBack`, `EffPandaSneezing`, and `EffScreaming`
- tightened `EffectCompatibilityTest` so those fallback effects prove their parse-mode/tag handling without needing new runtime bridge work

## Verification

- `./gradlew test --tests ch.njol.skript.entity.EntityCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.entity.EntityCompatibilityTest --tests ch.njol.skript.effects.EffectCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane F bundle is whichever additional `events` or `effects` cluster can bind to existing mapped entity/world handles without new `org/...` bridge edits; likely follow-ups are more low-argument entity-state effects or event classes that only need current shared event scaffolding

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/entity/*`
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
  - `src/test/java/ch/njol/skript/entity/EntityCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/effects/EffectCompatibilityTest.java`
