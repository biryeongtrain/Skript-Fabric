# Lane C Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/literals/**`
- `src/main/java/ch/njol/skript/sections/**`
- `src/main/java/ch/njol/skript/structures/**`
- tightly matching tests only

## Latest Slice

- added local-compatible `StructUsing` as a simple structure that resolves experiment names through `Skript.experiments()`, warns for `MAINSTREAM` / `DEPRECATED` / `UNKNOWN`, and records enabled experiments on the current script's `ExperimentSet`
- added `StructUsingCompatibilityTest` to prove both registered and unknown `using ...` forms parse and update the current script state
- added upstream-style `SecConditional` as a self-contained adjacent section class on the current parser surface without touching `SecIf`
- added `SecConditionalCompatibilityTest` to prove `else if`, `parse if`, multiline `if any` + `then`, and implicit conditional parsing all verify cleanly in isolation

## Verification

- command: `./gradlew -q test --no-daemon --console plain --tests '*StructUsingCompatibilityTest' --tests '*SecConditionalCompatibilityTest' --rerun-tasks`
- result: passed
- command: `./gradlew -q test --no-daemon --console plain --tests '*StructEventCompatibilityTest' --tests '*StructExampleCompatibilityTest' --tests '*StructVariablesCompatibilityTest' --tests '*SecIfCompatibilityTest' --rerun-tasks`
- result: passed

## Next Lead

- fallback bundle review is currently blocked outside Lane C:
- `LitAt` needs upstream `ch/njol/skript/util/Direction`
- `LitEternity` needs upstream `Timespan.infinite()`
- `LitConsole` depends on Bukkit console sender surface
- remaining upstream `variables` classes are the storage backends (`VariablesStorage`, `FlatFileStorage`, `SQLStorage`, `SQLiteStorage`, `MySQLStorage`) and pull in Lane B `util` / config scaffolding plus database/runtime work
- `StructFunction` is blocked on missing local `FunctionParser.parse(...)` / upstream function-signature parsing glue, which is Lane D-owned `lang/function` work
- remaining upstream `sections` / `structures` classes (`EffSecShoot`, `EffSecSpawn`, `ExprSecCreateWorldBorder`, `SecCatchErrors`, `SecFilter`, `SecFor`, `SecLoop`, `SecWhile`, `StructAliases`, `StructAutoReload`, `StructCommand`) still cross into effects, entity, aliases, or wider runtime ownership

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/structures/StructUsing.java`
  - `src/main/java/ch/njol/skript/sections/SecConditional.java`
  - `src/test/java/ch/njol/skript/structures/StructUsingCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/sections/SecConditionalCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
