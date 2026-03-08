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

- Continue `Part 1B` from the restored class-alias bridge and pick the next smallest lane-owned blocker that unlocks one more exact upstream live form.

## Work Log

- compared the current local `Classes` compatibility layer against upstream `e6ec744` and selected one contained blocker with direct user-visible impact:
  - upstream `ClassInfo` supports regex-backed `.user(...)` aliases that `Classes.getClassInfoFromUserInput(...)` uses for typed class names and parser-facing placeholder type lookup
  - the local compatibility layer still only matched normalized code names, so exact upstream alias forms like `material` remained dead on those lookup paths
- closed that alias bridge in the lane-owned compatibility surface:
  - `ClassInfo` now stores regex-backed `.user(...)` patterns and exposes `matchesUserInput(...)`
  - `Classes.getClassInfoFromUserInput(...)` now checks registered user patterns before falling back to normalized code-name matching
  - `Classes.isPluralClassInfoUserInput(...)` now recognizes common `s` / `es` / `ies` plural forms for regex-backed aliases instead of only compact code names
- wired one shipped class-info registration through that restored path:
  - `ItemTypeClassInfo` now registers the upstream-style aliases `item ?types?` and `materials?`
- used that bridge to unlock one exact live script form:
  - a real `.sk` resource can now load a registered syntax pattern that declares `%material%` and execute it against the built-in item-type parser
- added focused coverage proving:
  - regex-backed alias lookup resolves singular and plural user input
  - the real resource-loader path accepts `capture material alias stone` and executes through `%material%`
- lane-local verification needed one follow-up product fix:
  - the first alias unit test passed lookup but exposed that plural detection still only understood normalized code names; patched that heuristic before the final green pass
- did not claim parity complete

## Files Changed

- `src/main/java/ch/njol/skript/classes/ClassInfo.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/org/skriptlang/skript/bukkit/base/types/ItemTypeClassInfo.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/resources/skript/gametest/base/material_alias_placeholder_set_test_block.sk`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `git grep -n "\\.user(" e6ec744dd83cb1a362dd420cde11a0d74aef977d -- 'src/main/java/**/*.java' | sed -n '1,260p'`
  - passed
  - confirmed upstream still relies on `.user(...)` aliases broadly, including `materials?` on item-type class infos
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - failed first
  - the new regex-alias test exposed that `Classes.isPluralClassInfoUserInput(...)` still only recognized normalized code-name plurals
- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.SilentSyntaxTest --tests org.skriptlang.skript.fabric.runtime.InvulnerableSyntaxTest --tests org.skriptlang.skript.fabric.runtime.AliveKillSyntaxTest --rerun-tasks`
  - failed
  - concurrent lane-local Gradle execution collided while writing test XML results for `AliveKillSyntaxTest`; this was an execution conflict, not a product regression
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
  - passed
  - validated regex-backed alias lookup plus plural detection for registered class infos
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - verified the full Fabric GameTest suite with the new real `.sk` `%material%` fixture green on the live loader path

## Unresolved Risks

- broader upstream class-alias parity is still open beyond this slice:
  - only the compatibility bridge plus `itemtype` / `material(s)` alias wiring landed here
  - richer regex plural handling beyond the current `s` / `es` / `ies` heuristic is still thinner than upstream
- the real `.sk` GameTest proves live `%material%` placeholder loading and execution, not every remaining class-alias consumer such as future typed-input or additional alias-heavy class infos

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/classes/ClassInfo.java`
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- added real-script fixture:
  - `src/gametest/resources/skript/gametest/base/material_alias_placeholder_set_test_block.sk`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
- note:
  - `SkriptFabricBaseGameTest.java` is outside the lane's primary source ownership matrix and may need manual merge attention if another lane also touched shared GameTest scaffolding
