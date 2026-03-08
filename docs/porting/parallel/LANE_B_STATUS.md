# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser` / `patterns` Part 1A closure work
- parser tag / mark / pattern parity
- parser-facing compatibility tests

## Goal For This Slice

- land one contained, mergeable parser/pattern closure slice without touching Lane A or Lane C ownership
- prioritize the still-open upstream `patterns` graph parity over broader parser refactors

## Latest Slice

- compared the local `patterns` package against `/tmp/skript-upstream-ueogiz`
  - the exact pre-slice upstream-only files were:
    - `Keyword.java`
    - `MalformedPatternException.java`
    - `package-info.java`
- closed one contained upstream parity gap inside Lane B scope:
  - restored upstream-style pattern graph stringification through `PatternElement.toFullString()`
  - restored upstream-style pattern combination expansion through `getCombinations(...)` / `getAllCombinations(...)`
  - preserved parenthesized alternation groups in the graph by keeping `GroupPatternElement` around `(a|b)` forms instead of collapsing them to a bare choice
  - restored upstream-style malformed-pattern wrapping by adding `MalformedPatternException`
  - kept the shared regex matcher path and parser mark/tag behavior intact
- post-slice `patterns` package delta versus upstream is now:
  - remaining upstream-only files:
    - `Keyword.java`
    - `package-info.java`

## Exact Counts Changed

- local `src/main/java/ch/njol/skript/patterns/*.java`: `11 -> 12`
- upstream-only `patterns` Java files relative to `/tmp/skript-upstream-ueogiz`: `3 -> 2`

## Files Changed

- `src/main/java/ch/njol/skript/patterns/ChoicePatternElement.java`
- `src/main/java/ch/njol/skript/patterns/GroupPatternElement.java`
- `src/main/java/ch/njol/skript/patterns/LiteralPatternElement.java`
- `src/main/java/ch/njol/skript/patterns/MalformedPatternException.java`
- `src/main/java/ch/njol/skript/patterns/OptionalPatternElement.java`
- `src/main/java/ch/njol/skript/patterns/ParseTagPatternElement.java`
- `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`
- `src/main/java/ch/njol/skript/patterns/PatternElement.java`
- `src/main/java/ch/njol/skript/patterns/RegexPatternElement.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/main/java/ch/njol/skript/patterns/TypePatternElement.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- upstream comparison before the slice:
  - command:
    - `comm -23 <(cd /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/patterns && ls | sort) <(cd src/main/java/ch/njol/skript/patterns && ls | sort)`
  - result:
    - `Keyword.java`
    - `MalformedPatternException.java`
    - `package-info.java`
- targeted verification after the slice:
  - command:
    - `./gradlew test --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`
  - result:
    - passed
    - `build/test-results/test/TEST-ch.njol.skript.patterns.PatternCompilerCompatibilityTest.xml`: `18` tests, `0` failures, `0` errors, `0` skipped
    - `build/test-results/test/TEST-ch.njol.skript.lang.SkriptParserRegistryTest.xml`: `30` tests, `0` failures, `0` errors, `0` skipped
- upstream comparison after the slice:
  - command:
    - `comm -23 <(cd /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/patterns && ls | sort) <(cd src/main/java/ch/njol/skript/patterns && ls | sort)`
  - result:
    - `Keyword.java`
    - `package-info.java`
- local `patterns` file count after the slice:
  - command:
    - `find src/main/java/ch/njol/skript/patterns -maxdepth 1 -name '*.java' | wc -l`
  - result:
    - `12`

## Coverage Added

- `PatternCompilerCompatibilityTest` now additionally proves:
  - nested group/optional string reconstruction keeps upstream-style spaces and parentheses
  - pattern-element combination expansion is exposed again
  - malformed pattern compilation is wrapped in `MalformedPatternException`

## Unresolved Risks

- `Keyword.java` is still absent, so the local shared matcher still does not have upstream keyword prefilter parity
- this slice restored graph APIs, not full upstream matcher internals; future parser closures may still need richer graph linkage if another upstream consumer depends on it
- no live `.sk` parse semantics were intentionally changed in this slice, so no GameTest run was performed

## Merge Notes

- likely conflict surface is limited to `src/main/java/ch/njol/skript/patterns/**` and `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- this slice does not touch `Statement.java`, `ScriptLoader.java`, `Variables.java`, `Classes.java`, or canonical `docs/porting/*.md`
