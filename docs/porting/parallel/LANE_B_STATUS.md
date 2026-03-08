# Lane B Status

Last updated: 2026-03-08

## Scope

- `SkriptParser`
- `patterns`
- parser tag / mark parity

## Owned Files

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/**`
- parser-facing tests in `src/test/java/ch/njol/skript/**`

## Goal For Next Session

- Continue `Part 1A` on the remaining parser parity after the safe explicit default-value slice, without claiming parser-pattern parity complete.

## Work Log

- closed the next parser-owned tag parity slice around ordered duplicate-preserving parse tags
- upstream snapshot `e6ec744dd83cb1a362dd420cde11a0d74aef977d` still keeps parse tags as ordered lists through `patterns.MatchResult` and `SkriptParser.ParseResult`, while the local shim had collapsed them into a set
- `SkriptPattern.match(...)` now accumulates parse tags in encounter order without deduplicating repeated tags
- `patterns.MatchResult` and `SkriptParser.ParseResult` now expose those tags as ordered `List<String>` values again while keeping `hasTag(...)` behavior unchanged for existing callers
- added regressions proving:
  - direct shared-matcher patterns preserve repeated tags in order for exact syntax `repeat:alpha unique:beta repeat:gamma`
  - parser-initialized syntax elements receive the same ordered duplicate-preserving tag list through `ParseResult.tags`
- live parser behavior changed in a parser-core path, so the existing real `.sk` GameTest corpus was rerun instead of adding a new dedicated `.sk` fixture:
  - no currently shipped runtime syntax consumes duplicate ordered tags yet
  - the rerun confirms this parity closure stays inert for the active natural-script corpus
- did not mark parity complete: broader upstream parser tag/mark behavior is still richer than the current compatibility surface even after restoring ordered duplicate-preserving tag accumulation
- closed the safe parser-owned subset of omitted-placeholder default-value parity in `SkriptParser`
- `SkriptParser.parseModern(...)` and `parseStatic(...)` now backfill omitted non-optional placeholder captures from `DefaultValueData` when a valid parser default value exists for the placeholder return type
- intentionally kept the fallback additive instead of fully enforcing upstream failure semantics when no default exists:
  - current local runtime still has omitted-group syntaxes that remain green by receiving `null`
  - local `ClassInfo` still does not expose upstream `defaultExpression(...)`
- restored the private `SkriptParser.match(String, String, ParseContext, int)` bridge after the first GameTest rerun exposed that the current GameTest helper reflects that exact method signature
- added parser regressions anchored to the exact upstream syntax string `default number [%number%]` from `ch/njol/skript/test/runner/ExprDefaultNumberValue.java`
- added coverage proving:
  - the compiled matcher still leaves the omitted `%number%` capture as `null` for `default number`
  - the registry-backed parser now injects an explicit parser default value for `default number`
  - explicit input still wins for `default number 5`
- did not add a new real `.sk` fixture:
  - current shipped runtime still does not register `DefaultValueData` into any live syntax path
  - reran the existing GameTest corpus instead to confirm the slice stays inert for the current live `.sk` coverage
- closed the next shared-matcher placeholder-metadata slice in `PatternCompiler` / `SkriptPattern`
- `PatternCompiler` now parses and preserves placeholder metadata beyond raw return types:
  - literal-only / expression-only parse flags via `*` and `~`
  - placeholder optional-marker metadata via leading `-`
  - placeholder plural metadata for graph/API parity
  - placeholder `@time` metadata
- `TypePatternElement` now exposes the upstream-style metadata needed by the current compatibility surface:
  - `pluralities()`
  - `flagMask()`
  - `time()`
  - `isOptional()`
- `SkriptPattern.match(...)` now applies placeholder-local parse flags and `@time` state when turning captured text into expressions, so patterns like `%*integer%`, `%~integer%`, and `%string@1%` now behave through the shared live parser path instead of silently flattening to unrestricted `%type%`
- added parser regressions proving:
  - literal-only placeholders reject registered expressions but still accept literals
  - expression-only placeholders reject literals but still accept registered expressions
  - placeholder `@time` metadata reaches the parsed expression instance
  - the lightweight `TypePatternElement` graph now exposes the preserved placeholder metadata
- attempted a stricter plurality-enforcement follow-up during the same slice, but real `.sk` verification showed that the currently green compatibility corpus still relies on several production expressions that report `isSingle()` more strictly than their effective parser usage:
  - `brewing stand fuel slot`
  - `brewing results`
  - `loot`
- narrowed the landed slice back to metadata preservation plus flag/time behavior only, so the live parser keeps the current green runtime surface while still closing the missing `*` / `~` / `@time` behavior gap
- closed the next upstream `patterns` API gap around pattern-element graph introspection
- added the missing `PatternElement` hierarchy locally:
  - `PatternElement`
  - `LiteralPatternElement`
  - `OptionalPatternElement`
  - `GroupPatternElement`
  - `ChoicePatternElement`
  - `ParseTagPatternElement`
  - `TypePatternElement`
  - `RegexPatternElement`
- `PatternCompiler` now builds a lightweight pattern-element graph alongside the existing shared regex matcher without changing the currently green direct/parser match path
- `SkriptPattern` now exposes the upstream-style structural APIs that were still missing locally:
  - `countTypes()`
  - `countNonNullTypes()`
  - `getElements(Class<T>)`
- added regressions proving:
  - alternation-heavy patterns report total placeholder count separately from the maximum non-null branch count
  - optional/choice/type elements are discoverable through the new graph traversal APIs with stable expression indexes
- did not mark parity complete: the full upstream element-graph matcher/runtime is still richer than this slice, and current live parsing still runs through the existing shared regex matcher
- did not rerun GameTest because this batch adds parser-structure introspection only; the existing matcher behavior remained under parser-focused unit coverage
- moved the shared syntax-pattern matcher out of `SkriptParser` and into `ch/njol/skript/patterns`, so parser flow and direct pattern compilation now use the same compiled matcher path
- `SkriptParser.ParseResult` now carries `mark`, and parser init paths now receive general parse tags from matched branches instead of only the prior hardcoded leading `implicit:` case
- `PatternCompiler` / `SkriptPattern` now support compiled literals, placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦` on the current compatibility surface
- added parser-facing regression coverage for parse marks, branch-specific parse tags, and direct pattern-compiler matching while preserving the already-green inline optional whitespace natural form
- closed the next parser-owned tag parity gap in `PatternCompiler`: bare leading `:` tags now auto-derive from the following literal token on the current compatibility surface, including literal-leading forms such as `:future` / `:non(-| )` and choice-leading forms such as `:(min|max)[imum]`
- kept empty auto-tags flowing through the same metadata capture path as explicit tags, so derived numeric-looking tags still contribute parse marks while literal-derived tags are normalized to the lowercase branch text that upstream syntax expects
- added direct matcher coverage for literal-derived empty tags, choice-derived empty tags with omitted optional branches, and uppercase literal branches normalizing to lowercase tags
- added a parser-registry regression proving those auto-derived tags reach `SkriptParser.ParseResult` during real syntax-element initialization
- did not mark parity complete: full upstream `patterns` element-graph parity is still open beyond the shared matcher, and this slice only closes the current empty auto-tag derivation gap
- live `.sk` parsing changed through the shared matcher path, so real Fabric GameTest verification was rerun against the existing `.sk` corpus; no new dedicated `.sk` fixture was added because the newly closed empty auto-tag forms are not yet used by the currently shipping local runtime syntax
- closed the next shared-matcher parity gap around optional and alternation-scoped raw regex captures
- `SkriptPattern.match(...)` now skips unmatched raw-regex capture groups when an optional branch is omitted or another alternation branch wins, instead of failing the whole match
- added direct matcher regressions for:
  - omitted optional raw regex captures staying green with an empty `regexes` list
  - alternation branches without regex captures no longer inheriting a stale null-regex failure from another branch
- added a parser-registry regression proving a registered section pattern with `[<.+>]` initializes correctly both when the regex capture is omitted and when it is present
- did not mark parity complete: broader upstream `patterns` element-graph parity is still open beyond this raw-regex omission/alternation closure

## Files Changed

- `src/main/java/ch/njol/skript/lang/SkriptParser.java`
- `src/main/java/ch/njol/skript/patterns/MatchResult.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Verification

- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after restoring ordered duplicate-preserving parse-tag accumulation
- `./gradlew runGameTest --rerun-tasks`
  - passed after the ordered parse-tag slice
  - `198 / 198` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after landing explicit parser default-value backfill for omitted placeholders
- `./gradlew runGameTest --rerun-tasks`
  - first rerun failed because the current GameTest helper reflects the private matcher bridge and the refactor had removed `SkriptParser.match(String, String, ParseContext, int)`
  - failure surfaced as `NoSuchMethodException: ch.njol.skript.lang.SkriptParser.match(java.lang.String,java.lang.String,ch.njol.skript.lang.ParseContext,int)`
  - reran after restoring that private bridge; command passed
  - `198 / 198` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after landing placeholder flag/time metadata handling
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks`
  - passed after narrowing the slice away from plurality enforcement
- `./gradlew runGameTest --rerun-tasks`
  - first rerun failed after an overly strict plurality attempt by crashing `skript_fabric_base_game_test_executes_real_skript_file_using_list_variable_reindexing_on_set`
  - second rerun failed after the first rollback with `3` required tests red:
    - `skript_fabric_expression_game_test_brewing_fuel_slot_expression_executes_real_script`
    - `skript_fabric_event_game_test_brewing_complete_event_executes_real_script`
    - `skript_fabric_event_game_test_loot_generate_event_executes_real_script`
  - final rerun passed after keeping plurality metadata exposed but non-enforcing in the shared matcher
  - `197 / 197` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed on the final tree

- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - first run failed in `:compileJava` with stale `PatternMatch` references at `src/main/java/ch/njol/skript/lang/SkriptParser.java:307` and `:360`
  - reran after fixing those references; command passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - prior shared-matcher verification passed with `195 / 195` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after adding empty auto-tag derivation coverage
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after the empty auto-tag slice
- `./gradlew runGameTest --rerun-tasks`
  - passed after the empty auto-tag slice
  - `196 / 196` required GameTests completed successfully
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks`
  - passed after closing optional/alternation raw-regex capture parity
- `./gradlew test --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks`
  - first run failed on the new graph traversal assertion because wrapper elements were being unwrapped before type collection
  - reran after fixing wrapper-element collection in `SkriptPattern.getElements(...)`; command passed

## Exact Syntax Exercised

- pattern text: `repeat:alpha unique:beta repeat:gamma`
- pattern text: `default number [%number%]`
- omitted form: `default number`
- explicit form: `default number 5`

## Unresolved Risks

- ordered duplicate-preserving tags now match upstream more closely, but broader parser tag/mark parity is still incomplete outside this slice; the local compatibility layer still lacks the full upstream matcher/runtime behavior around the richer pattern element graph
- placeholder plurality metadata is now preserved and exposed through `TypePatternElement`, but it is not enforced during live matching on the current compatibility surface; the real `.sk` corpus still depends on several production expressions that report `isSingle()` more strictly than their effective parser use
- omitted-placeholder fallback is now closed only for explicit parser-scoped `DefaultValueData` values; full upstream classinfo-backed `defaultExpression(...)` parity is still open because the local `ClassInfo` surface is thinner than upstream and sits outside this lane’s owned files
- full upstream failure semantics for omitted non-optional placeholders without any valid default remain intentionally deferred so the current live syntax corpus does not regress on patterns that still expect `null` when optional groups are omitted

## Merge Notes

- current-cycle highest conflict risk is `src/main/java/ch/njol/skript/lang/SkriptParser.java`; any concurrent parser-flow work will need manual reconciliation around the ordered parse-tag list restore, the omitted-placeholder default-value backfill, and the restored private matcher bridge
- `src/main/java/ch/njol/skript/patterns/MatchResult.java` and `src/main/java/ch/njol/skript/patterns/SkriptPattern.java` now carry the ordered-tag behavior and may conflict with any concurrent matcher/parity slice
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` may conflict with any concurrent parser-regression additions
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java` remains a likely conflict point for any concurrent parser-pattern compatibility additions
- current-cycle conflict surface is now `src/main/java/ch/njol/skript/lang/SkriptParser.java`, `src/main/java/ch/njol/skript/patterns/MatchResult.java`, `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`, `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`, and `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
