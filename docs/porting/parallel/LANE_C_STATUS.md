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

- Continue `Part 1B` after the `Classes.getParser(...)` converter-fallback slice.

## Work Log

- fetched the requested upstream snapshot with `git fetch --depth 1 https://github.com/SkriptLang/Skript.git e6ec744dd83cb1a362dd420cde11a0d74aef977d` and compared the owned surface against `FETCH_HEAD`
- selected the next `Classes` compatibility gap from that diff:
  - upstream `Classes.getParser(...)` exposes a converter-backed parser after direct parser lookup fails
  - the local shim had converter-backed `Classes.parse(...)` but still lacked the reusable parser helper
- closed the next `Classes` parser-lookup semantics slice around converter-backed fallback
- `Classes.getParser(...)` now returns a direct registered parser when one exists and otherwise exposes a converter-backed parser built from the matching registered source parser
- added regression coverage proving:
  - `Classes.getParser(...)` now parses the exact text `"parsed-73"` through a registered parser-plus-converter chain
  - that returned parser rejects the exact text `"invalid"` on the same path
  - the earlier converter-backed `UnparsedLiteral` path stays green on the exact text `"bridge-9"`
- did not run GameTest for this slice because the current runtime bootstrap still does not call `Classes.getParser(...)`; verification stayed on the parser-facing unit surface
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
  - confirmed the lane-owned surface still differed materially from upstream; the next selected owned-file gap was missing converter-backed fallback inside `Classes.getParser(...)`
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --rerun-tasks`
  - passed
  - exercised the exact parser texts `"parsed-73"`, `"invalid"`, and `"bridge-9"`

## Unresolved Risks

- no live runtime caller currently uses `Classes.getParser(...)`, so this slice is verified through direct registry/parser unit coverage rather than real `.sk` or GameTest coverage
- broader upstream `Classes` parity is still open beyond this slice, including parse-log-aware `parseSimple(...)`, exact-parser helper parity, and user-input-pattern handling

## Merge Notes

- low conflict surface:
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- no cross-lane owned-file overlap was required for this slice
