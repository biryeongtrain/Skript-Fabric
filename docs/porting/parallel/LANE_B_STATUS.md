# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser`
- `patterns`
- parser tag / mark parity

## Owned Files

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/**`
- parser-facing tests in `src/test/java/ch/njol/skript/**`

## Goal For Next Session

- Continue `Part 1A` on the remaining parser and matcher parity after the prefixed-variable and classinfo-backed omitted-default slices, without claiming parser-pattern parity complete.

## Work Log

### Latest Slice

- closed the current parser default-value gap for omitted non-optional placeholders by adding the missing `ClassInfo` default-expression fallback behind the already-green parser-scoped `DefaultValueData` path
- compared the local implementation against upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` with:
  - `curl -L --silent https://raw.githubusercontent.com/SkriptLang/Skript/e6ec744dd83cb1a362dd420cde11a0d74aef977d/src/main/java/ch/njol/skript/lang/SkriptParser.java | sed -n '372,428p'`
    - confirmed upstream first checks `DefaultValueData`, then falls back to `ClassInfo.getDefaultExpression()` for omitted non-optional placeholders
  - `curl -L --silent https://raw.githubusercontent.com/SkriptLang/Skript/e6ec744dd83cb1a362dd420cde11a0d74aef977d/src/main/java/ch/njol/skript/classes/ClassInfo.java | sed -n '120,160p'`
    - confirmed upstream exposes `ClassInfo.defaultExpression(...)`
  - `curl -L --silent https://raw.githubusercontent.com/SkriptLang/Skript/e6ec744dd83cb1a362dd420cde11a0d74aef977d/src/main/java/ch/njol/skript/classes/ClassInfo.java | sed -n '292,306p'`
    - confirmed upstream exposes `ClassInfo.getDefaultExpression()`
- `ClassInfo` now exposes upstream-shaped default-expression storage/access, and `SkriptParser` now consults it only when parser-scoped default data is absent for the omitted slot's return type
- added tight parser regression coverage for the exact omitted form `default number [%number%]`, proving:
  - classinfo-backed defaults fill the omitted `default number` slot
  - explicit `default number 5` still prefers the provided value
  - parser-scoped `DefaultValueData` still wins over a registered classinfo default for the same omitted form
- did not add GameTest coverage for this slice because the local runtime does not currently register shipped classinfo default expressions, so this change stays parser/unit infrastructure coverage rather than a live shipped `.sk` behavior change
- did not mark parity complete: broader upstream parser parity remains open around richer default-expression diagnostics and the remaining matcher/runtime differences outside this contained fallback closure

### Previous Slice

- closed an upstream-backed parser-flow parity slice around prefixed variable expressions
- upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` still accepts `var {x}`, `variable {x}`, and `the variable {x}` through `SkriptParser.parseVariable(...)`, while the local parser only recognized bare `{x}` expressions
- `SkriptParser.parseVariableExpression(...)` now uses the same optional prefix pattern and nested-brace guard shape as upstream while preserving the already-green bare-variable path
- added parser-facing regressions proving:
  - `var {MiXeD}` parses as a `Variable`
  - `the variable {MiXeD}` parses as a `Variable`
  - effect-style live usage now works through the parser path with `set var {MiXeDBlock} to "gold_block"` and `the variable {mixedblock}`
- live `.sk` parsing changed in a shipped syntax family, so added real coverage for prefixed variables:
  - [src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk](../../../src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk)
  - [src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java](../../../src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java)
- reran the full Fabric GameTest corpus after the parser change; the existing natural-script forms, parse-if coverage, duplicate tag ordering, slash placeholder unions, and Patbox placeholder behavior stayed green

## Files Changed

- `src/main/java/ch/njol/skript/classes/ClassInfo.java`
- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Verification

- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
- no `runGameTest` for the latest slice
  - reason: no locally shipped syntax currently exercises classinfo-backed default expressions, so the new behavior is covered by parser/unit tests only
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - first attempt failed because I incorrectly launched it in parallel with another Gradle build, which left a transient empty `build/resources/main/fabric.mod.json` and caused Fabric mod discovery to abort with `EOFException`
  - reran sequentially on the finished tree; command passed
  - `204 / 204` required GameTests completed successfully

## Exact Syntax Exercised

- parser omitted forms: `default number`, `default number 5`
- parser forms: `var {MiXeD}`, `the variable {MiXeD}`
- live statement form: `set var {MiXeDBlock} to "gold_block"`
- live expression form: `set test block at 0 1 0 to the variable {mixedblock}`

## Unresolved Risks

- local omitted-placeholder handling now matches upstream more closely for classinfo-backed defaults, but broader upstream parity is still open around multi-type invalid-default diagnostics and other `getDefaultExpressions(...)` error surfaces
- prefixed variable forms now match upstream more closely, but the local parser still accepts variable expressions in broader parse contexts than upstream because this slice intentionally did not touch the surrounding context gates
- broader upstream parser parity is still open around matcher/runtime behavior outside these contained fallback and prefixed-variable fixes

## Merge Notes

- highest conflict risk is `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/classes/ClassInfo.java` now carries the new default-expression compatibility surface
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` now carries the exact omitted-form default-value regressions
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java` may conflict with any concurrent variable-parser regressions
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java` and `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk` now carry the real `.sk` coverage for prefixed variable parsing
- current branch conflict surface is `src/main/java/ch/njol/skript/classes/ClassInfo.java`, `src/main/java/ch/njol/skript/lang/SkriptParser.java`, `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`, `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`, `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`, and `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk`
