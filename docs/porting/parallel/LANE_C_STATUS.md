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

- Continue `Part 1B` after the validator-backed `options:` and `SectionNode` config slice.

## Work Log

- closed a deeper `Variables` runtime semantics slice around natural variable-name ordering for list/prefix iteration
- ported the upstream-style numeric-aware comparator into `Variables.getVariablesWithPrefix(...)` so list keys like `2` and `10` no longer sort lexically as `10`, `2`
- verified the runtime impact at three levels:
  - direct list variable array/key exposure now returns numeric-like keys in natural order
  - parsed `set {target::*} to {source::*}` now reindexes source keys using that natural order
  - real `.sk` GameTest coverage now proves `{source::2}` lands in `{target::1}` ahead of `{source::10}`
- did not touch canonical docs or parser/statement-owned files
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/variables/Variables.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/list_variable_numeric_order_set_test_block.sk`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - `196` game tests completed
  - `196 / 196` required tests passed

## Merge Notes

- likely conflict surface:
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- low conflict surface:
  - `src/main/java/ch/njol/skript/variables/Variables.java`
  - `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- new resource file added:
  - `src/gametest/resources/skript/gametest/base/list_variable_numeric_order_set_test_block.sk`
