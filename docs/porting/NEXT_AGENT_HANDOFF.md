# Next Agent Handoff

Last updated: 2026-03-09

## Read Order

1. [README.md](README.md)
2. this file
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) if this is a multi-session Codex run
8. [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) if this is a multi-session Codex run

## Parallel Session Docs

For the next Codex app parallel session, use:

- [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md)
- [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md)
- lane-local notes under [parallel/README.md](parallel/README.md)

## Current State

- Source-level condition port: `28 / 28` complete
- Source-level expression port: `84 / 84` complete
- Source-level effect port: `24 / 24` complete
- Stage 5 event backend closure: `22 / 22`
- Stage 8 parity audit: `in_progress`
- Package-local Stage 8 audit progress: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local Stage 8 scope: `191 / 214`
- Top-level non-package Bukkit helpers still outside that matrix: `4`
- Cross-cutting Stage 8 base gap still open: ambiguous bare item-id generic compare, for example `event-item is wheat`
- Latest verified runtime baseline from 2026-03-09:
  - `./gradlew runGameTest --rerun-tasks` passed
  - `./gradlew build --rerun-tasks` passed
  - `230 / 230` scheduled Fabric GameTests completed without build failure
- Current upstream `ch/njol/skript` inventory snapshot:
  - upstream snapshot `e6ec744`: `1189` Java files
  - local tree: `140` Java files
  - remaining shortfall: `1049` Java files

## Priority Shift

Do not continue the next Stage 8 package-local Bukkit slice yet.
The user explicitly reprioritized upstream `ch/njol/skript` implementation first.

New immediate priority:

- keep the current Stage 8 status frozen and accurately recorded
- close the broader `ch/njol/skript` surface first, especially the next blocking `Part 1A` / `Part 1B` parser, loader, variable, and registry gaps
- import missing upstream syntax families into the active Fabric runtime using exact existing Skript forms once those blocking closure slices are green
- after that upstream closure track, resume the broader Bukkit-behavior parity push until verified behavior matches upstream as closely as practical

## Latest Closure Slice

- merged the next focused `lang` / registry closure batch:
  - [src/main/java/ch/njol/skript/lang/SkriptParser.java](../../src/main/java/ch/njol/skript/lang/SkriptParser.java) now runs legacy `parseStatic(...)` matches with `ALL_FLAGS`, so expression-only placeholders such as `%~integer%` work through legacy `SyntaxElementInfo` parsing again while placeholder-level masks still restrict literal versus expression acceptance
  - [src/main/java/ch/njol/skript/registrations/Classes.java](../../src/main/java/ch/njol/skript/registrations/Classes.java) now limits `getPatternInfos(...)` to explicitly registered literal patterns, which restores upstream unparsed-literal candidate discovery instead of parser-backed fallback matches
  - a fresh lane audit reran the current `Statement` / `ScriptLoader` / `Section` corpus and found no remaining mergeable mismatch in the green corpus, so that lane stayed docs-only for this cycle
- parser/runtime verification landed:
  - [src/test/java/ch/njol/skript/lang/parser/SkriptParserStaticFlagsCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/parser/SkriptParserStaticFlagsCompatibilityTest.java)
  - [src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java)
  - [src/test/java/ch/njol/skript/lang/UnparsedLiteralCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/UnparsedLiteralCompatibilityTest.java)
  - [src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java](../../src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java)
  - [src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java](../../src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java)
  - [src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java](../../src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java)
- latest verification for this merged slice:
  - `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.parser.SkriptParserStaticFlagsCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
  - `./gradlew build --rerun-tasks` passed, and the build path again executed the full Fabric GameTest suite with `230 / 230`
- next likely `lang` follow-ups:
  - broader parser default-value and placeholder-omission parity beyond the current fail-fast path plus restored legacy static flags
  - broader classinfo/parser registry parity beyond explicit literal-pattern lookup and current converter/default-expression closure
  - broader statement/loading orchestration only once a new concrete mismatch is reproduced, because the latest audit still did not find another mergeable `Statement` / `ScriptLoader` / `Section` gap in the current green corpus

## What Landed In This Slice

- moved the canonical porting docs under `docs/porting`
- kept root-level doc filenames as entrypoint pointers so existing prompts do not break
- recorded the current verified Stage 8 baseline in the canonical status and handoff docs
- added a dedicated persistent audit document for the new workstream:
  - [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- measured the upstream/local baseline for `ch/njol/skript`:
  - upstream snapshot `e6ec744`: `1189` Java files
  - local tree: `140` Java files
  - net missing relative surface: `1049` Java files
- started the first `Part 2` user-visible syntax-import slice on top of the current core closure:
  - `CondIsAlive`, `CondIsSilent`, and `CondIsInvulnerable`
  - `EffKill`, `EffSilence`, and `EffInvulnerability`
  - bootstrap registration now covers exact forms such as `%entities% are alive/dead`, `%entities% are silent`, `%entities% are invulnerable/invincible`, `kill %entities%`, `silence %entities%`, `unsilence %entities%`, and `make %entities% silent`
  - dedicated parser/bootstrap unit tests plus real `.sk` GameTests now verify those forms end-to-end
  - follow-up syntax-import closure now also covers `%material%`, `feed [the] %players% [by %-number% [beef[s]]]`, invisible/visible condition plus effect forms, `%entities% (is|are) (burning|ignited|on fire)`, `%livingentities% (has|have) (ai|artificial intelligence)`, `%players% (is|are) sprinting`, and exact sprinting start/stop effect forms
- kept the first concrete closure target as `Part 1A: lang parser/runtime closure`
- started `Part 1B` in parallel because `Classes`, `Variables`, `config`, and `structures` behavior is already being tightened in the same dependency cluster
- landed the current `Part 1A` / `Part 1B` slices:
  - `VariableString` now routes `StringMode.MESSAGE` through Patbox `TextPlaceholderAPI`, and `TriggerItem.walk(...)` now exposes the current event through `CurrentSkriptEvent` so live `%namespace:path%` placeholders resolve on active message/name paths
  - locked runtime GameTests now clear Skript variables before and after each body, preventing suite-order leakage while leaving production variable semantics unchanged
  - the missing legacy log-handler stack is now restored through `LogHandler`, `BlockingLogHandler`, `FilteringLogHandler`, `CountingLogHandler`, and `RetainingLogHandler`, with `ParseLogHandler` and `SkriptLogger` wired back through the same compatibility surface
  - `patterns.Keyword` now exists locally and `SkriptPattern` again applies the upstream-style keyword prefilter before the heavier matcher path
  - `variables.TypeHints` now restores the legacy add/get/enter-scope/exit-scope/clear bridge on top of the active hint manager
  - `classes.Parser`, `PatternedParser`, `Converter`, and `registrations.Converters` now restore the missing wrapper and adapter surface expected by older parser and converter paths
  - `Variable.isValidVariableName(...)` now ignores `*` inside paired `%...%` spans, restoring dynamic forms such as `result::%{source::*}%`
  - `ClassInfo` now exposes default expressions, and `SkriptParser` now falls back to them when omitted non-optional placeholders have no parser-scoped default
  - `Classes` now also exposes upstream helper overloads for default-expression lookup by codename or exact class
  - `EffChange.init(...)` now publishes parse-time local-variable hints for the exact built-in `set %object% to %object%` path when it targets a hintable local variable
  - `Variables.getVariable("name::*", ...)` now reconstructs upstream-style nested list maps, including `null` parent sentinels when a direct parent value and descendants coexist, while `getVariablesWithPrefix(...)` keeps the current shallow direct-child view
  - `SkriptParser` now recognizes upstream-prefixed variable forms such as `var {x}`, `variable {x}`, and `the variable {x}`
  - `Statement.selectRetainedFailure(...)` now keeps earlier higher-quality effect/condition parse errors over later lower-quality plain-statement failures on the same syntax line
  - `Classes.parse(...)` now clears stale direct-parser failures before later parser or converter fallback success
  - `SkriptParser.ParseResult.tags` and the shared matcher now preserve duplicate parse tags in encounter order
  - shared pattern-element graph nodes now preserve grouped string/combinations parity through `toFullString()`, `getCombinations(...)`, and `getAllCombinations()`, and malformed grouped patterns now wrap through local `MalformedPatternException`
  - `ScriptLoader.parseSectionTriggerItem(...)` now keeps temporary hint scopes alive when a section line falls back from `Section.parse(...)` to a plain statement and still succeeds
  - `Statement.parse(...)` now keeps same-pattern effect/condition init failures non-terminal until the plain registered-statement path has been tried, while restoring the best prior specific error if no statement matches
  - real base `.sk` coverage now includes `statement_fallback_after_failed_effect_set_test_block.sk`, `statement_fallback_section_hint_test_block.sk`, `patbox_placeholder_entity_name_test_block.sk`, `prefixed_variable_set_test_block.sk`, `higher_quality_parse_error_prefers_effect_test_block.sk`, `variable_name_expression_inner_list_marker_set_test_block.sk`, `built_in_set_local_hint_test_block.sk`, `alive_entity_names_entity.sk`, `silent_entity_names_entity.sk`, `invulnerable_entity_names_entity.sk`, `kill_entity_marks_block.sk`, `make_silent_names_entity.sk`, `make_invulnerable_names_entity.sk`, `material_alias_placeholder_set_test_block.sk`, `feed_event_player_by_beefs_marks_block.sk`, `make_invisible_names_entity.sk`, `make_visible_names_entity.sk`, `burning_entity_names_entity.sk`, `ignited_entity_names_entity.sk`, `on_fire_entity_names_entity.sk`, `invisible_entity_names_entity.sk`, `visible_entity_names_entity.sk`, `has_ai_entity_names_entity.sk`, `no_ai_entity_names_entity.sk`, `sprinting_player_names_player.sk`, `make_player_start_sprinting_names_player.sk`, and `make_player_stop_sprinting_names_player.sk`, increasing the current Fabric GameTest suite to `230 / 230`
  - `ExprInput` now acts as a working compatibility expression instead of a pure stub
  - `SkriptParser` now resolves `input`, typed `%classinfo% input`, and `input index` when `InputSource` context is active
  - `Classes` now normalizes spaced, hyphenated, and plural user type names for parser-facing class-info lookup
  - `options:` entries are now represented by `EntryNode`
  - `StructOptions` is now present locally and `ScriptLoader.replaceOptions(...)` performs real option substitution
  - `Node.remove()`, `NodeMap`, and `SectionNode` now close the missing case-insensitive lookup, ordered replace/remove, parent-reassignment, and entry-conversion map-sync behavior used by config/structure compatibility paths
  - `Skript.registerStructure(...)` now accepts `EntryValidator` through the compatibility surface instead of forcing direct modern-registry registration
  - `KeyValueEntryData` now accepts both raw `SimpleNode` entries and runtime-shaped `EntryNode` entries
  - `StructOptions` now uses a recursive validator-backed load path, so runtime `EntryNode` trees and raw simple `key: value` nodes both load through the same compatibility surface while preserving `Invalid line in options` diagnostics
  - `ScriptLoader.loadItems(...)` now attempts registered `Section` parsing for section nodes before falling back to statement parsing, so pure section syntax can load and execute children through the normal trigger-item path
  - `ClassInfo` / `Classes` now close the missing codename, literal-pattern, and supertype-lookup behavior used by parser/runtime compatibility paths
  - `SkriptParser` now recognizes `{...}` variable expressions directly
  - `Variables` now defaults to case-insensitive storage/lookup, with tests for both case-insensitive and case-sensitive operation
  - `Variables.withLocalVariables(...)` now matches upstream copy-back semantics, so nested section-event local-variable mutations are written back to the provider scope instead of restoring the old target snapshot
  - `Variable` now matches upstream list-to-list `set` semantics by not recommending keyed preservation for list variables, so keyed sources are reindexed into numeric target slots
  - `EffChange` now only applies keyed `SET`/`ADD`/`REMOVE` deltas when the source expression explicitly recommends key preservation, and treats empty `SET` payloads as deletions when supported
  - quoted string literals now stay string literals in generic `%object%` contexts instead of being consumed by registry-backed parsers during live `.sk` loading
  - `SecIf` now executes chained `if / else if / else` sections and preserves the post-chain continuation path
  - `SkriptParser` now supports minimal raw regex captures in registered syntax patterns like `if <.+>`
  - `SkriptParser` now preserves required whitespace around omitted inline optional groups and inline alternation branches for the currently verified natural-script surface
  - `SecIf` is now a registered `Section` with a real `init(...)` path, and the runtime bootstrap registers it through the normal syntax registry
  - `Statement.parse(...)` no longer carries a dedicated `SecIf` special-case fallback
  - `SecIf` now supports `parse if` and `else parse if` through the registered section path
  - `SecIf` now supports multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run`
  - `SecIf` now supports implicit conditional sections such as `%condition%:`
  - `SkriptParser` now forwards the minimal leading `implicit:` tag needed by those registered conditional section patterns
  - parse-time false branches now skip body loading, so invalid skipped bodies no longer break live `.sk` loading
  - `Condition.parse(...)` now unwraps grouped outer parentheses, so `if ((...)):` and `else if (((...))):` conditions now follow upstream grouping behavior
  - `CondCompare` now keeps generic-object variable comparisons available to `if` conditions without stealing entity-specific condition syntax
  - `Effect.parse(...)` now opens `SectionContext` for plain effects with section-managing expressions, and `Statement.parse(...)` now routes effect parsing through it so lines like `set {_component} to a blank equippable component:` actually own and execute their section body
  - section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed local values instead of assuming typed runtime arrays
  - real expression `.sk` GameTests now also cover section-local propagation for custom damage source, potion effect, and loot context creation sections
  - `Skript.error(...)`, `Skript.warning(...)`, and `Skript.debug(...)` now flow through a parse-log-aware `SkriptLogger`
  - `ParseLogHandler` now retains specific parse errors across nested parser scopes instead of acting as an empty shim
  - `Statement.parse(...)` now stops on captured function/effect/condition parse errors and prints the retained diagnostic instead of falling through to a generic section fallback
  - `ScriptLoaderCompatibilityTest` now proves that a valid effect used as a section keeps its specific ownership error without also logging `Can't understand this section`
  - `PatternCompiler` / `SkriptPattern` now back a shared matcher used by both direct pattern compilation and `SkriptParser`, including placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦`
  - `PatternCompiler` now also builds a lightweight `PatternElement` graph, and `SkriptPattern` now exposes `countTypes()`, `countNonNullTypes()`, and `getElements(...)` for the current upstream-introspection compatibility surface
  - `SkriptParser.ParseResult` now carries `mark`, and parser init paths now receive general parse tags from matched branches instead of only the earlier hardcoded leading `implicit:` case
  - bare leading `:` metadata now auto-derives from the following literal or choice branch on the current compatibility surface
  - `ScriptLoader.loadItems(...)` now restores the more specific retained section-versus-statement diagnostic when both parse paths fail on a section node
  - `Statement.parse(...)` now rejects plain conditions used as section headers instead of silently returning a body-less condition item
  - `Classes` now sorts class infos by assignable-type specificity plus `before(...)` / `after(...)` dependencies instead of raw registration order
  - shared literal-pattern matches now also follow that stable class-info ordering when multiple class infos register the same alias
  - `ScriptLoader.loadItems(...)` now emits unreachable-code warnings behind `ScriptWarning.UNREACHABLE_CODE` suppression when a previously loaded statement stops further execution
  - nested section-contained stop-trigger and stop-section intents now propagate through `TriggerItem.walk(...)`, and registered sections now surface stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
  - `ParserInstance` now owns a per-script `HintManager`, and `Variable.newInstance(...)` now consumes local variable type hints so generic `%object%` locals can narrow to a known hinted type
  - `ScriptLoader.loadItems(...)` and `parseSectionTriggerItem(...)` now manage section and temporary non-section hint scopes, so failed section parses roll back temporary hints while successful section loads can propagate, freeze, or merge hints through the current stop-flow compatibility surface
  - `Variables` now uses natural variable-name ordering for prefix/list iteration, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set` reindexing
  - real base `.sk` GameTests now also cover numeric list ordering through [src/gametest/resources/skript/gametest/base/list_variable_numeric_order_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/list_variable_numeric_order_set_test_block.sk)
  - real base `.sk` GameTests now also cover loader unreachable-code warnings through [src/gametest/resources/skript/gametest/base/unreachable_code_warning_stop_test_block.sk](../../src/gametest/resources/skript/gametest/base/unreachable_code_warning_stop_test_block.sk)
  - parser-compatible natural forms that were red during this closure are green again under real `.sk` loading, including `%objects% can be equipped on[to] entities`, `%objects% will lose durability when injured`, and `make %entities% not breedable`
  - real base `.sk` GameTests now cover options replacement, mixed-case variable set/lookup, direct conditional chain execution, parenthesized conditional chain execution, `parse if` / `else parse if`, multiline `if any` / `if all` plus `then`, implicit conditional sections, skipped-invalid-body parse-if paths, and list-variable reindexing on `set {target::*} to {source::*}`
  - real expression `.sk` GameTests now cover plain-effect section ownership and section-local variable copy-back through `set {_component} to a blank equippable component:`
- upstream cross-check corrected two false gap candidates so they do not get reopened:
  - `TriggerSection.run(...)` matches upstream by throwing `UnsupportedOperationException`
  - function-call wrapper `init(...)` methods matching upstream are not current blockers
- reran verification after the code slices:
  - targeted unit slices passed
  - `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.lang.VariableStringCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing section-level execution-intent propagation
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed after closing explicit literal-pattern ordering parity
  - `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging the latest parse-log-aware class parsing, ordered duplicate parser tags, and statement fallback slice
  - `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging classinfo-backed omitted defaults, inner-expression variable-name list-marker validation, and built-in `EffChange` local hints
  - `./gradlew test --tests org.skriptlang.skript.fabric.runtime.SilentSyntaxTest --tests org.skriptlang.skript.fabric.runtime.InvulnerableSyntaxTest --tests org.skriptlang.skript.fabric.runtime.AliveKillSyntaxTest --rerun-tasks` passed after importing the first missing base entity-state/control syntax families
  - `./gradlew runGameTest --rerun-tasks` passed with `216 / 216`
  - `./gradlew build --rerun-tasks` passed

## Files Changed In This Slice

- [README.md](README.md)
- [PORTING_STATUS.md](PORTING_STATUS.md)
- [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md)
- [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
- [src/main/java/ch/njol/skript/ScriptLoader.java](../../src/main/java/ch/njol/skript/ScriptLoader.java)
- [src/main/java/ch/njol/skript/Skript.java](../../src/main/java/ch/njol/skript/Skript.java)
- [src/main/java/ch/njol/skript/classes/ClassInfo.java](../../src/main/java/ch/njol/skript/classes/ClassInfo.java)
- [src/main/java/ch/njol/skript/lang/Condition.java](../../src/main/java/ch/njol/skript/lang/Condition.java)
- [src/main/java/ch/njol/skript/lang/Effect.java](../../src/main/java/ch/njol/skript/lang/Effect.java)
- [src/main/java/ch/njol/skript/lang/Statement.java](../../src/main/java/ch/njol/skript/lang/Statement.java)
- [src/main/java/ch/njol/skript/log/ErrorQuality.java](../../src/main/java/ch/njol/skript/log/ErrorQuality.java)
- [src/main/java/ch/njol/skript/log/LogEntry.java](../../src/main/java/ch/njol/skript/log/LogEntry.java)
- [src/main/java/ch/njol/skript/log/ParseLogHandler.java](../../src/main/java/ch/njol/skript/log/ParseLogHandler.java)
- [src/main/java/ch/njol/skript/log/SkriptLogger.java](../../src/main/java/ch/njol/skript/log/SkriptLogger.java)
- [src/main/java/ch/njol/skript/config/EntryNode.java](../../src/main/java/ch/njol/skript/config/EntryNode.java)
- [src/main/java/ch/njol/skript/config/NodeMap.java](../../src/main/java/ch/njol/skript/config/NodeMap.java)
- [src/main/java/ch/njol/skript/config/SectionNode.java](../../src/main/java/ch/njol/skript/config/SectionNode.java)
- [src/main/java/ch/njol/skript/expressions/ExprInput.java](../../src/main/java/ch/njol/skript/expressions/ExprInput.java)
- [src/main/java/ch/njol/skript/lang/EffectSectionEffect.java](../../src/main/java/ch/njol/skript/lang/EffectSectionEffect.java)
- [src/main/java/ch/njol/skript/lang/SkriptParser.java](../../src/main/java/ch/njol/skript/lang/SkriptParser.java)
- [src/main/java/ch/njol/skript/lang/Variable.java](../../src/main/java/ch/njol/skript/lang/Variable.java)
- [src/main/java/ch/njol/skript/lang/parser/ParserInstance.java](../../src/main/java/ch/njol/skript/lang/parser/ParserInstance.java)
- [src/main/java/ch/njol/skript/patterns/ChoicePatternElement.java](../../src/main/java/ch/njol/skript/patterns/ChoicePatternElement.java)
- [src/main/java/ch/njol/skript/patterns/GroupPatternElement.java](../../src/main/java/ch/njol/skript/patterns/GroupPatternElement.java)
- [src/main/java/ch/njol/skript/patterns/LiteralPatternElement.java](../../src/main/java/ch/njol/skript/patterns/LiteralPatternElement.java)
- [src/main/java/ch/njol/skript/patterns/OptionalPatternElement.java](../../src/main/java/ch/njol/skript/patterns/OptionalPatternElement.java)
- [src/main/java/ch/njol/skript/patterns/ParseTagPatternElement.java](../../src/main/java/ch/njol/skript/patterns/ParseTagPatternElement.java)
- [src/main/java/ch/njol/skript/patterns/PatternCompiler.java](../../src/main/java/ch/njol/skript/patterns/PatternCompiler.java)
- [src/main/java/ch/njol/skript/patterns/PatternElement.java](../../src/main/java/ch/njol/skript/patterns/PatternElement.java)
- [src/main/java/ch/njol/skript/patterns/RegexPatternElement.java](../../src/main/java/ch/njol/skript/patterns/RegexPatternElement.java)
- [src/main/java/ch/njol/skript/patterns/SkriptPattern.java](../../src/main/java/ch/njol/skript/patterns/SkriptPattern.java)
- [src/main/java/ch/njol/skript/patterns/TypePatternElement.java](../../src/main/java/ch/njol/skript/patterns/TypePatternElement.java)
- [src/main/java/ch/njol/skript/registrations/Classes.java](../../src/main/java/ch/njol/skript/registrations/Classes.java)
- [src/main/java/ch/njol/skript/sections/SecIf.java](../../src/main/java/ch/njol/skript/sections/SecIf.java)
- [src/main/java/ch/njol/skript/structures/StructOptions.java](../../src/main/java/ch/njol/skript/structures/StructOptions.java)
- [src/main/java/ch/njol/skript/variables/HintManager.java](../../src/main/java/ch/njol/skript/variables/HintManager.java)
- [src/main/java/ch/njol/skript/variables/Variables.java](../../src/main/java/ch/njol/skript/variables/Variables.java)
- [src/main/java/org/skriptlang/skript/lang/entry/KeyValueEntryData.java](../../src/main/java/org/skriptlang/skript/lang/entry/KeyValueEntryData.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsAlive.java](../../src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsAlive.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsInvulnerable.java](../../src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsInvulnerable.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsSilent.java](../../src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsSilent.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondCompare.java](../../src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondCompare.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffInvulnerability.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffInvulnerability.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffKill.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffKill.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffSilence.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffSilence.java)
- [src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextEntity.java](../../src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextEntity.java)
- [src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLocation.java](../../src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLocation.java)
- [src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLooter.java](../../src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLooter.java)
- [src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLuck.java](../../src/main/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLuck.java)
- [src/main/java/org/skriptlang/skript/bukkit/potion/elements/effects/EffApplyPotionEffect.java](../../src/main/java/org/skriptlang/skript/bukkit/potion/elements/effects/EffApplyPotionEffect.java)
- [src/main/java/org/skriptlang/skript/bukkit/potion/elements/effects/EffPoison.java](../../src/main/java/org/skriptlang/skript/bukkit/potion/elements/effects/EffPoison.java)
- [src/main/java/org/skriptlang/skript/bukkit/potion/elements/expressions/ExprPotionEffect.java](../../src/main/java/org/skriptlang/skript/bukkit/potion/elements/expressions/ExprPotionEffect.java)
- [src/main/java/org/skriptlang/skript/bukkit/potion/elements/expressions/ExprPotionEffects.java](../../src/main/java/org/skriptlang/skript/bukkit/potion/elements/expressions/ExprPotionEffects.java)
- [src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java](../../src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java)
- [src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java](../../src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java)
- [src/test/java/ch/njol/skript/config/SectionNodeCompatibilityTest.java](../../src/test/java/ch/njol/skript/config/SectionNodeCompatibilityTest.java)
- [src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/InputSourceCompatibilityTest.java)
- [src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java](../../src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java)
- [src/test/java/ch/njol/skript/lang/UnparsedLiteralCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/UnparsedLiteralCompatibilityTest.java)
- [src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/VariableCompatibilityTest.java)
- [src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java](../../src/test/java/ch/njol/skript/patterns/PatternCompilerCompatibilityTest.java)
- [src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java)
- [src/test/java/ch/njol/skript/sections/SecIfCompatibilityTest.java](../../src/test/java/ch/njol/skript/sections/SecIfCompatibilityTest.java)
- [src/test/java/ch/njol/skript/structures/StructureEntryValidatorCompatibilityTest.java](../../src/test/java/ch/njol/skript/structures/StructureEntryValidatorCompatibilityTest.java)
- [src/test/java/org/skriptlang/skript/fabric/runtime/AliveKillSyntaxTest.java](../../src/test/java/org/skriptlang/skript/fabric/runtime/AliveKillSyntaxTest.java)
- [src/test/java/org/skriptlang/skript/fabric/runtime/InvulnerableSyntaxTest.java](../../src/test/java/org/skriptlang/skript/fabric/runtime/InvulnerableSyntaxTest.java)
- [src/test/java/org/skriptlang/skript/fabric/runtime/SilentSyntaxTest.java](../../src/test/java/org/skriptlang/skript/fabric/runtime/SilentSyntaxTest.java)
- [src/test/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLocationCompatibilityTest.java](../../src/test/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprLootContextLocationCompatibilityTest.java)
- [src/test/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprSecCreateLootContextCompatibilityTest.java](../../src/test/java/org/skriptlang/skript/bukkit/loottables/elements/expressions/ExprSecCreateLootContextCompatibilityTest.java)
- [src/test/java/org/skriptlang/skript/bukkit/potion/elements/PotionEntityObjectCompatibilityTest.java](../../src/test/java/org/skriptlang/skript/bukkit/potion/elements/PotionEntityObjectCompatibilityTest.java)
- [src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java](../../src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java)
- [src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricExpressionGameTest.java](../../src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricExpressionGameTest.java)
- [src/gametest/resources/skript/gametest/base/conditional_chain_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/conditional_chain_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/list_variable_reindex_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/list_variable_reindex_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/multiline_conditional_then_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/multiline_conditional_then_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/implicit_conditional_chain_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/implicit_conditional_chain_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/parenthesized_conditional_chain_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/parenthesized_conditional_chain_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/parse_if_conditional_chain_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/parse_if_conditional_chain_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/parse_if_skips_invalid_body_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/parse_if_skips_invalid_body_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/options_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/options_set_test_block.sk)
- [src/gametest/resources/skript/gametest/base/mixed_case_variable_set_test_block.sk](../../src/gametest/resources/skript/gametest/base/mixed_case_variable_set_test_block.sk)
- [src/gametest/resources/skript/gametest/expression/blank_equippable_component_section_propagates_locals_names_entity.sk](../../src/gametest/resources/skript/gametest/expression/blank_equippable_component_section_propagates_locals_names_entity.sk)
- [src/gametest/resources/skript/gametest/expression/custom_damage_source_section_propagates_locals_names_entity.sk](../../src/gametest/resources/skript/gametest/expression/custom_damage_source_section_propagates_locals_names_entity.sk)
- [src/gametest/resources/skript/gametest/expression/loot_context_section_propagates_locals_names_entity.sk](../../src/gametest/resources/skript/gametest/expression/loot_context_section_propagates_locals_names_entity.sk)
- [src/gametest/resources/skript/gametest/expression/potion_effect_section_propagates_locals_names_entity.sk](../../src/gametest/resources/skript/gametest/expression/potion_effect_section_propagates_locals_names_entity.sk)
- repo-root entrypoint pointers:
  - `PORTING_STATUS.md`
  - `NEXT_AGENT_HANDOFF.md`
  - `FABRIC_PORT_STAGES.md`
  - `FABRIC_EVENT_MAPPING.md`
  - `IMPLEMENTED_SYNTAX.md`

## User Preferences You Must Preserve

- Do not expose `fabric` in end-user Skript syntax unless absolutely necessary.
- For item/block ids:
  - accept bare ids like `diamond_block`
  - default missing namespace to `minecraft:`
  - keep explicit namespaces unchanged
- For status-effect ids:
  - keep registry-backed parsing
  - accept bare ids like `poison`
  - default missing namespace to `minecraft:`
  - keep explicit namespaces unchanged
- Prefer registry lookup over hardcoded tables when the Fabric side can be modded and dynamically extended.
- Do not claim parity that is not actually verified.
- Validate user-visible behavior through real `.sk` plus Fabric GameTest, not compile-only work.
- The worktree may already be dirty. Do not revert changes you did not make.

## Important Context

- The new priority workstream is tracked in [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md).
- Stage progress is tracked in [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md).
- Event bridge status is tracked in [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md).
- Current registered syntax snapshot is tracked in [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md).
- The current active Fabric event/backend code still lives under:
  - `src/main/java/org/skriptlang/skript/fabric/runtime`
  - `src/main/java/org/skriptlang/skript/fabric/syntax/event`
- The current Fabric GameTest layout still lives under:
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest`
  - `src/gametest/resources/skript/gametest`

## What Is Still Not Done

- Stage 8 remains incomplete even though the current package-local slice is recorded accurately.
- The broader upstream `ch/njol/skript` surface is far from complete locally.
- After that upstream closure track, the repo still has to continue the broader Bukkit-behavior parity push toward 100% verified-equivalent behavior.
- Entire upstream top-level packages are still absent locally, including `effects`, `events`, `entity`, `command`, `aliases`, and others listed in the audit doc.
- `ch/njol/skript/lang` is close in file count but not in behavioral completeness.
- `Part 1A` and `Part 1B` remain active as enabling workstreams, and `Part 2` syntax import is now active too, but broader statement orchestration, remaining input-source usage, broader classinfo-backed default-expression/default-value parity, deeper variable/class runtime semantics, and most missing user-visible syntax families are still pending.
- pure registered section loading is now closed in `ScriptLoader`, loader fallback now restores the better retained section-versus-statement diagnostic, nested section-contained stop-trigger intent now propagates through loader/runtime, local hint scopes now open/freeze/merge through the current stop-flow path, plain conditions no longer masquerade as section headers, and plain statement parsing no longer leaks outer expression-section ownership into nested argument parsing; the remaining gap is broader statement orchestration and richer built-in hint producers.
- do not reopen the just-closed inline optional-whitespace and inline alternation parser gap unless a new failing unit reproducer or real `.sk` path appears; the current verified closure covers `on[to]`, `when injured`, and `not breedable`
- the shared parser matcher now forwards general parse tags and XOR marks on the current compatibility surface, derives the current bare leading `:` auto-tags again, preserves placeholder-local `*` / `~` / `@time` metadata, no longer fails when optional or alternation-scoped raw-regex captures are omitted, and now lets parser-owned `DefaultValueData` backfill omitted non-optional placeholders on exact forms such as `default number [%number%]`; broader upstream pattern element-graph/runtime parity plus fuller classinfo-backed default-expression/default-value parity are still open
- validator-backed recursive `options:` loading for both runtime `EntryNode` trees and manual raw simple-entry trees is now closed, but broader structure/config validation and diagnostics are still pending.
- the specific list-variable `set {target::*} to {source::*}` reindexing path, natural numeric ordering for prefix/list iteration, legacy list-variable loop aliases, all-values list-check semantics, parse-time local variable type hints, converter-backed class parse/parser helper fallback, and shared literal-pattern ordering are now closed; remaining variable and class-registry gaps are deeper runtime semantics beyond these ordering, hint-consumption, and conversion-bridge paths.
- grouped-parenthesis condition parsing is now closed for the current real-script `if` path, but general statement/orchestration parity is still open.
- do not reopen the just-closed comment-aware loader parsing unless a new failing unit reproducer or real `.sk` path appears

## Recommended Next Priority

Continue `Part 1A` and `Part 1B` upstream `ch/njol/skript` closure first, then resume `Part 2` exact missing-syntax imports on top of that cleaner core.

Recommended order:

1. audit the next missing or behavior-thin upstream `ch/njol/skript` slices, starting with `SkriptParser`, `Statement`, `ScriptLoader`, `Classes`, `Variables`, and nearby dependency closures
2. land those core closures with upstream method shapes and semantics first, not local approximations
3. once a core slice is green, pull the next exact upstream syntax family that the slice unblocks
4. when the change affects user-visible script behavior, add real `.sk` plus Fabric GameTest coverage
5. update [PORTING_STATUS.md](PORTING_STATUS.md), this handoff, and [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md) in the same turn
6. after the missing `ch/njol/skript` surface and exact syntax imports are materially landed, resume the broader Stage 8 and behavioral-parity push toward 100% verified Bukkit-equivalent behavior

## Verification Policy

- If the next slice changes runtime-visible behavior, run:

```bash
./gradlew runGameTest --rerun-tasks
./gradlew build --rerun-tasks
```

- If the next slice is limited to non-runtime compatibility internals, at minimum run the narrow relevant unit tests and then decide whether GameTest coverage also needs to move.
- Latest targeted syntax-import verification already completed in this slice:

```bash
./gradlew test --tests org.skriptlang.skript.fabric.runtime.SilentSyntaxTest --tests org.skriptlang.skript.fabric.runtime.InvulnerableSyntaxTest --tests org.skriptlang.skript.fabric.runtime.AliveKillSyntaxTest --rerun-tasks
```

- Latest full verification already completed in this slice:

```bash
./gradlew runGameTest --rerun-tasks
./gradlew build --rerun-tasks
```

## Prompt To Give The Next Agent

```text
/Users/qf/IdeaProjects/Skript-Fabric-port/NEXT_AGENT_HANDOFF.md 를 먼저 읽고 이어서 작업해.

현재 우선순위:
- 기존 Stage 8 상태는 유지하되, 다음 package-local Bukkit audit로 바로 가지 말 것
- upstream `ch/njol/skript` 구현을 먼저 진행할 것
- exact user-visible syntax import 는 그 다음 blocking closure slice 가 닫힌 뒤 이어갈 것
- canonical docs 는 `docs/porting` 아래에 있고, 루트 문서는 포인터임

현재 상태:
- Condition 28/28 완료
- Expression 84/84 완료
- Effect 24/24 완료
- Stage 5 closure 22/22
- Stage 8 package-local audit 23/214
- latest verified GameTest 230/230
- build 통과
- new `ch/njol/skript` baseline: local 129 / upstream 1189

중요 제약:
- 사용자 Skript 문법에 fabric 접두/접미사를 넣지 마
- block/item/status effect resource id 는 bare id 허용, namespace 없으면 minecraft 기본값
- registry lookup 우선
- compile-only 작업 금지, user-visible behavior 는 real `.sk` + Fabric GameTest 로 검증
- parity가 안 된 건 완료라고 말하지 마
- dirty worktree 정리하지 마

다음 작업:
- upstream 에서 아직 빠진 user-visible syntax family 를 먼저 찾고, exact syntax 그대로 추가해
- `docs/porting/CH_NJOL_SKRIPT_AUDIT.md` 기준으로 `Part 1A` / `Part 1B` 는 그 syntax family 를 막을 때만 같이 진행해
- upstream 역할과 local behavior 를 class-by-class 로 대조해
- placeholder/stub 경로를 실제 동작으로 바꾸고, 필요한 테스트를 추가해
- 현재 닫힌 것:
  - real `.sk` base entity-state/control syntax:
    - `%entities% are alive/dead`
    - `%entities% are silent`
    - `%entities% are invulnerable/invincible`
    - `kill %entities%`
    - `silence %entities%`
    - `unsilence %entities%`
    - `make %entities% silent`
    - `make %entities% invulnerable/vulnerable`
  - real `.sk` conditional `if / else if / else` chain
  - real `.sk` parenthesized conditional chain
  - real `.sk` `parse if` / `else parse if` chain
  - real `.sk` multiline `if any` / `if all` plus `then`
  - real `.sk` implicit conditional section chain
  - real `.sk` plain-effect section ownership via `set {_component} to a blank equippable component:`
  - real `.sk` section-local propagation via custom damage source / potion effect / loot context creation sections
  - section-local variable copy-back through `Variables.withLocalVariables(...)`
  - real `.sk` `parse if` skipped-invalid-body load path
  - minimal raw-regex capture support for registered syntax patterns like `if <.+>`
  - minimal leading `implicit:` tag forwarding for registered conditional section patterns
  - shared parser/pattern matcher support for general `tag:` metadata and XOR parse marks via `¦`
  - loader fallback choosing the more specific retained section-vs-statement diagnostic
  - plain conditions no longer silently parsing as section headers
  - natural numeric ordering for variable prefix/list iteration and real `.sk` list ordering coverage
  - generic registered `Section` loading through `ScriptLoader.loadItems(...)`
  - `SecIf` registered `Section` loading through the normal syntax registry and bootstrap path
  - validator-backed `options:` loading from both runtime `EntryNode` trees and raw `SimpleNode` `key: value` entries
  - typed `%classinfo% input` parser path
  - specific section-ownership diagnostics retained through real `ParseLogHandler` / `SkriptLogger` / `Statement.parse(...)`
- 변경 후 관련 문서를 같은 turn 에 갱신해
```
