# Lane C Status

Last updated: 2026-03-08

## Scope

- Part 1B variables compatibility only
- exact upstream-backed closure slice: restore `ch/njol/skript/variables/TypeHints.java` as a live bridge onto the current `HintManager` / `ParserInstance` path

## Owned Files

- `src/main/java/ch/njol/skript/variables/HintManager.java`
- `src/main/java/ch/njol/skript/variables/TypeHints.java`
- `src/test/java/ch/njol/skript/variables/TypeHintsCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- restore the upstream legacy `TypeHints` API surface with faithful `add`, `get`, `enterScope`, `exitScope`, and `clear` methods
- make that legacy API affect the current local-variable hint lookup path used by `Variable.newInstance(...)`
- avoid reopening parser, statement, classes, config, structures, or storage-map work

## Work Log

- compared the local variables package against upstream snapshot `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/variables/TypeHints.java`
- confirmed the local tree had no `TypeHints` class and that parse-time local hint narrowing currently flows through `ParserInstance.get().getHintManager()`
- extended `HintManager` with package-local scope helpers so the legacy bridge can inherit outer hints on enter while discarding inner overrides on exit instead of using the normal merge-back section flow
- restored `src/main/java/ch/njol/skript/variables/TypeHints.java` as a compatibility bridge instead of a disconnected local stack:
  - `add(String, Class<?>)` now writes through to the live `HintManager`
  - `get(String)` now reads the visible hint from the live `HintManager`
  - `enterScope()` now opens a copied child hint scope
  - `exitScope()` now drops that child scope without merging it back
  - `clear()` now resets the bridge to one empty scope, matching the upstream legacy surface
- normalized legacy variable names in the bridge so calls like `TypeHints.add("{_value}", String.class)` still drive the current `_value` lookup path
- added focused unit coverage proving the restored legacy API changes what `Variable.newInstance(...)` resolves through the active parser runtime
- did not broaden into `VariablesMap` or storage imports
- did not claim parity complete
- did not update Stage 8 counts
- did not add GameTests because this slice restores parser-time compatibility API behavior only; the new runtime effect is covered directly in unit tests

## Files Changed

- `src/main/java/ch/njol/skript/variables/HintManager.java`
- `src/main/java/ch/njol/skript/variables/TypeHints.java`
- `src/test/java/ch/njol/skript/variables/TypeHintsCompatibilityTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Stage 8 package-local audit counts changed: `0`
- canonical porting doc counts changed: `0`
- Java source file count changed in `src/main/java/ch/njol/skript/variables`: `1` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/variables`: `1` added, `0` modified
- real `.sk` fixture count changed: `0`

## Verification

- `./gradlew test --tests ch.njol.skript.variables.TypeHintsCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.variables.VariablesCompatibilityTest --rerun-tasks`
  - passed
  - verified the restored legacy `TypeHints` bridge plus the existing local-variable hint and list-variable compatibility suites

## Unresolved Risks

- `TypeHints` now shares the active `HintManager`, so legacy `clear()` also clears any currently visible parse-time local hints in that parser instance; that matches the old global intent but could matter if a later mixed API path expects isolated stores
- `TypeHints.get(String)` returns the currently visible single hint from the live hint set; if a future slice intentionally stores multiple live hints for the same local variable through the newer API, the legacy single-class surface remains inherently lossy
- this slice restores only the legacy hint API bridge and does not import the broader upstream variable storage subsystem

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/variables/HintManager.java`
  - `src/main/java/ch/njol/skript/variables/TypeHints.java`
  - `src/test/java/ch/njol/skript/variables/TypeHintsCompatibilityTest.java`
- no overlap expected with active parser or statement lanes under the current ownership matrix
