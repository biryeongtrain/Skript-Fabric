# Lane D Status

Last updated: 2026-03-08

## Scope

- `ch/njol/skript/lang/function` runtime compatibility only
- no parser, loader, variables, or user-visible syntax import work
- no changes outside the lane-owned status doc and function compatibility files

## Owned Files

- `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Goal For This Slice

- restore the upstream-backed `DynamicFunctionReference` validation-cache behavior so different expression inputs with the same return types do not share an incorrect cached validation result
- keep the slice contained to function runtime behavior and focused unit coverage
- avoid overlap with parser, loader, variable, or new syntax import work

## What Landed

- tightened `DynamicFunctionReference.Input` equality and hash behavior so the validation cache distinguishes different expression arrays instead of collapsing everything to return-type-only keys
- restored the missing behavior where a later plural expression no longer reuses an earlier single-expression validation result for the same function signature
- added a focused regression test proving `DynamicFunctionReference.validate(...)` rejects a plural string expression after a prior successful single-string validation against the same single-parameter function

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Counts Changed

- Stage 8 package-local audit counts changed: `0`
- Fabric GameTest counts changed: `0`
- source files added: `0`
- test files added: `0`
- test methods added: `1`
- canonical docs changed: `0`

## Exact Commands And Results

- `sed -n '1,220p' docs/porting/README.md`
  - read successfully
- `sed -n '1,220p' docs/porting/NEXT_AGENT_HANDOFF.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
  - read successfully
- `sed -n '1,240p' docs/porting/CODEX_PARALLEL_WORKFLOW.md`
  - read successfully
- `if [ -f docs/porting/parallel/LANE_D_STATUS.md ]; then sed -n '1,240p' docs/porting/parallel/LANE_D_STATUS.md; else echo '__MISSING__'; fi`
  - confirmed a stale prior `LANE_D_STATUS.md` existed and needed replacement for this batch
- `git status --short`
  - working tree was clean before edits
- `rg --files src/main/java/ch/njol/skript/lang src/main/java/ch/njol/skript/expressions src/test/java/ch/njol/skript/lang src/test/java/ch/njol/skript/expressions | rg 'InputSource|TriggerSection|ExprInput|lang/function|InputSourceCompatibilityTest|Function.*CompatibilityTest'`
  - confirmed the in-scope source and test files
- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/InputSource.java src/main/java/ch/njol/skript/lang/InputSource.java || true`
  - reviewed local-vs-upstream `InputSource` delta
- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/TriggerSection.java src/main/java/ch/njol/skript/lang/TriggerSection.java || true`
  - reviewed local-vs-upstream `TriggerSection` delta
- `diff -ru /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function src/main/java/ch/njol/skript/lang/function || true`
  - reviewed local-vs-upstream function-surface delta and selected a contained runtime cache gap
- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/expressions/ExprInput.java src/main/java/ch/njol/skript/expressions/ExprInput.java || true`
  - reviewed local-vs-upstream `ExprInput` delta
- `sed -n '1,260p' src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java`
  - reviewed existing input-source coverage
- `sed -n '1,320p' src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`
  - reviewed existing function core coverage
- `sed -n '1,360p' src/test/java/ch/njol/skript/lang/function/FunctionImplementationCompatibilityTest.java`
  - reviewed existing function implementation coverage
- `sed -n '1,360p' src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
  - reviewed current function-call coverage and found room for a dynamic-reference cache regression test
- `sed -n '1,280p' src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
  - reviewed the local cache-key implementation
- `sed -n '1,240p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
  - confirmed upstream differentiates cached validation inputs more precisely than the local implementation
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - first run failed in `:compileTestJava` because the new test used `assertNull(...)` without importing it
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed after adding the missing import
- `git diff --stat`
  - confirmed the slice is limited to two code files plus this lane status file
- `git diff -- src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
  - reviewed the final patch before commit

## Verification

- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Unresolved Risks

- this slice restores only the dynamic-reference validation-cache discrimination path; broader local-vs-upstream differences in `DynamicFunctionReference` remain, including script-source retention and contract-aware return typing
- verification is unit-only because the behavior changed is internal function validation/runtime plumbing, not live `.sk` syntax execution

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
  - `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `InputSource.java`, `ExprInput.java`, and `TriggerSection.java` were reviewed but intentionally left untouched in this slice
