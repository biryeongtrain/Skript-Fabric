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

- Continue `Part 1B` after the variable-name validation slice, keeping the restored `%...%` / `*` compatibility forms green.

## Work Log

- compared the current local variable-name validator against upstream `e6ec744` and selected one contained compatibility gap:
  - upstream `Variable.isValidVariableName(...)` ignores `*` characters that appear inside paired `%...%` expression spans
  - the local compatibility layer still treated any non-terminal `*` as invalid, so forms like `result::%{source::*}%` were rejected even though the only asterisk was inside the nested expression
- closed that validation slice in `Variable.isValidVariableName(...)`
- `Variable` now counts only asterisks outside paired `%...%` spans when validating list-variable markers
- restored accepted forms:
  - `result::%{source::*}%`
  - `result::%{source::*}%::*`
- kept rejected forms:
  - `result::%{source::*}%*`
  - `result::*::tail`
- added focused compatibility coverage proving:
  - `Variable.isValidVariableName(...)` accepts the restored forms and still rejects invalid outer list-variable asterisks
  - `Variable.newInstance(...)` rejects the invalid outer-asterisk form directly
  - parser-facing variable-expression coverage still recognizes the restored accepted forms
- added one real `.sk` GameTest fixture that loads and executes a script containing `set {result::%{source::*}%} to "ignored"`
- lane-local verification work needed two follow-up test-only fixes before the final green pass:
  - the first `VariableCompatibilityTest` draft used a parser-level rejection assertion that was broader than the product change, because the outer parser can still fall through to a non-variable expression after invalid variable parsing
  - the first two GameTest passes failed due swapped neighboring base-test block expectations during lane-local editing, not due product-code regressions
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/lang/Variable.java`
- `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/variable_name_expression_inner_list_marker_set_test_block.sk`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `git show e6ec744dd83cb1a362dd420cde11a0d74aef977d:src/main/java/ch/njol/skript/lang/Variable.java | sed -n '90,160p'`
  - passed
  - confirmed upstream discounts `*` characters inside paired `%...%` spans before validating the final outer `::*` marker
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - failed first
  - the first rejection assertion targeted the outer parser instead of `Variable.newInstance(...)`, and the outer parser can still fall through to a non-variable expression after invalid variable parsing
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed
  - validated the restored accepted/rejected variable-name forms through `Variable.isValidVariableName(...)`, `Variable.newInstance(...)`, and parser acceptance coverage
- `./gradlew runGameTest --rerun-tasks`
  - failed first
  - the new GameTest fixture assertion expected the wrong block after a lane-local test edit
- `./gradlew runGameTest --rerun-tasks`
  - failed second
  - neighboring base GameTest assertions were still temporarily pointed at swapped block expectations; fixed in lane-local test code
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - verified the full Fabric GameTest suite with `206 / 206` scheduled and the new real `.sk` fixture green

## Unresolved Risks

- broader upstream variable-name parity is still open beyond this slice, especially list-variable colon checks inside `%...%` expressions and the thinner local `VariableString` runtime for dynamic variable names
- the new GameTest proves load-and-execute acceptance for the restored form, not full upstream runtime evaluation parity for `%...%` inside variable names

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/Variable.java`
  - `src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- added real-script fixture:
  - `src/gametest/resources/skript/gametest/base/variable_name_expression_inner_list_marker_set_test_block.sk`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
- note:
  - `SkriptFabricBaseGameTest.java` is outside the lane's primary source ownership matrix and may need manual merge attention if another lane also touched shared GameTest scaffolding
