# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser` / `patterns` Part 1A closure work
- parser tag / mark / pattern parity
- parser-facing compatibility tests

## Goal For This Slice

- restore the missing upstream `patterns/Keyword.java` surface from `/tmp/skript-upstream-e6ec744-2`
- wire the upstream keyword prefilter into the local shared `SkriptPattern.match(...)` path without reopening broader pattern refactors
- prove the keyword surface and matcher integration through focused pattern and parser tests

## Latest Slice

- compared the local `patterns` package against `/tmp/skript-upstream-e6ec744-2`
  - the exact pre-slice upstream-only files were:
    - `Keyword.java`
    - `MalformedPatternException.java`
    - `package-info.java`
- closed the targeted upstream keyword parity gap inside Lane B scope:
  - restored `ch/njol/skript/patterns/Keyword.java` with upstream-style required-literal and one-level choice keyword extraction
  - adapted keyword traversal to the local graph-only compatibility layer so grouped/choice keywords respect trailing required literals even though branch nodes do not carry upstream `next` links
  - wired the keyword prefilter into `SkriptPattern.match(...)` before the shared regex matcher using normalized lower-cased input
  - kept the current regex matcher, mark/tag capture flow, placeholder parsing, and already-green natural-script forms intact
- post-slice `patterns` package delta versus upstream is now:
  - remaining upstream-only files:
    - `MalformedPatternException.java`
    - `package-info.java`

## Exact Counts Changed

- local `src/main/java/ch/njol/skript/patterns/*.java`: `11 -> 12`
- upstream-only `patterns` Java files relative to `/tmp/skript-upstream-e6ec744-2`: `3 -> 2`

## Files Changed

- `src/main/java/ch/njol/skript/patterns/Keyword.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- upstream comparison before the slice:
  - command:
    - `comm -23 <(cd /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/patterns && ls | sort) <(git ls-tree -r --name-only HEAD src/main/java/ch/njol/skript/patterns | sed 's#^.*/##' | sort)`
  - result:
    - `Keyword.java`
    - `MalformedPatternException.java`
    - `package-info.java`
- targeted verification after the slice:
  - command:
    - `./gradlew test --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`
  - result:
    - passed
    - `build/test-results/test/TEST-ch.njol.skript.patterns.PatternCompilerCompatibilityTest.xml`: `19` tests, `0` failures, `0` errors, `0` skipped
    - `build/test-results/test/TEST-ch.njol.skript.lang.SkriptParserRegistryTest.xml`: `31` tests, `0` failures, `0` errors, `0` skipped
- upstream comparison after the slice:
  - command:
    - `comm -23 <(cd /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/patterns && ls | sort) <(cd src/main/java/ch/njol/skript/patterns && ls | sort)`
  - result:
    - `MalformedPatternException.java`
    - `package-info.java`
- local `patterns` file count after the slice:
  - command:
    - `find src/main/java/ch/njol/skript/patterns -maxdepth 1 -name '*.java' | wc -l`
  - result:
    - `12`

## Coverage Added

- `PatternCompilerCompatibilityTest` now additionally proves:
  - the restored `Keyword` builder only emits required literal surface for optional + choice patterns
  - the keyword prefilter does not break exact `[the] name` natural-form matching
  - the keyword prefilter still allows grouped choice forms with a trailing required literal
- `SkriptParserRegistryTest` now additionally proves:
  - parser-facing effect registration still parses `(alpha|beta) gamma` through the shared matcher path with the keyword prefilter enabled

## Unresolved Risks

- `MalformedPatternException.java` and `package-info.java` are still absent from the local `patterns` package
- the local keyword traversal is intentionally adapted to the compatibility graph, not copied onto upstream `PatternElement.next` linkage; future graph consumers may still need richer structural parity
- no live `.sk` parsing surface was intentionally changed in this slice, so no GameTest run was performed

## Merge Notes

- likely conflict surface is limited to `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`, the new `src/main/java/ch/njol/skript/patterns/Keyword.java`, and the two parser-facing test files
- this slice does not touch `Statement.java`, `ScriptLoader.java`, `Variables.java`, `Classes.java`, or canonical `docs/porting/*.md`
