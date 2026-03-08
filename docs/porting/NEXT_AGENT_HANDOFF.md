# Next Agent Handoff

Last updated: 2026-03-08

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
- Latest verified runtime baseline from 2026-03-08:
  - `./gradlew runGameTest --rerun-tasks` passed
  - `./gradlew build --rerun-tasks` passed
  - `197 / 197` scheduled Fabric GameTests completed without build failure

## Priority Shift

Do not continue the next Stage 8 package-local Bukkit slice yet.
The user explicitly reprioritized the broader upstream `ch/njol/skript` package surface first.

New immediate priority:

- keep the current Stage 8 status frozen and accurately recorded
- audit and close the broader `ch/njol/skript` surface against upstream
- continue with `lang` and its immediate dependency cluster before importing larger missing syntax packages

## Latest Closure Slice

- merged the next coordinator follow-up slice in `Lane C -> Lane B -> Lane A` order, then reran full coordinator verification
- Lane C closed the explicit literal-pattern ordering follow-up:
  - shared literal-pattern matches returned by `Classes.getPatternInfos(...)` now honor the same stable class-info ordering used by superclass lookup and parser-backed class iteration
  - added a regression in [src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java) proving shared aliases respect `before(...)` / `after(...)` ordering
- Lane B carried no new code in this follow-up cycle:
  - the merged empty auto-tag derivation closure remains intact and green
- Lane A closed section-level execution-intent propagation:
  - `TriggerItem.walk(...)` now honors nested `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` results instead of always falling through to the current section's next item
  - `TriggerSection` now exposes loader-visible stop intent derived from its nested trigger items, so `ScriptLoader.loadItems(...)` also warns when a registered section body makes following sibling lines unreachable
  - added regressions in [src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java](../../src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java) that cover section-contained stop-trigger short-circuiting, section-local `stopSection`, and the corresponding warning behavior
- latest verification for this merged slice:
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew runGameTest --rerun-tasks` passed with `197 / 197`
  - `./gradlew build --rerun-tasks` passed

## What Landed In This Slice

- moved the canonical porting docs under `docs/porting`
- kept root-level doc filenames as entrypoint pointers so existing prompts do not break
- recorded the current verified Stage 8 baseline in the canonical status and handoff docs
- added a dedicated persistent audit document for the new workstream:
  - [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- measured the upstream/local baseline for `ch/njol/skript`:
  - upstream snapshot `e6ec744`: `1189` Java files
  - local tree: `119` Java files
  - net missing relative surface: `1070` Java files
- kept the first concrete closure target as `Part 1A: lang parser/runtime closure`
- started `Part 1B` in parallel because `Classes`, `Variables`, `config`, and `structures` behavior is already being tightened in the same dependency cluster
- landed the current `Part 1A` / `Part 1B` slices:
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
  - `SkriptParser.ParseResult` now carries `mark`, and parser init paths now receive general parse tags from matched branches instead of only the earlier hardcoded leading `implicit:` case
  - bare leading `:` metadata now auto-derives from the following literal or choice branch on the current compatibility surface
  - `ScriptLoader.loadItems(...)` now restores the more specific retained section-versus-statement diagnostic when both parse paths fail on a section node
  - `Statement.parse(...)` now rejects plain conditions used as section headers instead of silently returning a body-less condition item
  - `Classes` now sorts class infos by assignable-type specificity plus `before(...)` / `after(...)` dependencies instead of raw registration order
  - shared literal-pattern matches now also follow that stable class-info ordering when multiple class infos register the same alias
  - `ScriptLoader.loadItems(...)` now emits unreachable-code warnings behind `ScriptWarning.UNREACHABLE_CODE` suppression when a previously loaded statement stops further execution
  - nested section-contained stop-trigger and stop-section intents now propagate through `TriggerItem.walk(...)`, and registered sections now surface stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
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
  - `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing section-level execution-intent propagation
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed after closing explicit literal-pattern ordering parity
  - `./gradlew runGameTest --rerun-tasks` passed with `197 / 197`
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
- [src/main/java/ch/njol/skript/registrations/Classes.java](../../src/main/java/ch/njol/skript/registrations/Classes.java)
- [src/main/java/ch/njol/skript/sections/SecIf.java](../../src/main/java/ch/njol/skript/sections/SecIf.java)
- [src/main/java/ch/njol/skript/structures/StructOptions.java](../../src/main/java/ch/njol/skript/structures/StructOptions.java)
- [src/main/java/ch/njol/skript/variables/Variables.java](../../src/main/java/ch/njol/skript/variables/Variables.java)
- [src/main/java/org/skriptlang/skript/lang/entry/KeyValueEntryData.java](../../src/main/java/org/skriptlang/skript/lang/entry/KeyValueEntryData.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondCompare.java](../../src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondCompare.java)
- [src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java](../../src/main/java/org/skriptlang/skript/bukkit/base/effects/EffChange.java)
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
- [src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java)
- [src/test/java/ch/njol/skript/sections/SecIfCompatibilityTest.java](../../src/test/java/ch/njol/skript/sections/SecIfCompatibilityTest.java)
- [src/test/java/ch/njol/skript/structures/StructureEntryValidatorCompatibilityTest.java](../../src/test/java/ch/njol/skript/structures/StructureEntryValidatorCompatibilityTest.java)
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
- Entire upstream top-level packages are still absent locally, including `effects`, `events`, `entity`, `command`, `aliases`, and others listed in the audit doc.
- `ch/njol/skript/lang` is close in file count but not in behavioral completeness.
- `Part 1A` and `Part 1B` are both active now, but broader statement orchestration, loader/config hint flow, input-source usage, variable semantics, and type-system closure are still pending.
- pure registered section loading is now closed in `ScriptLoader`, loader fallback now restores the better retained section-versus-statement diagnostic, nested section-contained stop-trigger intent now propagates through loader/runtime, plain conditions no longer masquerade as section headers, and `SecIf` now also runs through that registered path with `parse if` / `else parse if`, multiline `if any` / `if all`, `then`, and implicit condition sections; the remaining gap is broader loader/config hint flow.
- do not reopen the just-closed inline optional-whitespace and inline alternation parser gap unless a new failing unit reproducer or real `.sk` path appears; the current verified closure covers `on[to]`, `when injured`, and `not breedable`
- the shared parser matcher now forwards general parse tags and XOR marks on the current compatibility surface and now also derives the current bare leading `:` auto-tags again, but broader upstream pattern element-graph parity is still open
- validator-backed recursive `options:` loading for both runtime `EntryNode` trees and manual raw simple-entry trees is now closed, but broader structure/config validation and diagnostics are still pending.
- the specific list-variable `set {target::*} to {source::*}` reindexing path and natural numeric ordering for prefix/list iteration are now closed, and shared literal-pattern matches now follow stable class-info ordering; remaining variable and class-registry gaps are deeper runtime semantics beyond these ordering paths.
- grouped-parenthesis condition parsing is now closed for the current real-script `if` path, but general statement/orchestration parity is still open.
- do not reopen the just-closed comment-aware loader parsing unless a new failing unit reproducer or real `.sk` path appears

## Recommended Next Priority

Continue `Part 1A: lang parser/runtime closure`, while keeping the already-started `Part 1B` dependency closure in sync.

Recommended order:

1. audit upstream versus local behavior for:
   - `ch/njol/skript/lang/SkriptParser`
   - `ch/njol/skript/lang/Statement`
   - `ch/njol/skript/ScriptLoader`
   - `ch/njol/skript/sections/SecIf`
   - `ch/njol/skript/expressions/ExprInput`
   - `ch/njol/skript/registrations/Classes`
   - `ch/njol/skript/variables/Variables`
2. prioritize the still-open parser/runtime gaps that were not closed in this slice:
   - `Statement` orchestration and diagnostics beyond the now-closed specific parse-error retention path
   - remaining `ScriptLoader` execution-flow gaps beyond options replacement and comment-aware line splitting
   - remaining `Classes` and `Variables` behavior that still diverges from upstream
   - broader `InputSource` usage paths beyond the now-landed typed `input` forms
3. land real behavior, not just placeholders, and add regression coverage
4. when the change affects user-visible script behavior, add real `.sk` plus Fabric GameTest coverage
5. update [PORTING_STATUS.md](PORTING_STATUS.md), this handoff, and [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md) in the same turn
6. only resume the next Stage 8 package-local Bukkit audit slice after the first `lang` closure part is landed, unless the user redirects again

## Verification Policy

- If the next slice changes runtime-visible behavior, run:

```bash
./gradlew runGameTest --rerun-tasks
./gradlew build --rerun-tasks
```

- If the next slice is limited to non-runtime compatibility internals, at minimum run the narrow relevant unit tests and then decide whether GameTest coverage also needs to move.
- Latest targeted non-runtime verification already completed in this slice:

```bash
./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks
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
- upstream `ch/njol/skript` surface audit/closure 를 먼저 진행할 것
- canonical docs 는 `docs/porting` 아래에 있고, 루트 문서는 포인터임

현재 상태:
- Condition 28/28 완료
- Expression 84/84 완료
- Effect 24/24 완료
- Stage 5 closure 22/22
- Stage 8 package-local audit 23/214
- latest verified GameTest 196/196
- build 통과
- new `ch/njol/skript` baseline: local 119 / upstream 1189

중요 제약:
- 사용자 Skript 문법에 fabric 접두/접미사를 넣지 마
- block/item/status effect resource id 는 bare id 허용, namespace 없으면 minecraft 기본값
- registry lookup 우선
- compile-only 작업 금지, user-visible behavior 는 real `.sk` + Fabric GameTest 로 검증
- parity가 안 된 건 완료라고 말하지 마
- dirty worktree 정리하지 마

다음 작업:
- `docs/porting/CH_NJOL_SKRIPT_AUDIT.md` 기준으로 `Part 1A` 를 계속 진행하고, 이미 시작된 `Part 1B` 의존성 closure 와 같이 움직여
- upstream 역할과 local behavior 를 class-by-class 로 대조해
- placeholder/stub 경로를 실제 동작으로 바꾸고, 필요한 테스트를 추가해
- 현재 닫힌 것:
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
