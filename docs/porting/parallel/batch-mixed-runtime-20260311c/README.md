# Mixed Runtime Batch 2026-03-11C

Coordinator follow-up batch for remaining concrete event activation.

- coordinator branch baseline: `659f6145c`
- runtime verification baseline before launch: `245 / 245` Fabric GameTests through `./gradlew build --rerun-tasks`
- priority: promote concrete event classes from compat-only proof to active runtime status when a real producer path and real `.sk` GameTest can be shown
- worker rules:
  - use local upstream snapshots only
  - no canonical doc edits
  - keep the slice inside lane-owned event/runtime verification scope
  - if an event is promoted to active, prove it with a real `.sk` GameTest that uses the live producer path, not manual compat dispatch
- coordinator duties after worker completion:
  - merge/no-op judgment
  - canonical doc promotion for newly verified active events
  - final `./gradlew build --rerun-tasks`

Lane split:

- `M1`: `EvtGrow` / `EvtPlantGrowth` producer-path activation proof
