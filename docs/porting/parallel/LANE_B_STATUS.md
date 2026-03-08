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

- switched this lane to the user-priority `Part 2` syntax-import track for one concrete upstream-visible effect family instead of continuing parser internals first
- imported upstream `EffInvisible` behavior for the exact effect forms:
  - `make %livingentities% (invisible|not visible)`
  - `make %livingentities% (visible|not invisible)`
- verified the upstream source before implementing with:
  - `curl -L --silent https://raw.githubusercontent.com/SkriptLang/Skript/master/src/main/java/ch/njol/skript/effects/EffInvisible.java | sed -n '1,120p'`
    - confirmed the exact registered patterns and the `matchedPattern == 0` visibility toggle
- added a Fabric runtime effect implementation at `org/skriptlang/skript/bukkit/base/effects/EffInvisible.java` that applies `LivingEntity#setInvisible(boolean)` on the active Mojang runtime
- wired the effect into `SkriptFabricBootstrap` alongside the existing base entity-control effect registrations so the exact upstream forms are available in the active Fabric runtime
- added focused syntax coverage proving all four exact surface forms parse to the new effect with the expected boolean state:
  - `make event-entity invisible`
  - `make event-entity not visible`
  - `make event-entity visible`
  - `make event-entity not invisible`
- added real `.sk` + GameTest runtime verification for a living entity using the exact upstream alternates rather than a rewritten variant:
  - `make event-entity not visible`
  - `make event-entity not invisible`
  - verified on a cow that the scripts both execute and mutate the Mojang invisibility flag in the live Fabric runtime
- did not broaden this slice into the invisible condition family; the user asked for one concrete effect syntax family first

### Previous Slice

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

### Earlier Slice

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

- `src/main/java/org/skriptlang/skript/bukkit/base/effects/EffInvisible.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/InvisibleSyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricInvisibleGameTest.java`
- `src/gametest/resources/skript/gametest/effect/make_invisible_names_entity.sk`
- `src/gametest/resources/skript/gametest/effect/make_visible_names_entity.sk`
- `docs/porting/parallel/LANE_B_STATUS.md`
- `src/main/java/ch/njol/skript/classes/ClassInfo.java`
- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.InvisibleSyntaxTest --rerun-tasks`
  - passed
- live `.sk` verification is present through `SkriptFabricInvisibleGameTest` plus the two new effect resources
  - full `./gradlew runGameTest --rerun-tasks` is deferred to coordinator integration verification
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

- unit parse forms: `make event-entity invisible`, `make event-entity not visible`, `make event-entity visible`, `make event-entity not invisible`
- live effect forms: `make event-entity not visible`, `make event-entity not invisible`
- parser omitted forms: `default number`, `default number 5`
- parser forms: `var {MiXeD}`, `the variable {MiXeD}`
- live statement form: `set var {MiXeDBlock} to "gold_block"`
- live expression form: `set test block at 0 1 0 to the variable {mixedblock}`

## Unresolved Risks

- this slice verifies the exact upstream effect surface and live flag mutation for living entities, but it does not add the separate upstream invisible condition family
- local omitted-placeholder handling now matches upstream more closely for classinfo-backed defaults, but broader upstream parity is still open around multi-type invalid-default diagnostics and other `getDefaultExpressions(...)` error surfaces
- prefixed variable forms now match upstream more closely, but the local parser still accepts variable expressions in broader parse contexts than upstream because this slice intentionally did not touch the surrounding context gates
- broader upstream parser parity is still open around matcher/runtime behavior outside these contained fallback and prefixed-variable fixes

## Merge Notes

- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java` now registers the upstream invisible effect forms in the active runtime
- `src/test/java/org/skriptlang/skript/fabric/runtime/InvisibleSyntaxTest.java`, `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricInvisibleGameTest.java`, and the two new `.sk` resources carry the exact invisible/visible coverage for this slice
- highest conflict risk is `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/classes/ClassInfo.java` now carries the new default-expression compatibility surface
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` now carries the exact omitted-form default-value regressions
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java` may conflict with any concurrent variable-parser regressions
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java` and `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk` now carry the real `.sk` coverage for prefixed variable parsing
- current branch conflict surface is `src/main/java/ch/njol/skript/classes/ClassInfo.java`, `src/main/java/ch/njol/skript/lang/SkriptParser.java`, `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`, `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`, `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`, and `src/gametest/resources/skript/gametest/base/prefixed_variable_set_test_block.sk`
