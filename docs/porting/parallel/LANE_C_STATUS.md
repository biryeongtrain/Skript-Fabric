# Lane C Status

Last updated: 2026-03-08

## Scope

- `Variables`
- `Classes`
- `config`
- `structures`

## Owned Files

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/structures/**`
- matching tests in `src/test/java/ch/njol/skript/**`

## Goal For Next Session

- Continue `Part 1B` after the `Classes.parseSimple(...)` parse-log fallback slice.

## Work Log

- fetched the requested upstream snapshot with `git fetch --depth 1 https://github.com/SkriptLang/Skript.git e6ec744dd83cb1a362dd420cde11a0d74aef977d` and compared the owned surface against `FETCH_HEAD`
- selected the next `Classes` compatibility gap from that diff:
  - upstream `Classes.parseSimple(...)` and `Classes.parse(...)` clear retained parse-log errors between parser attempts and before successful fallback branches
  - the local shim still parsed values correctly, but a failed earlier parser could leak a stale error into the surrounding parse log even when a later parser or converter branch succeeded
- closed the next `Classes` parse-log semantics slice around successful direct and converter fallback
- `Classes` now exposes a compatibility `parseSimple(...)` helper that preserves the current primitive bridge and clears retained parse-log state before each registered parser attempt
- `Classes.parse(...)` now routes through that helper and clears retained direct-parse diagnostics before successful converter-backed fallback, so stale earlier parser errors do not survive a later successful parse path
- added regression coverage proving:
  - `Classes.parseSimple(...)` no longer leaks the exact error text `"first parser should not leak"` when a later compatible parser succeeds on the exact text `"fallback-14"`
  - `Classes.parse(...)` no longer leaks the exact error text `"direct parser should not leak"` when converter-backed fallback succeeds on the exact text `"convert-28"`
  - the earlier converter-backed `UnparsedLiteral` path stays green on the exact text `"bridge-9"`
- did not run GameTest for this slice because the change stays inside the `Classes` parser helper's retained-log handling and does not change syntax strings, loader behavior, or structure execution; verification stayed on the targeted parser-facing unit surface
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `git fetch --depth 1 https://github.com/SkriptLang/Skript.git e6ec744dd83cb1a362dd420cde11a0d74aef977d`
  - passed
  - fetched the requested upstream snapshot to `FETCH_HEAD`
- `git diff --stat FETCH_HEAD -- src/main/java/ch/njol/skript/variables src/main/java/ch/njol/skript/config src/main/java/ch/njol/skript/structures src/main/java/ch/njol/skript/registrations/Classes.java src/test/java/ch/njol/skript`
  - passed
  - confirmed the lane-owned surface still differed materially from upstream; the next selected owned-file gap was missing parse-log-aware direct/converter fallback handling inside `Classes.parseSimple(...)` / `Classes.parse(...)`
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --rerun-tasks`
  - passed
  - exercised the exact parse texts `"fallback-14"`, `"convert-28"`, and `"bridge-9"`

## Unresolved Risks

- broader upstream `Classes` parity is still open beyond this slice, including exact-parser helper parity and regex-backed user-input-pattern handling
- this slice proves retained parse-log cleanup through the helper surface, but it does not yet add a live `.sk` regression around a runtime caller that reaches `Classes.parse(...)`

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
- no cross-lane owned-file overlap was required for this slice
