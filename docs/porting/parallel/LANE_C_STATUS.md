# Lane C Status

Last updated: 2026-03-08

## Scope

- stay inside the `lang-core` parser/input-source closure
- compare local `InputSource.parseExpression(...)` behavior against `/tmp/skript-upstream-e6ec744-2`
- restore one contained upstream-visible input-source/runtime gap without widening into syntax imports or non-owned files

## Owned Files

- `src/main/java/ch/njol/skript/lang/InputSource.java`
- `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- verify whether the local `InputSource.parseExpression(...)` still diverges from upstream after the recent parser-source/default-value work
- if so, land the smallest fix and focused regression coverage inside the owned input-source surface

## Work Log

- compared local `src/main/java/ch/njol/skript/lang/InputSource.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/InputSource.java`
- confirmed the local file had an extra `isBareStringLiteral(...)` rejection branch that does not exist upstream
- traced the upstream behavior through `SkriptParser.parseExpression(Class<? extends T>... types)` in `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/SkriptParser.java`
- confirmed upstream input-source parsing falls back to plain literal parsing when an input-shaped form does not produce a usable `ExprInput`
- added focused coverage in `InputSourceCompatibilityTest` for:
  - bare string literal mappings such as `plain text`
  - source restoration while parsing a plain literal such as `anything`
  - literal fallback for `input index` when the source has no indices
  - literal fallback for plural typed-input text such as `foos input`
- removed the local-only bare-string veto from `InputSource.parseExpression(...)`
- kept the existing `LiteralUtils` defense path and `InputData` restoration behavior unchanged

## Files Changed

- `src/main/java/ch/njol/skript/lang/InputSource.java`
- `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Stage 8 package-local audit counts changed: `0`
- canonical porting doc counts changed: `0`
- Java source file count changed in `src/main/java/ch/njol/skript/lang`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/lang`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --rerun-tasks`
  - first run failed before the runtime fix
  - failing assertion was the new bare-literal regression in `InputSourceCompatibilityTest`
- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --rerun-tasks`
  - passed after removing the local-only `isBareStringLiteral(...)` rejection
  - verified bare literal fallback, invalid input-form literal fallback, typed input resolution, indexed input resolution, and source restoration

## Unresolved Risks

- this slice only restores the upstream literal-fallback path in `InputSource.parseExpression(...)`
- broader upstream input-source syntax packages and downstream consumers of `getDependentInputs()` remain outside this lane

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/InputSource.java`
  - `src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
- no overlap expected with parser-data or default-value work unless another lane is editing the same input-source compatibility file
