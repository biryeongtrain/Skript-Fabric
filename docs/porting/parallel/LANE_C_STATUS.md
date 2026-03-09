# Lane C Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/literals/**`
- `src/main/java/ch/njol/skript/sections/**`
- `src/main/java/ch/njol/skript/structures/**`
- tightly matching tests only

## Latest Slice

- restored the failed structures slice with local-compatible `StructEvent` and `StructExample`
- added `StructEventCompatibilityTest` to prove event-structure parsing forwards listening behavior and priority through `StructEvent.EventData`, including a local fallback from stripped event text to `on ...` registrations
- added `StructExampleCompatibilityTest` to prove `example:` bodies load under the `FunctionEvent` parse context and restore the previous parser event afterward

## Verification

- command: `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.structures.StructEventCompatibilityTest --tests ch.njol.skript.structures.StructExampleCompatibilityTest --tests ch.njol.skript.structures.StructVariablesCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --rerun-tasks`
- result: passed

## Next Lead

- fallback bundle review is currently blocked outside Lane C:
- `LitAt` needs upstream `ch/njol/skript/util/Direction`
- `LitEternity` needs upstream `Timespan.infinite()`
- `LitConsole` depends on Bukkit console sender surface
- remaining upstream `variables` classes are the storage backends (`VariablesStorage`, `FlatFileStorage`, `SQLStorage`, `SQLiteStorage`, `MySQLStorage`) and pull in Lane B `util` / config scaffolding plus database/runtime work
- remaining upstream `sections` / `structures` classes (`EffSecShoot`, `EffSecSpawn`, `ExprSecCreateWorldBorder`, `SecCatchErrors`, `SecFilter`, `SecFor`, `SecLoop`, `SecWhile`, `StructAliases`, `StructAutoReload`, `StructCommand`, `StructFunction`, `StructUsing`) cross into effects, entity, lang, or runtime ownership

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/structures/StructEvent.java`
  - `src/main/java/ch/njol/skript/structures/StructExample.java`
  - `src/test/java/ch/njol/skript/structures/StructEventCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/structures/StructExampleCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
