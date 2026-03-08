# Lane E Status

Last updated: 2026-03-08

## Scope

- dependency-closure support only for `Variables` / `HintManager` / `Variable` / `Classes`
- excluded array cloning and parser-source metadata because those were already merged
- landed one contained upstream-backed behavior fix that helps local variable hint compatibility

## Owned Files

- `src/main/java/ch/njol/skript/variables/HintManager.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Goal For This Slice

- compare the local compatibility bridge against `/tmp/skript-upstream-e6ec744-2`
- find one remaining observable mismatch inside the allowed dependency-closure surface
- add focused regression coverage, implement the smallest fix, and verify with the narrowest relevant test command

## What Landed

- identified an upstream mismatch in `HintManager.clearScope(level, true)`
- restored upstream section-scope semantics so section-only clearing removes the targeted section frame instead of just emptying its hint map
- added a regression in `VariableCompatibilityTest` proving a removed section scope no longer leaks copied local variable hints back into later parsing scopes

## Files Changed

- `src/main/java/ch/njol/skript/variables/HintManager.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `docs/porting/parallel/LANE_E_STATUS.md`

## Counts Changed

- source files added: `0`
- source files modified: `1`
- test files added: `0`
- test files modified: `1`
- test methods added: `1`
- canonical docs changed: `0`

## Exact Commands And Results

- `git status --short --branch`
  - branch was `codex/lane-e-20260308m`
- `git rev-parse HEAD && git merge-base --is-ancestor 7b27f6bc3 HEAD; echo $?`
  - current HEAD was `7b27f6bc3572e37d29756fb2d99507fa8c2e979a`
  - merge-base check returned `0`
- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/variables/HintManager.java src/main/java/ch/njol/skript/variables/HintManager.java`
  - reviewed the local-vs-upstream `HintManager` delta and selected the section-scope clear mismatch
- `diff -u /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/Variable.java src/main/java/ch/njol/skript/lang/Variable.java`
  - reviewed the variable-side consumer behavior to build an observable regression through `Variable.newInstance(...)`
- `sed -n '1,420p' src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
  - reviewed existing variable compatibility coverage and added a missing section-scope regression
- `sed -n '130,470p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/variables/HintManager.java`
  - confirmed upstream `clearScope(level, true)` removes a section scope entry rather than clearing its hints
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest.clearingSectionOnlyScopeDropsRemovedSectionHintsFromLaterScopes`
  - failed before the fix
  - failure: `VariableCompatibilityTest > clearingSectionOnlyScopeDropsRemovedSectionHintsFromLaterScopes() FAILED`
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest`
  - passed after the fix

## Verification

- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest`
  - passed

## Unresolved Risks

- this slice only restores the section-only `HintManager.clearScope(...)` behavior; broader local-vs-upstream compatibility differences still remain across the trimmed variable/class bridge
- verification stayed at the focused variable compatibility level; no wider parser or loader suite was rerun in this lane

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/HintManager.java`
  - `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `Variables.java`, `Variable.java`, and `Classes.java` were compared during triage but intentionally left untouched in this slice
