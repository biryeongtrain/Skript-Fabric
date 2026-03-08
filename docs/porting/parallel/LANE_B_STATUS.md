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

- Continue `Part 1A` on richer parser tag / mark / pattern parity after the placeholder flag/time metadata slice, without claiming parser-pattern parity complete.

## Work Log

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

- `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`
- `src/main/java/ch/njol/skript/patterns/TypePatternElement.java`
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Verification

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

## Unresolved Risks

- placeholder plurality metadata is now preserved and exposed through `TypePatternElement`, but it is not enforced during live matching on the current compatibility surface; the real `.sk` corpus still depends on several production expressions that report `isSingle()` more strictly than their effective parser use
- omitted-placeholder default-expression/default-value fallback is still open in `SkriptParser` parity; closing it likely needs coordinator guidance because the local `ClassInfo` surface is still much thinner than upstream and sits outside this lane’s owned files

## Merge Notes

- current-cycle highest conflict risk is `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`; any concurrent pattern-parity work will need manual reconciliation around the new placeholder metadata parsing
- `src/main/java/ch/njol/skript/patterns/SkriptPattern.java` is the next conflict risk because the final matcher behavior now applies placeholder-local parse flags and `@time` state but intentionally leaves plurality metadata non-enforcing
- `src/main/java/ch/njol/skript/patterns/TypePatternElement.java` is part of the active conflict surface because it now carries placeholder metadata that earlier local code did not preserve
- `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java` may conflict with any concurrent parser-regression additions
- `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java` remains a likely conflict point for any concurrent parser-pattern compatibility additions
- current-cycle conflict surface is now `src/main/java/ch/njol/skript/patterns/PatternCompiler.java`, `src/main/java/ch/njol/skript/patterns/SkriptPattern.java`, `src/main/java/ch/njol/skript/patterns/TypePatternElement.java`, `src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java`, and `src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java`
