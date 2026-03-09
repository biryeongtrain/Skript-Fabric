# Lane F Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- landed a larger upstream-backed entity leaf bundle on top of the existing Lane F scaffolding
- added shared exact-type runtime glue in `ExactEntityData` and widened `EntityDataRegistry` / `EntityData` suppliers to track non-`SimpleEntityData` entries
- restored `AxolotlData`, `BeeData`, `CatData`, `ChickenData`, `CowData`, `CreeperData`, `EndermanData`, `FoxData`, `FrogData`, `GoatData`, `LlamaData`, `PandaData`, `ParrotData`, `PigData`, `RabbitData`, `SalmonData`, `SheepData`, `TropicalFishData`, `VillagerData`, `WolfData`, and `ZombieVillagerData`
- tightened `EntityCompatibilityTest` so parser/classinfo/class-based lookup now proves the imported exact wrappers are returned and plural names like `zombie villagers` still normalize correctly

## Verification

- `./gradlew test --tests ch.njol.skript.entity.EntityCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane F bundle is whichever additional upstream `events` or `effects` cluster can bind to the existing Fabric handles without requiring new `org/...` runtime bridge edits or Lane E parser/expression overlap; the entity leaf registry itself now has a broader exact-type base to build on

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/entity/*`
  - `src/main/java/ch/njol/skript/events/Evt*.java`
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
