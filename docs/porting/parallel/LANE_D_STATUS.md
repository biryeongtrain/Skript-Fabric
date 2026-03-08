# Lane D Status

Last updated: 2026-03-08

## Scope

- `Classes` registry default-expression lookup semantics only
- targeted `ClassesCompatibilityTest` coverage only

## Owned Files

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Goal For This Slice

- restore the missing upstream-backed `Classes.getDefaultExpression(...)` helper surface on top of the already-ported `ClassInfo.defaultExpression(...)` data
- keep the change mergeable and confined to `Classes` plus its compatibility test

## Work Log

- read the required porting docs in the requested order:
  - `docs/porting/README.md`
  - `docs/porting/NEXT_AGENT_HANDOFF.md`
  - `docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
  - `docs/porting/CODEX_PARALLEL_WORKFLOW.md`
  - `docs/porting/CODEX_PARALLEL_PROMPTS.md`
- reviewed the existing lane file, local `Classes.java`, local `ClassesCompatibilityTest.java`, and upstream snapshot `/tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/registrations/Classes.java`
- selected one contained Part 1B gap that stayed inside the owned files:
  - local `ClassInfo` already stored default expressions
  - upstream `Classes` exposed lookup helpers by code name and exact class
  - local `Classes` was missing those helper methods entirely
- added the missing lookup helpers to `Classes`:
  - `Classes.getDefaultExpression(String codeName)`
  - `Classes.getDefaultExpression(Class<T> type)`
- added targeted regression coverage proving:
  - registered default expressions are returned by code name
  - registered default expressions are returned by exact class
  - unregistered exact classes return `null`

## Files Changed

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Counts Changed

- Stage 8 package-local audit counts changed: `0`
- Fabric GameTest counts changed: `0`
- targeted compatibility tests added: `1`

## Exact Commands And Results

- `sed -n '1,220p' docs/porting/README.md`
  - read successfully
- `sed -n '1,220p' docs/porting/NEXT_AGENT_HANDOFF.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CH_NJOL_SKRIPT_AUDIT.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CODEX_PARALLEL_WORKFLOW.md`
  - read successfully
- `sed -n '1,260p' docs/porting/CODEX_PARALLEL_PROMPTS.md`
  - read successfully
- `if [ -f docs/porting/parallel/LANE_D_STATUS.md ]; then sed -n '1,240p' docs/porting/parallel/LANE_D_STATUS.md; else echo '__MISSING__'; fi`
  - existing stale lane file found and reviewed
- `git status --short`
  - clean before edits
- `sed -n '1,260p' src/main/java/ch/njol/skript/registrations/Classes.java`
  - reviewed local implementation
- `sed -n '1,260p' /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/registrations/Classes.java`
  - reviewed upstream implementation
- `sed -n '1,260p' src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - reviewed existing compatibility coverage
- `sed -n '261,520p' src/main/java/ch/njol/skript/registrations/Classes.java`
  - reviewed remainder of local implementation
- `sed -n '261,520p' /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/registrations/Classes.java`
  - reviewed remainder of upstream implementation
- `diff -u /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/registrations/Classes.java src/main/java/ch/njol/skript/registrations/Classes.java | sed -n '1,260p'`
  - confirmed the missing default-expression helper surface
- `sed -n '1,280p' src/main/java/ch/njol/skript/classes/ClassInfo.java`
  - confirmed local `ClassInfo` already stores `DefaultExpression`
- `rg -n "toString\\(|default expression|DefaultExpression|getDefaultExpression|clone\\(" src/main/java/ch/njol/skript/classes/ClassInfo.java /tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/registrations/Classes.java`
  - isolated upstream default-expression helper methods as the contained gap
- `rg -n "getAllSuperClassInfos|getDefaultExpression\\(|toString\\(Object value, StringMode|toString\\(Object\\[] values, boolean and\\)|clone\\(" src/main/java src/test/java`
  - confirmed no existing local helper implementation or direct coverage
- `rg -n "Classes\\.getDefaultExpression|defaultExpression\\(" src/test/java src/main/java`
  - confirmed existing parser-side default-expression data but no `Classes` helper usage
- `sed -n '1,220p' src/main/java/ch/njol/skript/lang/DefaultExpression.java`
  - reviewed interface shape
- `rg -n "class SimpleLiteral|record SimpleLiteral|new SimpleLiteral" src/main/java src/test/java`
  - identified the existing default-expression test helper
- `sed -n '1,220p' src/main/java/ch/njol/skript/lang/util/SimpleLiteral.java`
  - reviewed helper implementation for the new test
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
  - `BUILD SUCCESSFUL`
- `git diff -- src/main/java/ch/njol/skript/registrations/Classes.java src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - reviewed final code diff
- `git status --short`
  - only the owned files were modified

## Verification

- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed

## Unresolved Risks

- this slice restores only the missing default-expression lookup helper surface in `Classes`; it does not close broader upstream `Classes` gaps such as fuller classinfo helper coverage or legacy stringification behavior
- the new coverage is unit-only because the change is registry helper plumbing, not live `.sk` runtime behavior

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
