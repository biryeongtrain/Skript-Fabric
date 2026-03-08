# Lane A Status

Last updated: 2026-03-08

## Scope

- `Statement`
- `ScriptLoader`
- parse-log / loader diagnostics

## Owned Files

- `src/main/java/ch/njol/skript/lang/Statement.java`
- `src/main/java/ch/njol/skript/ScriptLoader.java`
- `src/main/java/ch/njol/skript/log/**`
- `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`

## Goal For Next Session

- Continue `Part 1A` on broader statement orchestration after restoring exact built-in local-variable hint producers for `set`.

## Work Log

### 2026-03-08 Statement Fallback Section-Hint Slice

- compared the local `ScriptLoader.parseSectionTriggerItem(...)` cleanup path against upstream `ScriptLoader.loadItems(...)` from snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` in `/tmp/skript-upstream-ueogiz`
- selected one contained upstream-backed gap that stays inside Lane A ownership:
  - the temporary non-section hint scope for a section line should only be cleared when the entire line fails, not when `Section.parse(...)` fails but later `Statement.parse(...)` succeeds on that same section header
- closed that loader hint-flow gap in `src/main/java/ch/njol/skript/ScriptLoader.java`:
  - `parseSectionTriggerItem(...)` now tracks the final parsed `TriggerItem` instead of only the initial section candidate
  - temporary section-line hints are now cleared only when both the section parse and the statement fallback fail
  - successful statement-managed section lines now keep their parse-time hints available to later sibling lines, matching upstream `item == null` cleanup behavior more closely
- added focused `ScriptLoaderCompatibilityTest` coverage in `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`:
  - registered a statement-managed section expression that publishes an `Integer` local-variable hint while parsing a section line through `Statement.parse(...)`
  - proved a later sibling line `capture hinted integer {_value}` now parses after that section-line statement fallback succeeds
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/statement_fallback_section_hint_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness:
  - the live resource-loader path now verifies that a statement-managed section line loads, its section body executes, and a later sibling `%integer%` line reads the hinted local value
  - this new live regression increased the full Fabric GameTest total from `229 / 229` to `230 / 230`
- changed files in this slice:
  - `src/main/java/ch/njol/skript/ScriptLoader.java`
  - `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
  - `src/gametest/resources/skript/gametest/base/statement_fallback_section_hint_test_block.sk`
- verification:
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
    - passed
  - `./gradlew runGameTest --rerun-tasks`
    - first run failed with `1` required failure because the new GameTest asserted an extra block-placement side effect that was not required to prove the loader-hint behavior
    - trimmed that extra assertion and kept the test focused on the live hinted local-variable path
  - `./gradlew runGameTest --rerun-tasks`
    - passed with `230 / 230` required GameTests completed
- unresolved risks kept open:
  - this slice only restores hint retention when a section header succeeds through `Statement.parse(...)`; broader remaining hint-producer parity still sits in other effect/statement families
  - the live regression proves later sibling parse and execution for one statement-managed section shape, not every possible section-claiming statement/expression combination

### 2026-03-08 Invisible/Visible Condition Slice

- compared the current local tree against upstream `CondIsInvisible` from snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and confirmed Lane B had only landed the invisible/visible effect family, not the matching condition family
- imported the exact upstream invisible/visible condition surface into the active Fabric runtime without renaming the user-facing forms:
  - `%livingentities% (is|are) invisible`
  - `%livingentities% (is|are) visible`
  - `%livingentities% (isn't|is not|aren't|are not) invisible`
  - `%livingentities% (isn't|is not|aren't|are not) visible`
- added `org/skriptlang/skript/bukkit/base/conditions/CondIsInvisible.java` with the upstream negation/tag behavior adapted to the local Fabric event bridge:
  - it keeps the upstream `visible` parse-tag inversion logic
  - it accepts the local `event-entity` compatibility expression by gating on `Entity` at init time and narrowing to `LivingEntity` at check time
- wired the condition into `SkriptFabricBootstrap` with the exact upstream-expanded `%livingentities%` patterns
- extended `InvisibleSyntaxTest` so the targeted unit coverage now proves all four exact condition forms parse on the active runtime alongside the already-landed effect forms
- added real `.sk` coverage in `skript/gametest/condition/invisible_entity_names_entity.sk` and `skript/gametest/condition/visible_entity_names_entity.sk`, then extended `SkriptFabricInvisibleGameTest` to prove:
  - the invisible condition executes through the live resource-loader path and names an actually invisible cow
  - the visible condition executes through the live resource-loader path and names an actually visible cow
  - direct parsed conditions also evaluate correctly against a live Fabric use-entity event handle
- the new live condition coverage increased the full Fabric GameTest total to `223 / 223`

- compared the current loader hint flow against upstream `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and selected one contained shipped-syntax gap that still sits on the loader-orchestration boundary:
  - the built-in `EffChange` syntax `set {_value} to 1` should act as a parse-time local-variable hint producer for later sibling lines, just like the already-green custom hint test harnesses
- closed that built-in hint-producer slice without broadening the parser-owned scope:
  - `EffChange.init(...)` now publishes hint return types through the current `HintManager` when the exact built-in `set %object% to %object%` path targets a hintable local variable
  - `EffChange.execute(...)` now also bypasses stale hinted target conversion for those simple local `SET` lines, so a later built-in `set {_value} to "text"` actually stores the new runtime value instead of being coerced through the earlier hinted target type
  - later sibling lines now see the inferred type from the exact built-in syntax, and a later built-in `set {_value} to "text"` correctly overrides the earlier integer hint with `String`
- added focused `ScriptLoaderCompatibilityTest` coverage proving:
  - `set {_value} to 1` allows a later exact syntax `capture hinted integer {_value}`
  - a later exact syntax `set {_value} to "text"` overrides the earlier hint so `capture hinted text {_value}` also loads and executes
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/built_in_set_local_hint_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness that verifies the built-in `set` syntax loads, dispatches, and exposes the expected typed values on the live resource path
- did not claim hint-flow parity complete: this slice restores the built-in `set` producer only, not the broader remaining built-in effect family
- compared the current lane-owned `Statement` error-retention flow against upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and selected one contained closure slice that stays inside Lane A ownership:
  - when an earlier effect or condition failure logs a higher-quality specific parse error, a later plain `Statement` failure must not replace it with a lower-quality specific error
- `Statement.selectRetainedFailure(...)` now compares later statement-specific failures against the earlier retained effect/condition failure by parse-error quality, matching the upstream single-handler preference more closely:
  - a later statement-specific error only replaces the earlier retained failure when it is strictly higher quality
  - equal-quality or lower-quality later statement failures now leave the earlier effect/condition diagnostic in place
- added `ScriptLoaderCompatibilityTest` regression coverage proving:
  - a same-pattern effect failure logged at `ErrorQuality.NOT_AN_EXPRESSION` still wins over a later same-pattern statement failure that logs a generic specific error
  - the later statement diagnostic and the generic `Can't understand this condition/effect: ...` fallback both stay suppressed on that path
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/higher_quality_parse_error_prefers_effect_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness that verifies `runtime.loadFromResource(...)` keeps the earlier higher-quality effect diagnostic on the live loader path
- preserved the already-closed same-pattern plain-statement fallback after failed effect/condition `init(...)` and did not broaden the slice into parser-owned flows
- the new real `.sk` coverage increased the full GameTest total from `203` to `204`
- compared the current lane-owned `Statement` / parse-log surface against upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and selected one thinner upstream-backed slice that stays inside Lane A ownership:
  - when effect and condition parse failures tie on error quality, the earlier specific effect diagnostic should still win instead of being replaced by the later condition diagnostic
- `ParseLogHandler` now keeps the first retained severe parse error when later errors only tie its quality, matching the upstream parse-log preference more closely
- `Statement.moreRelevantFailure(...)` now also breaks equal-quality effect-versus-condition ties in favor of the earlier retained failure, while preserving the just-closed same-pattern plain-statement fallback after failed effect/condition `init(...)`
- added `ScriptLoaderCompatibilityTest` regression coverage proving:
  - a same-pattern effect and condition pair that both reject initialization with equal-quality specific errors still logs the earlier effect diagnostic
  - the later condition diagnostic and the generic `Can't understand this condition/effect: ...` fallback both stay suppressed on that path
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/equal_quality_parse_error_prefers_effect_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness that verifies `runtime.loadFromResource(...)` keeps the earlier effect parse diagnostic on the live loader path
- preserved the previously closed same-pattern statement fallback after failed effect/condition init and did not claim parity complete: broader equal-quality ordering beyond this effect-versus-condition tie is still thinner than upstream
- compared the lane-owned surface against upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and selected one contained closure slice that is still thinner than upstream:
  - `Statement.parse(...)` should not treat a failed effect or condition parse as terminal when a later plain `Statement` registration can still load the exact same line
- `Statement.parse(...)` now retains effect and condition failure logs, clears those failures, and only restores the best retained parse error if the plain statement path also fails
- preserved the existing function-call short-circuit and the exact existing diagnostic text:
  - successful later plain statements now win over earlier failed effect/condition candidates
  - when nothing matches, the prior specific effect/condition error still beats the generic `Can't understand this condition/effect: ...` fallback
- added `ScriptLoaderCompatibilityTest` regressions proving:
  - a valid plain statement still loads after a same-pattern effect candidate logs an error and rejects initialization
  - a valid plain statement still loads after a same-pattern condition candidate logs an error and rejects initialization
  - a failed same-pattern effect candidate still keeps its specific parse error when no later statement matches
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/statement_fallback_after_failed_effect_set_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness that verifies `runtime.loadFromResource(...)` keeps parsing after the failed effect candidate and the later fallback statement still executes
- the new real `.sk` coverage increased the full GameTest total from `198` to `199`
- added a narrow loader regression for the exact section header syntax `set {_var} to true:` using the existing `EffChange` registration pattern `set %object% to %object%`
- verified that the current loader/log path already retains the specific effect ownership diagnostic for the exact line `set {_var} to true`
- no production code changes were needed for this slice; the new regression passed with the existing `Statement` / `ScriptLoader` behavior
- closed the next upstream `ScriptLoader` gap around parse-time local-variable hint scope lifecycle
- `ScriptLoader.loadItems(...)` now opens a section-level hint scope for each loaded section body, matching the upstream loader model closely enough for nested section parsing
- section-node parsing now also opens a temporary non-section hint scope around the `Section.parse(...)` attempt, so hints created by a failed section parse are cleared before falling back to statement parsing
- loader stop-intent handling now freezes hint propagation after the first stopping item in a scope:
  - stop-trigger paths clear later unreachable hints before the scope exits
  - stop-section paths merge currently known hints into the enclosing resume section before clearing the frozen scope
- added `ScriptLoaderCompatibilityTest` regressions proving:
  - a failed section parse cannot leak local variable hints into later sibling lines
  - a successful section parse can propagate hints into later sibling parsing
  - nested stop-trigger statements prevent later unreachable section-body hints from escaping
  - nested stop-section statements still merge already-known hints into the enclosing resume scope
- did not claim parity complete: actual built-in hint producers outside the test harness are still thinner than upstream and need follow-up wiring in later slices
- preserved the recently closed section-vs-statement fallback diagnostics and plain-condition section-header rejection without reopening comment-aware loader parsing
- updated `ScriptLoader.loadItems(...)` to emit the upstream-style `Unreachable code. The previous statement stops further execution.` warning when a previously loaded `Statement` advertises a stopping `ExecutionIntent`
- gated that warning behind active script context plus `ScriptWarning.UNREACHABLE_CODE` suppression so loader diagnostics stay aligned with script-level warning controls
- added `Statement.loaderExecutionIntent()` so loader-owned warning flow can inspect stop intent without changing the current statement parse ordering
- extended `ScriptLoaderCompatibilityTest` with stopping-statement regressions that cover:
  - warning emission for a later unreachable line
  - suppression via `ScriptWarning.UNREACHABLE_CODE`
  - runtime short-circuit so the later line never executes after the stopping statement
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/base/unreachable_code_warning_stop_test_block.sk` plus a matching `SkriptFabricBaseGameTest` harness that proves the warning is logged during resource load and the unreachable line never runs
- the new real `.sk` coverage increased the full GameTest total from `196` to `197`
- `TriggerItem.walk(...)` now honors `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` when a nested statement returns `false`, unwinding parent sections instead of always falling through to the current section's next item
- `TriggerSection` now exposes loader-visible execution intent derived from its nested trigger items, and `ScriptLoader.loadItems(...)` now treats section items with propagated stop-trigger intent as unreachable-code boundaries too
- extended `ScriptLoaderCompatibilityTest` with section-contained stopping regressions that cover:
  - warning emission when a registered section body contains a stop-trigger statement
  - no unreachable-code warning when a registered section only stops its own body with `stopSection`
  - runtime short-circuit past later sibling items after a nested stop-trigger statement
- compared lane-owned files against upstream `e6ec744dd83cb1a362dd420cde11a0d74aef977d` and pulled the next missing closure-track regression from upstream test `8199-parse exprsecs in function args.sk`
- `Statement.parse(...)` now clears any outer `Section.SectionContext` owner when parsing a plain statement path (`node == null`), so nested function/effect/condition argument parsing cannot inherit an enclosing expression section by mistake
- kept section-node parsing behavior unchanged: only the plain statement parse path now runs through the temporary cleared section ownership wrapper
- added `ScriptLoaderCompatibilityTest` regression coverage proving a plain function-call statement parsed under an already-claimed outer `ExpressionSection` still initializes its argument section expression with `node == null` and `triggerItems == null`
- added real `.sk` coverage in `src/gametest/resources/skript/gametest/expression/plain_effect_argument_inside_outer_section_expression_names_entity.sk` plus a matching `SkriptFabricExpressionGameTest` harness that verifies a nested plain effect argument still parses and runs inside an outer expression section body
- the new real `.sk` coverage increased the full GameTest total from `197` to `198`
- attempted a more direct runtime function-argument `.sk` regression first, but top-level `function ...` structures still fail to parse in the current GameTest runtime; kept that out-of-slice limitation documented and switched the landed real-script coverage to the equivalent nested plain-effect path instead

## Files Changed

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsInvisible.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/InvisibleSyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricInvisibleGameTest.java`
- `src/gametest/resources/skript/gametest/condition/invisible_entity_names_entity.sk`
- `src/gametest/resources/skript/gametest/condition/visible_entity_names_entity.sk`
- `src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java`
- `src/main/java/ch/njol/skript/ScriptLoader.java`
- `src/main/java/ch/njol/skript/lang/Statement.java`
- `src/main/java/ch/njol/skript/lang/TriggerItem.java`
- `src/main/java/ch/njol/skript/lang/TriggerSection.java`
- `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricExpressionGameTest.java`
- `src/gametest/resources/skript/gametest/base/built_in_set_local_hint_test_block.sk`
- `src/gametest/resources/skript/gametest/base/equal_quality_parse_error_prefers_effect_test_block.sk`
- `src/gametest/resources/skript/gametest/base/higher_quality_parse_error_prefers_effect_test_block.sk`
- `src/gametest/resources/skript/gametest/base/statement_fallback_after_failed_effect_set_test_block.sk`
- `src/gametest/resources/skript/gametest/base/unreachable_code_warning_stop_test_block.sk`
- `src/gametest/resources/skript/gametest/expression/plain_effect_argument_inside_outer_section_expression_names_entity.sk`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.InvisibleSyntaxTest --rerun-tasks`
  - first run failed because the new condition rejected the local `event-entity` bridge when it required `LivingEntity` at init time
  - reran after matching the local Fabric compatibility path used by `EffInvisible` and narrowing to `LivingEntity` during `check(...)`; command passed
- `./gradlew runGameTest --rerun-tasks`
  - passed with `223 / 223` required GameTests completed, including the new invisible/visible condition `.sk` coverage
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.builtInSetEffectOverridesEarlierHintsWithLaterStringAssignment --rerun-tasks --info`
  - first run failed with `expected: <text> but was: <null>` because the later local `SET` still executed through the earlier hinted `Integer` target view
  - fixed the runtime side inside `EffChange.execute(...)`, then continued verification on the finished tree
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.builtInSetEffectHintsIntegerLocalsForLaterSiblingLines --tests ch.njol.skript.ScriptLoaderCompatibilityTest.builtInSetEffectOverridesEarlierHintsWithLaterStringAssignment --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed on the finished tree with the new built-in `set` hint regressions included
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - the finished tree completed `206 / 206` required GameTests with the new built-in `set` live-script regression included
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsHigherQualityEffectErrorWhenLaterStatementAlsoFails --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed on the finished tree with the new higher-quality statement-vs-effect regression included
- `./gradlew runGameTest --rerun-tasks`
  - passed with `204 / 204` required tests completed after adding the new real `.sk` loader-diagnostic regression
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsEarlierEffectErrorWhenEffectAndConditionFailuresTie --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - waited for the Fabric Loom cache lock to be released because a concurrent `runGameTest` invocation was already starting, then passed on the finished tree
- `./gradlew runGameTest --rerun-tasks`
  - passed with `200 / 200` required tests completed after adding the new real `.sk` loader regression
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsLetsRegisteredStatementWinAfterFailedEffectParse --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsLetsRegisteredStatementWinAfterFailedConditionParse --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsSpecificEffectParseErrorWhenNoStatementMatches --rerun-tasks`
  - first run failed in the expected pre-fix state because the new effect and condition fallback regressions still stopped inside `Statement.parse(...)`
  - reran after the `Statement.parse(...)` retained-failure fix; command passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed on the finished tree
- `./gradlew runGameTest --rerun-tasks`
  - first run failed with `199` tests complete and `1` required test failed because the new GameTest helper statement wrote to absolute coordinates instead of the GameTest-relative structure
  - reran after fixing the harness to set the block through `GameTestHelper`; passed with `199 / 199` required tests completed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsSpecificSectionOwnershipErrorForSetTrueSyntax --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - passed with `197 / 197` required tests completed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - passed after closing section-level execution-intent propagation
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - first run failed in `:compileTestJava` because the new test-only section scaffolding still needed explicit `toString(...)` implementations
  - reran after adding those `toString(...)` methods; command passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - first run failed in `:compileTestJava` because the new regression still needed the `Classes` import for the registered function signature
  - reran after adding the import; command passed
- `./gradlew runGameTest --rerun-tasks`
  - first run failed while loading a temporary direct function-structure regression script with `IllegalArgumentException: Failed to parse top-level structure or event: function remember_component(component: equippablecomponent)`
  - removed that out-of-slice runtime function-structure coverage, replaced it with the nested plain-effect argument resource, and reran
  - passed with `198 / 198` required tests completed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
  - final rerun on the finished tree passed
- `./gradlew runGameTest --rerun-tasks`
  - final rerun on the finished tree passed with `198 / 198` required tests completed

### 2026-03-08 Feed Effect Slice

- imported the upstream exact feed effect syntax `feed [the] %players% [by %-number% [beef[s]]]` onto the active Fabric runtime
- added [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffFeed.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffFeed.java) with the upstream default-vs-explicit food-level behavior adapted to `ServerPlayer`
- wired the exact runtime registration in [src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java](../../src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java)
- added focused parser/resource coverage in [src/test/java/org/skriptlang/skript/fabric/runtime/FeedSyntaxTest.java](../../src/test/java/org/skriptlang/skript/fabric/runtime/FeedSyntaxTest.java) and [src/test/java/org/skriptlang/skript/fabric/runtime/EffectSyntaxParsingTest.java](../../src/test/java/org/skriptlang/skript/fabric/runtime/EffectSyntaxParsingTest.java)
- added live runtime verification in [src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricFeedGameTest.java](../../src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricFeedGameTest.java) backed by [src/gametest/resources/skript/gametest/effect/feed_event_player_by_beefs_marks_block.sk](../../src/gametest/resources/skript/gametest/effect/feed_event_player_by_beefs_marks_block.sk)
- verification:
  - `./gradlew test --tests org.skriptlang.skript.fabric.runtime.FeedSyntaxTest --tests org.skriptlang.skript.fabric.runtime.EffectSyntaxParsingTest --rerun-tasks`
    - first run failed in `FeedSyntaxTest` because the parsed numeric literal rendered as `[2]`; updated the assertion to read the parsed number value directly and reran
    - final run passed
  - `./gradlew runGameTest --rerun-tasks`
    - passed with `216 / 216` required GameTests completed, including the new live feed-player script coverage
  - the live script proved `feed the event-player by 2 beefs` executed on a survival mock player and raised the player's food level from `5` to `7`

## Unresolved Risks

- the invisible/visible condition now matches the exact upstream visible forms, but the local implementation still relies on the Fabric compatibility bridge accepting broad `Entity`-typed event expressions and narrowing at runtime because `event-entity` is not typed as `LivingEntity`
- this slice restores parse-time hints for the exact built-in `set` path only; broader built-in hint producers such as other change modes and effect families are still thinner than upstream
- the imported feed effect follows upstream’s direct food-level addition semantics, so it does not separately adjust saturation or exhaustion beyond whatever Mojang’s `setFoodLevel(...)` path already does
- the runtime bootstrap still registers `EffChange` through the minimal `set %object% to %object%` compatibility pattern instead of upstream’s richer `%~objects%` target surface, so the local runtime fix is intentionally narrow to hintable simple locals
- coverage proves the higher-quality earlier effect diagnostic beats a later lower-quality statement diagnostic, but it does not yet separately cover the same quality/priority shape for an earlier condition failure versus a later statement failure on the live `.sk` path
- broader upstream parse-error ordering is still open when multiple later plain-statement candidates each log distinct specific diagnostics before final failure selection
- coverage proves the fallback ordering for same-pattern effect/condition candidates that reject during `init(...)`, but it does not yet cover more complex multi-pattern ambiguity where multiple later statement candidates also log distinct specific errors
- this slice closes the equal-quality effect-versus-condition tie, but broader upstream parser/error-priority parity is still open when later plain-statement failures, nested parse errors, or non-owned parser flows introduce competing specific diagnostics
- coverage is intentionally narrow: it proves retained ownership diagnostics for `set {_var} to true:` through the `EffChange` pattern `set %object% to %object%`, but it does not broaden assertion coverage across other `EffChange` patterns
- top-level script `function ...` structure loading is still not available in the current GameTest runtime, so the live `.sk` coverage for this slice uses the equivalent nested plain-effect argument path instead of a direct function-argument script
- the new unit regression covers the upstream function-argument ownership bug directly, but broader runtime parity for script-defined functions remains outside this lane slice

## Merge Notes

- current-cycle conflict surface now also includes `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`, `src/test/java/org/skriptlang/skript/fabric/runtime/InvisibleSyntaxTest.java`, and `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricInvisibleGameTest.java` around the new invisible/visible condition slice
- preserve the exact invisible condition registration and behavior:
  - keep the bootstrap patterns as `%livingentities% (is|are) (invisible|:visible)` and `%livingentities% (isn't|is not|aren't|are not) (invisible|:visible)`
  - keep the upstream-style `visible` parse-tag inversion logic in `CondIsInvisible`
  - keep the local `Entity`-level init gate so `event-entity is invisible/visible` continues to parse on the current Fabric bridge
- current-cycle conflict surface also includes `src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java`, `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`, and `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java` around the new built-in hint-producer path
- preserve the exact `EffChange` behavior:
  - only successful `SET` operations on hintable local variables should publish parse-time hints
  - those same simple local `SET` operations must store the new runtime value without reusing a stale hinted target conversion from an earlier sibling line
  - later built-in `set` lines must be able to overwrite earlier local hints before later sibling parsing
- current-cycle conflict surface now also includes `Statement.java`, `ScriptLoaderCompatibilityTest.java`, and `SkriptFabricBaseGameTest.java` around the new higher-quality statement-vs-effect parse-error retention rule
- preserve the new retained-failure ordering in `Statement.parse(...)`:
  - later plain-statement-specific errors should only replace an earlier effect/condition failure when they are strictly higher quality
  - equal-quality or lower-quality later statement failures must keep the earlier retained effect/condition diagnostic
- current-cycle conflict surface now also includes `ParseLogHandler.java` plus `Statement.java` around the new earlier-equal-quality parse-error retention rule
- likely conflicts for this slice are limited to `Statement.java`, `ScriptLoaderCompatibilityTest.java`, and `SkriptFabricBaseGameTest.java` if another branch changed statement parse ordering, loader-facing diagnostics, or base GameTest registration helpers after lane split
- preserve the new retained-failure ordering in `Statement.parse(...)`:
  - same-pattern plain statements must still load after earlier effect/condition init failures
  - exact specific effect/condition error text must still win when no later statement matches
- likely conflicts are limited to `ScriptLoader.java`, `TriggerItem.java`, `TriggerSection.java`, and `Statement.java` if another branch changed loader warning flow or statement/trigger orchestration after lane split
- current-cycle conflict surface also includes `ScriptLoader.java` plus `ScriptLoaderCompatibilityTest.java` around the new hint-scope lifecycle regressions
- `SkriptFabricBaseGameTest.java` now contains one lane-local loader-warning harness and test-only stopping statement for real `.sk` coverage
- current-cycle conflict surface also includes `Statement.java`, `ScriptLoaderCompatibilityTest.java`, and `SkriptFabricExpressionGameTest.java` around the plain-statement section-context reset regression and its real `.sk` harness
- preserve the already-closed section fallback diagnostics, plain-condition section-header rejection, and existing unreachable-code warning behavior while merging this slice
- no canonical `docs/porting/*.md` files were touched
