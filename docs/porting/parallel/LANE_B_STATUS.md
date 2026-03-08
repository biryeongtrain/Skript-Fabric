# Lane B Status

Last updated: 2026-03-08

## Scope

- `lang-core` statement orchestration only
- allowed files only:
  - `src/main/java/ch/njol/skript/lang/Statement.java`
  - `src/main/java/ch/njol/skript/lang/Condition.java`
  - `src/main/java/ch/njol/skript/lang/Effect.java`
  - `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/lang/ExpressionSectionCompatibilityTest.java`
  - `docs/porting/parallel/LANE_B_STATUS.md`

## Goal For This Slice

- compare local statement/effect/condition orchestration against `/tmp/skript-upstream-e6ec744-2`
- land one contained observable gap outside the loader retained-log bug owned by Lane A
- prioritize section fallback / ownership behavior

## Latest Slice

- compared local `Statement.java`, `Condition.java`, and `Effect.java` against `/tmp/skript-upstream-e6ec744-2`
- identified one open ownership mismatch in the local effect fallback path:
  - `Statement.parse(...)` already resets `SectionContext.owner` between registered statement candidates
  - `Effect.parse(...)` did not reset section ownership between registered effect candidates on section lines
  - result: an earlier failing effect candidate could leave an expression-section claim behind, and a later literal effect candidate could be accepted as if it owned the section
- restored the local behavior in one contained fix:
  - `Effect.parse(...)` now wraps the effect iterator for section-line parsing
  - before each candidate, it clears `SectionContext.owner` and `ownerErrorRepresentation`
  - plain effect parsing without a section node is unchanged

## Regression Added

- `ScriptLoaderCompatibilityTest.loadItemsDoesNotLeakSectionOwnershipAcrossEffectCandidates()`
  - registers a section-claiming expression plus two overlapping effects
  - the first effect claims the section through `%object%` and fails init
  - the second effect is a plain literal fallback
  - verifies the section line is rejected instead of being accepted through leaked ownership from the first candidate

## Files Changed

- `src/main/java/ch/njol/skript/lang/Effect.java`
- `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Exact Commands And Results

- upstream comparison before the slice:
  - `sed -n '1,260p' src/main/java/ch/njol/skript/lang/Statement.java`
  - `sed -n '1,260p' src/main/java/ch/njol/skript/lang/Condition.java`
  - `sed -n '1,260p' src/main/java/ch/njol/skript/lang/Effect.java`
  - `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/Statement.java`
  - `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/Condition.java`
  - `sed -n '1,260p' /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/Effect.java`
  - result:
    - local statement parsing already had per-candidate ownership reset for registered statements
    - local effect parsing did not
- targeted verification after the slice:
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - result:
    - passed
    - `build/test-results/test/TEST-ch.njol.skript.ScriptLoaderCompatibilityTest.xml`: `31` tests, `0` failures, `0` errors, `0` skipped

## Unresolved Risks

- condition fallback still has its own candidate-ordering and retained-diagnostic behavior; this slice intentionally did not broaden into `Condition.java`
- the emitted retained error for multi-candidate effect failures is still governed by parse-log priority selection; this slice only restores ownership isolation between effect candidates

## Merge Notes

- conflict surface is limited to `src/main/java/ch/njol/skript/lang/Effect.java` and `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
- this slice does not touch `ScriptLoader.java`, syntax imports, canonical `docs/porting/*.md`, or any files outside the allowed closure
