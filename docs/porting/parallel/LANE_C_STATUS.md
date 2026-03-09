# Lane C Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/literals/**`
- `src/main/java/ch/njol/skript/sections/**`
- `src/main/java/ch/njol/skript/structures/**`
- `src/main/java/ch/njol/skript/aliases/**`
- tightly matching tests only

## Latest Slice

- added `LitEternity` as the now-unblocked infinite `Timespan` literal, matching upstream patterns for `an eternity`, `forever`, and `an infinite timespan`
- extended `LiteralsCompatibilityTest` to prove the literal parses to `Timespan.infinite()` through the registry-backed parser path
- created the first local `aliases` package foundation with upstream-backed `MatchQuality`, `InvalidMinecraftIdException`, and package scaffolding
- added `AliasesCompatibilityTest` to verify match-quality ordering helpers and retained invalid-id payloads

## Verification

- command: `./gradlew -q test --no-daemon --console plain --tests '*LiteralsCompatibilityTest' --tests '*AliasesCompatibilityTest' --rerun-tasks`
- result: passed
- command: `./gradlew -q test --no-daemon --console plain --tests '*StructUsingCompatibilityTest' --tests '*SecConditionalCompatibilityTest' --rerun-tasks`
- result: passed
- command: `./gradlew -q test --no-daemon --console plain --tests '*StructEventCompatibilityTest' --tests '*StructExampleCompatibilityTest' --tests '*StructVariablesCompatibilityTest' --tests '*SecIfCompatibilityTest' --rerun-tasks`
- result: passed

## Next Lead

- remaining literal and structure follow-ups are still blocked outside Lane C:
- `LitAt` needs upstream `ch/njol/skript/util/Direction`
- `LitConsole` depends on Bukkit console sender surface
- remaining upstream `variables` classes are the storage backends (`VariablesStorage`, `FlatFileStorage`, `SQLStorage`, `SQLiteStorage`, `MySQLStorage`) and pull in Lane B `util` / config scaffolding plus database/runtime work
- `StructFunction` is blocked on missing local `FunctionParser.parse(...)` / upstream function-signature parsing glue, which is Lane D-owned `lang/function` work
- remaining upstream `sections` / `structures` classes (`EffSecShoot`, `EffSecSpawn`, `ExprSecCreateWorldBorder`, `SecCatchErrors`, `SecFilter`, `SecFor`, `SecLoop`, `SecWhile`, `StructAliases`, `StructAutoReload`, `StructCommand`) still cross into effects, entity, aliases, or wider runtime ownership
- next lane-local continuation inside `aliases` is likely `package-info`-adjacent scaffolding only if it can stay decoupled from item/runtime classes; otherwise Lane C is waiting on `Direction`, console sender, storage backends, or broader alias runtime imports

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/literals/LitEternity.java`
  - `src/main/java/ch/njol/skript/aliases/MatchQuality.java`
  - `src/main/java/ch/njol/skript/aliases/InvalidMinecraftIdException.java`
  - `src/main/java/ch/njol/skript/aliases/package-info.java`
  - `src/test/java/ch/njol/skript/literals/LiteralsCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/aliases/AliasesCompatibilityTest.java`
  - `src/main/java/ch/njol/skript/structures/StructUsing.java`
  - `src/main/java/ch/njol/skript/sections/SecConditional.java`
  - `src/test/java/ch/njol/skript/structures/StructUsingCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/sections/SecConditionalCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
