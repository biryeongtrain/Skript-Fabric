# Lane D Status

Last updated: 2026-03-08

## Scope

- legacy wrapper/helper compatibility for `ch/njol/skript/classes`
- legacy converter-registry bridge for `ch/njol/skript/registrations/Converters.java`
- focused wrapper compatibility tests only

## Owned Files

- `src/main/java/ch/njol/skript/classes/Parser.java`
- `src/main/java/ch/njol/skript/classes/PatternedParser.java`
- `src/main/java/ch/njol/skript/classes/Converter.java`
- `src/main/java/ch/njol/skript/registrations/Converters.java`
- `src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Goal For This Slice

- restore the legacy `Parser`, `PatternedParser`, `Converter`, and `Converters` surfaces from the upstream snapshot where they fit the current Fabric compatibility runtime
- keep the slice contained to wrapper plumbing and targeted unit coverage
- avoid widening into `Classes.java`, `SkriptParser.java`, serializer/config ports, or GameTest-only runtime behavior

## What Landed

- added legacy abstract `Parser<T>` that plugs directly into the current `ClassInfo.Parser<T>` contract and preserves upstream-facing stringification helpers
- added legacy `PatternedParser<T>` with upstream-style `getPatterns()` plus `getCombinedPatterns()`
- added legacy `Converter<F, T>` as a bridge over the current `org.skriptlang.skript.lang.converter.Converter`, including the legacy chaining flags and usable `ConverterUtils` helper wrappers
- added legacy `ch.njol.skript.registrations.Converters` as a bridge over the current converter backend:
  - registration
  - lookup
  - existence checks
  - single and array conversion helpers
  - strict conversion helpers
- added focused compatibility coverage proving the restored wrappers are usable on the current local runtime through `ClassInfo`, `Classes.parse(...)`, and the active converter backend
- kept `src/main/java/ch/njol/skript/registrations/Classes.java` untouched

## Files Changed

- `src/main/java/ch/njol/skript/classes/Parser.java`
- `src/main/java/ch/njol/skript/classes/PatternedParser.java`
- `src/main/java/ch/njol/skript/classes/Converter.java`
- `src/main/java/ch/njol/skript/registrations/Converters.java`
- `src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Counts Changed

- Stage 8 package-local audit counts changed: `0`
- Fabric GameTest counts changed: `0`
- source files added: `4`
- test files added: `1`
- test methods added: `4`
- canonical docs changed: `0`

## Exact Commands And Results

- `sed -n '1,240p' docs/porting/README.md`
  - read successfully
- `sed -n '1,240p' docs/porting/NEXT_AGENT_HANDOFF.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
  - read successfully
- `sed -n '1,260p' docs/porting/FABRIC_PORT_STAGES.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CODEX_PARALLEL_WORKFLOW.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CODEX_PARALLEL_PROMPTS.md`
  - read successfully
- `ls docs/porting/parallel`
  - confirmed an existing stale `LANE_D_STATUS.md` was present and needed replacement
- `sed -n '1,240p' src/main/java/ch/njol/skript/classes/ClassInfo.java`
  - reviewed the current local parser attachment point
- `sed -n '1,260p' src/main/java/ch/njol/skript/registrations/Classes.java`
  - reviewed the local classes runtime and confirmed wrapper work could stay outside this file
- `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/classes/Parser.java`
  - reviewed upstream parser wrapper shape
- `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/classes/PatternedParser.java`
  - reviewed upstream patterned parser shape
- `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/classes/Converter.java`
  - reviewed upstream legacy converter wrapper shape
- `sed -n '1,320p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/registrations/Converters.java`
  - reviewed upstream legacy converter-registry bridge
- `sed -n '1,320p' src/main/java/org/skriptlang/skript/lang/converter/Converters.java`
  - reviewed the active backend the wrapper would delegate to
- `sed -n '1,240p' src/main/java/org/skriptlang/skript/lang/converter/Converter.java`
  - reviewed active converter flags and signature
- `sed -n '1,240p' src/main/java/org/skriptlang/skript/lang/converter/ConverterInfo.java`
  - reviewed active converter info shape
- `sed -n '1,260p' src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - reviewed existing registrations/classes coverage to avoid overlap
- `sed -n '1,220p' src/main/java/ch/njol/util/StringUtils.java`
  - confirmed the local helper available for `PatternedParser.getCombinedPatterns()`
- `./gradlew test --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - first run failed in `:compileTestJava` because the test used incompatible wildcard generics against `ConverterUtils`
- `./gradlew test --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed after tightening the test generics
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed

## Verification

- `./gradlew test --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --rerun-tasks`
  - passed

## Unresolved Risks

- `PatternedParser` is restored as a usable wrapper base, but local `Classes.registerClassInfo(...)` still does not auto-index literal patterns from `PatternedParser` instances the way upstream did; this slice intentionally left `Classes.java` untouched to stay inside the requested scope fence
- the legacy `NO_COMMAND_ARGUMENTS` converter flag is preserved as the upstream numeric value `8`, but the local runtime does not currently expose the old command-layer constant or command-specific converter gating
- verification is unit-only because this slice restores compatibility plumbing, not user-visible `.sk` behavior

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/classes/Parser.java`
  - `src/main/java/ch/njol/skript/classes/PatternedParser.java`
  - `src/main/java/ch/njol/skript/classes/Converter.java`
  - `src/main/java/ch/njol/skript/registrations/Converters.java`
  - `src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java` was intentionally not changed in this lane
