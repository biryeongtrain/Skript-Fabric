# Lane C Status

Last updated: 2026-03-09

## Scope

- Variables/Classes/config/structures only
- prioritize classinfo/parser registry parity or deeper variable semantics
- find exactly one upstream-backed mismatch after literal-only `getPatternInfos` closure

## Owned Files

- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionOverloadDisambiguationTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- locate one concrete upstream-backed mismatch in `Classes`/`Variables`/`config`/`structures`
- land a narrow fix with a focused regression and verification commands

## Work Log

- compared local `Classes.getPatternInfos(...)` with upstream `ch/njol/skript/registrations/Classes#getPatternInfos`
- mismatch found: local implementation re-ordered explicit literal-pattern matches by sorted class-info specificity/dependencies; upstream returns matches in registration order
- reproduced via `ClassesCompatibilityTest`: expected order should reflect registration order when the same literal pattern is declared on multiple `ClassInfo`s
- applied minimal fix: return `List.copyOf(explicitMatches)` without reordering; keep literal-only behavior (no parser fallback) intact

## Files Changed

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/registrations`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (before fix): `getPatternInfos("shared")` returned class-infos in sorted order, not in registration order
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - expected: `explicitLiteralPatternMatchesUseRegistrationOrderForLiterals` passes, asserting `[beta, alpha, gamma]`
- After fix: command above passes; no changes to parser fallback behavior (still literal-only)

## Unresolved Risks

- none observed within this narrow surface; broader `Classes`/parser registry parity remains ongoing

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
