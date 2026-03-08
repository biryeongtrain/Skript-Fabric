# Skript-Fabric Porting Status

Last updated: 2026-03-08

## Goal

- Port `Skript` from its Bukkit/Paper runtime to a Fabric `1.21.8` server-side mod.
- Preserve upstream class roles and user-visible Skript behavior where practical.
- Verify user-visible behavior through real `.sk` scripts and Fabric GameTest.

## Non-negotiable Constraints

- Server-side only runtime.
- No `fabric` prefix or suffix in end-user Skript syntax unless absolutely unavoidable.
- Bare block/item/status-effect ids must stay valid and default to `minecraft:` when the namespace is omitted.
- Prefer registry lookup over hardcoded tables for moddable game data.
- Do not call parity complete unless it is actually verified.
- Do not do compile-only closure work for syntax claims.

## Current Verified Fabric Baseline

- Source-level condition port: `28 / 28` complete.
- Source-level expression port: `84 / 84` complete.
- Source-level effect port: `24 / 24` complete.
- Stage 5 event backend closure: `22 / 22` tracked rows active.
- Stage 8 parity audit: `in_progress`.
- Package-local Stage 8 audit progress: `23 / 214`.
- Package-local parity-complete slice:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Remaining package-local Stage 8 scope: `191 / 214`.
- Top-level non-package Bukkit helpers still outside the package-local matrix: `4`.
- Cross-cutting Stage 8 gap still open:
  - ambiguous bare item-id generic compare is not parity-complete yet, for example `event-item is wheat`
- Latest runtime verification:
  - `./gradlew build --rerun-tasks` passed on 2026-03-08
  - build path executed `runGameTest` successfully on 2026-03-08
  - `199 / 199` scheduled Fabric GameTests completed without build failure

## Priority Shift On 2026-03-08

Further Stage 8 package-local audit is temporarily deprioritized.
The immediate priority is now the broader upstream `ch/njol/skript` surface, because the local tree still contains mostly partial compatibility scaffolding and large missing package areas.

Detailed tracking for this workstream lives in [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md).

## `ch/njol/skript` Inventory Snapshot

Baseline reference used for the new audit:

- upstream repository: `SkriptLang/Skript`
- branch intent: `master`
- snapshot commit captured locally on 2026-03-08: `e6ec744`

Measured source counts:

- upstream `src/main/java/ch/njol/skript`: `1189` Java files
- local `src/main/java/ch/njol/skript`: `128` Java files
- net missing local surface relative to that snapshot: `1061` Java files

Top-level upstream packages missing locally entirely:

- `aliases`
- `bukkitutil`
- `command`
- `doc`
- `effects`
- `entity`
- `events`
- `hooks`
- `literals`
- `test`
- `timings`
- `update`

Key local package counts versus upstream:

- `lang`: local `81`, upstream `85`
- `expressions`: local `3`, upstream `391`
- `conditions`: local `1`, upstream `135`
- `classes`: local `2`, upstream `28`
- `util`: local `8`, upstream `57`
- `variables`: local `2`, upstream `11`
- `config`: local `6`, upstream `20`
- `registrations`: local `2`, upstream `10`
- `patterns`: local `11`, upstream `14`
- `log`: local `4`, upstream `17`
- `sections`: local `1`, upstream `10`
- `structures`: local `1`, upstream `10`
- `localization`: local `2`, upstream `11`

## Why `lang` Goes First

`ch/njol/skript/lang` is the highest-leverage closure slice:

- file-count parity is already close: local `81`, upstream `85`
- the four-file delta is only `package-info.java` coverage, not substantive runtime classes
- behavior is still incomplete in several foundational classes, for example:
  - `ch/njol/skript/lang/SkriptParser`
  - `ch/njol/skript/lang/Statement`
  - `ch/njol/skript/lang/TriggerSection`
  - `ch/njol/skript/ScriptLoader`
  - `ch/njol/skript/sections/SecIf`
  - `ch/njol/skript/expressions/ExprInput`
  - `ch/njol/skript/variables/Variables`
  - `ch/njol/skript/registrations/Classes`
- closing these foundations reduces risk before importing larger missing syntax packages.

## Part 1A / Part 1B Progress

The core closure workstream is active.
`Part 1A` remains in progress, and `Part 1B` has now started because `Classes`, `Variables`, `config`, and `structures` behavior is already being tightened in the same dependency cluster.

Landed slices so far:

- input-source parser/runtime closure:
  - `ch/njol/skript/expressions/ExprInput` is no longer only a minimal compatibility stub
  - `ch/njol/skript/lang/SkriptParser` now resolves `input`, typed `%classinfo% input`, and `input index` directly when an `InputSource` context is active
  - `Classes` now normalizes compact, spaced, hyphenated, and plural user type names for class-info lookup in parser-facing compatibility paths
  - `InputSourceCompatibilityTest` now covers current-value resolution, typed input resolution, spaced class-name matching, indexed input resolution, and plural typed-input rejection
- if-section chain closure:
  - `SkriptParser` now supports minimal raw regex captures in registered syntax patterns, plus the minimal leading `implicit:` tag needed by registered conditional sections
  - `SkriptParser` now also preserves required whitespace around omitted inline optional groups and inline alternation branches inside token patterns for the current natural-script compatibility surface
  - `SecIf` is now registered as a real `Section` and loads through the normal section registry path instead of depending on a `Statement.parse(...)` special-case
  - `Statement.parse(...)` no longer needs a dedicated `SecIf` fallback
  - `ch/njol/skript/sections/SecIf` now executes chained `if / else if / else` sections instead of only isolated `if` sections
  - `SecIf` now also supports `parse if` and `else parse if` through the registered section path
  - `SecIf` now also supports multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run`
  - `SecIf` now supports implicit conditional sections such as `%condition%:`, which run through the same chain logic as explicit `if %condition%:`
  - parse-time false branches now skip section-body loading, which preserves upstream-style behavior where invalid skipped bodies do not break script load
  - `ch/njol/skript/lang/Condition` now unwraps outer grouping parentheses before condition parsing
  - `CondCompare` now keeps generic-object variable comparisons available to `if` conditions without stealing entity-specific condition syntax
  - real base `.sk` coverage now includes direct conditional chains, parenthesized conditional chains, parse-time conditional chains, multiline `if any` / `if all` plus `then`, implicit condition sections, and invalid-body skip paths
- parser natural-script closure:
  - omitted inline optional whitespace now stays valid in registered patterns such as `%objects% can be (equipped|put) on[to] entities`
  - inline alternation branches with trailing whitespace now stay valid in registered patterns such as `make %entities% (not |non(-| )|un)breedable`
  - nested grouped optional and alternation content now stays valid in the current natural-script compatibility surface, including `%objects% will (lose durability|be damaged) ... when [[the] wearer [is]] injured`
  - `SkriptParserRegistryTest` now rechecks the exact parser paths that recovered those live real-script forms
- parser tag / mark closure:
  - the shared compiled matcher now lives under `ch/njol/skript/patterns`, so `SkriptParser` and direct pattern compilation use the same compatibility path
  - `SkriptParser.ParseResult` now carries `mark`, and init paths now receive general parse tags from matched branches instead of only the earlier hardcoded leading `implicit:` case
  - `SkriptParser.ParseResult.tags` and the shared matcher now preserve duplicate parse tags in encounter order instead of collapsing them into a unique set
  - `PatternCompiler` / `SkriptPattern` now support placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦` on the current compatibility surface
  - `PatternCompiler` now also builds a lightweight `PatternElement` graph, and `SkriptPattern` now exposes `countTypes()`, `countNonNullTypes()`, and `getElements(...)` for the current upstream-introspection compatibility surface
  - bare leading `:` metadata now auto-derives from following literal and choice branches on the current compatibility surface, so forms such as `:future`, `:non(-| )`, and `:(min|max)[imum]` reach `ParseResult` again
  - unmatched optional and alternation-scoped raw-regex captures now stay matchable through the shared matcher path instead of failing `ParseResult` construction
  - `PatternCompiler` now also preserves placeholder-local parse flags (`*` / `~`), leading optional markers, plural metadata, and `@time` metadata through `TypePatternElement`
  - `SkriptPattern` now applies placeholder-local parse flags plus `@time` when turning captured text into expressions through the shared matcher, while leaving placeholder plurality metadata exposed but non-enforcing on the current green corpus
  - `SkriptParser.parseModern(...)` and `parseStatic(...)` now backfill omitted non-optional placeholder values from parser-scoped `DefaultValueData` when the exact pattern and parser supply a valid default, while matcher captures still stay null for the omitted form of `default number [%number%]`
  - `PatternCompilerCompatibilityTest` and `SkriptParserRegistryTest` now cover branch tags, parse marks, and the already-green inline optional whitespace natural form through the shared matcher
- script-loading options closure:
  - `options:` entries are now represented by `EntryNode`
  - `StructOptions` is now present locally
  - `ScriptLoader.replaceOptions(...)` now performs actual option substitution
  - the runtime top-level structure load path now preserves and applies options in real `.sk` files
  - `Skript.registerStructure(...)` now has an `EntryValidator` overload, so validator-backed top-level structures can register through the compatibility surface instead of reaching into the modern registry directly
  - `KeyValueEntryData` now accepts both raw `SimpleNode` entries and runtime-shaped `EntryNode` entries, which closes the mismatch between the entry-validation API and the active Fabric `.sk` loader
  - `StructOptions` now loads through a recursive validator-backed entry path, so runtime `EntryNode` children and manual raw `key: value` simple nodes are both accepted without dropping nested option sections
  - invalid nested option simple lines still log `Invalid line in options` without rejecting the rest of the valid option tree
- `ScriptLoader.loadItems(...)` now attempts registered `Section` parsing for section nodes before falling back to statement parsing, so pure section syntax can actually load and execute child trigger items
  - `Statement.parse(...)` now routes effect parsing through `Effect.parse(...)`, so plain effects with section-managing expressions can own and execute their section body instead of silently dropping it
- config/container foundation closure:
  - `Node.remove()` now detaches from the current parent instead of forcing callers to coordinate parent cleanup manually
  - `SectionNode` now has case-insensitive mapped lookup, ordered replace/remove, parent reassignment on move, and `convertToEntries(...)` map synchronization through a local `NodeMap`
  - `SectionNode` behavior now better matches the map-like semantics expected by entry-validation and structure-loading code
  - `Effect.parse(...)` now opens `SectionContext` for plain effects and rejects `:` bodies that no effect-side syntax actually claims
- parse-log / statement diagnostic closure:
  - `Skript.error(...)`, `Skript.warning(...)`, and `Skript.debug(...)` now flow through a parse-log-aware `SkriptLogger`
  - `ParseLogHandler` now retains specific parse errors across nested parser scopes instead of acting as an empty shim
  - `Statement.parse(...)` now stops on captured function/effect/condition parse errors and prints the retained diagnostic instead of falling through to a generic section fallback
  - `Statement.parse(...)` now also keeps failed effect/condition init diagnostics non-terminal until the plain registered-statement path has been tried, so a later same-pattern statement can still load while the best prior specific error is restored if nothing ultimately matches
  - `Statement.parse(...)` now also clears any inherited outer `Section.SectionContext` owner when parsing plain statements (`node == null`), so nested function/effect/condition arguments do not accidentally inherit an enclosing expression section
  - `ScriptLoaderCompatibilityTest` now proves that a valid effect used as a section keeps its specific ownership error without also logging `Can't understand this section`
  - `ScriptLoaderCompatibilityTest` now also proves that exact `set {_var} to true:` retains the specific `EffChange` ownership diagnostic instead of collapsing to `Can't understand this section`
- loader fallback diagnostic closure:
  - `ParseLogHandler` now exposes snapshot/restore helpers and retained-error accessors so loader-owned section fallback can compare section and statement diagnostics without printing both
  - `ScriptLoader.loadItems(...)` now retries section-node parsing through both `Section.parse(...)` and `Statement.parse(...)`, then restores the more specific retained diagnostic instead of defaulting to the generic fallback
  - plain conditions used as section headers now fail with a specific ownership error instead of silently returning a body-less condition item
- loader unreachable-code warning closure:
  - `ScriptLoader.loadItems(...)` now emits `Unreachable code. The previous statement stops further execution.` when a previously loaded `Statement` advertises a stopping `ExecutionIntent`
  - that warning now respects `ScriptWarning.UNREACHABLE_CODE` suppression on the active script
  - nested `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` results now propagate through `TriggerItem.walk(...)`, and registered sections now surface stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
  - real base `.sk` coverage now verifies both warning emission and runtime short-circuiting through `unreachable_code_warning_stop_test_block.sk`
- loader hint-scope closure:
  - `ParserInstance` now owns an upstream-style `HintManager`, resets it per script load, and exposes the active local-variable hint stack to parser/runtime compatibility code
  - `ScriptLoader.loadItems(...)` now opens section hint scopes, freezes them behind stopping statements, and merges `stopSection` hints into the resumed sibling scope
  - `parseSectionTriggerItem(...)` now opens a temporary non-section hint scope around `Section.parse(...)`, so failed section parsing clears temporary hints before statement fallback runs
  - `ScriptLoaderCompatibilityTest` now covers failed-section hint rollback, successful sibling propagation, stop-trigger scope freezing, and stop-section hint merging
- class/type registry closure:
  - `ClassInfo` and `Classes` now close the missing codename, literal-pattern, and supertype resolution behavior that the parser/runtime depends on
  - `Classes` now also computes stable class-info ordering that prefers narrower assignable types and honors `before(...)` / `after(...)` dependencies instead of using raw registration order
  - shared literal-pattern matches returned by `Classes.getPatternInfos(...)` now follow that same stable ordering when multiple class infos register the same alias
  - `Classes.getParser(...)` now also falls back through registered converters after direct parser lookup, so converter-backed parser owners can still satisfy requested class infos on the current compatibility surface
  - `Classes.parse(...)` now falls back through registered converters after direct parser lookup, so converter-backed source types can still satisfy requested class infos on the current compatibility surface
  - `Classes.parse(...)` now clears stale direct-parser failures before later parser or converter fallback success, so successful fallback no longer leaks earlier parser diagnostics
- variable/runtime closure:
  - `SkriptParser` now recognizes `{...}` variable expressions directly
  - `Variable.newInstance(...)` now consumes parse-time local variable type hints through `ParserInstance` / `HintManager`, narrows generic `%object%` locals when a concrete hint is known, and rejects incompatible typed local lookups with a diagnostic
  - `Variables` now defaults to case-insensitive storage/lookup with a compatibility switch for case-sensitive operation
  - `Variables.withLocalVariables(...)` now follows upstream copy-back semantics for nested section-event execution instead of restoring the previous target snapshot
  - `Variable` no longer recommends preserving source keys for list-to-list `set`, so keyed list sources are reindexed into numeric target slots instead of leaking source keys
  - prefix/list iteration now uses natural variable-name ordering, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set` reindexing
  - list variables now expose the legacy loop aliases `index`, `var`, `variable`, and `value`, and their predicate checks again use upstream-style all-values `getAnd()` semantics
  - `EffChange` now forwards keyed deltas only when the source expression explicitly recommends keyed preservation
  - quoted string literals now remain string literals in generic `%object%` contexts instead of being consumed by registry-backed parsers during live script loading
- section-expression object-safe closure:
  - custom damage source, potion effect, and loot-context section expressions now tolerate object-backed local values instead of assuming typed runtime arrays
  - real expression `.sk` coverage now includes section-local propagation for custom damage source, potion effect, and loot context creation sections
- comment-aware runtime script parsing closure:
  - `Node.splitLine(...)` now strips inline comments, preserves quoted `#`, unescapes doubled `##`, and tracks `###` block comments
  - `SkriptRuntime.parseScript(...)` now uses that split logic, so trailing comments on section headers, option entries, conditions, and effects no longer break live `.sk` loading
  - real base `.sk` coverage now includes a comment-aware loader fixture with commented section headers, commented option entries, quoted hashes, and block-commented invalid syntax
- real `.sk` coverage added in the base GameTest slice:
  - options replacement path
  - comment-aware loader parsing path
  - mixed-case variable set/lookup path
  - conditional `if / else if / else` chain path
  - parenthesized conditional chain path
  - `parse if` / `else parse if` conditional chain path
  - multiline `if any` / `if all` with `then` path
  - implicit conditional section path
  - `parse if` skipped-invalid-body path
  - list variable reindexing path for `set {target::*} to {source::*}`
  - natural numeric list ordering path for `{source::2}` before `{source::10}`
  - unreachable-code warning plus stop-trigger short-circuit path
  - plain-effect section ownership plus local-variable copy-back through `set {_component} to a blank equippable component:`
  - plain-effect argument parsing inside an outer expression section path
  - statement fallback after failed effect parse through `ambiguous loader syntax`

Targeted verification completed on 2026-03-08:

- `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed after closing comment-aware runtime script parsing
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --tests ch.njol.skript.config.SectionNodeCompatibilityTest --rerun-tasks` passed after closing config map semantics and validator-backed structure entry bridging
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --rerun-tasks` passed after preserving invalid nested `options:` diagnostics on the validator-backed path
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after closing plain-effect section ownership and section-local copy-back
- `./gradlew test --tests org.skriptlang.skript.bukkit.potion.elements.PotionEntityObjectCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocationCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContextCompatibilityTest --rerun-tasks` passed after closing object-backed section-expression locals for custom damage source / potion effect / loot context paths
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed after closing parse-log retention and statement diagnostic fallback behavior
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing section-vs-statement loader fallback diagnostics and plain-condition section-header rejection
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after moving the shared matcher into `patterns` and forwarding general parse tags plus XOR marks
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after closing natural numeric variable-name ordering for list/prefix iteration
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after closing bare leading `:` auto-tag derivation
- `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed after closing class-info ordering semantics
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing loader unreachable-code warnings
- `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed after closing explicit literal-pattern ordering parity
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing section-level execution-intent propagation
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after closing parse-time local variable type hints
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after adding lightweight pattern element graph APIs
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing loader hint-scope lifecycle
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after closing converter-backed parser helper fallback plus parser-owned default-value backfill
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsSpecificSectionOwnershipErrorForSetTrueSyntax --rerun-tasks` passed after locking the exact `set {_var} to true:` ownership regression
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging parse-log-aware `Classes.parse(...)`, ordered duplicate parser tags, and statement fallback after failed effect/condition init
- `./gradlew runGameTest --rerun-tasks` passed with `199 / 199`
- `./gradlew build --rerun-tasks` passed, including the full Fabric GameTest path and `199 / 199` scheduled Fabric GameTests

## Foundation Already Landed Before This Pivot

The repository is not starting from zero.
The following foundations were already built before this priority shift:

- compatibility-oriented `ch.njol.skript.lang` class surface, including expressions, statements, triggers, sections, parser helpers, simplification helpers, variable strings, and function namespaces
- parser and syntax registry bridge work, including modern and legacy pattern matching, typed placeholder parsing, section-aware statement parsing, and parser-stack tracking
- function compatibility scaffolding, including signatures, registries, dynamic references, expression/effect call wrappers, and namespace fallback behavior
- variable and literal compatibility primitives, including `Variable`, `Variables`, `LiteralString`, `UnparsedLiteral`, `InputSource`, and section-expression helpers
- foundational utility scaffolding in `classes`, `config`, `log`, `patterns`, `registrations`, `util`, and `variables`
- active Fabric runtime harness and Fabric GameTest suite with `199 / 199` passing tests on the last code-verification run
- Stage 8 parity-audited package-local Bukkit slice for `breeding`, `input`, and `interactions`

## Current Gaps

- most upstream `ch/njol/skript` packages are still absent or only minimally scaffolded locally
- several present core classes still contain behaviorally incomplete paths, placeholder returns, or minimal-stub contracts
- upstream comparison showed that some earlier suspected gaps were false positives and should not be reopened:
  - `TriggerSection.run(...)` throwing `UnsupportedOperationException` matches upstream behavior
  - `EffFunctionCall.init(...)` and `ExprFunctionCall.init(...)` returning `false` on direct wrapper instances also match upstream behavior
- current Stage 8 package-local audit for `org/skriptlang/skript/bukkit` remains valid, but it is no longer the only gating audit track
- `Part 1A` and `Part 1B` are both active, but most parser, statement, loader, variable, and type-system closure work remains open
- generic registered section loading is now closed in `ScriptLoader`, `ScriptLoader` now also restores the more specific section-versus-statement fallback diagnostic, propagates nested section-contained stop-trigger intent through loader/runtime, warns about unreachable code behind script-level warning suppression, plain conditions no longer masquerade as section headers, and now carries a first upstream-style hint-scope lifecycle; broader loader/config hint flow and built-in hint producers are still incomplete
- validator-backed recursive `options:` loading for runtime `EntryNode` trees and raw simple-entry trees is now closed, but broader structure/config validation behavior is still much thinner than upstream
- the parser no longer regresses the currently verified natural-script inline optional/alternation forms, now forwards general tags/XOR marks through the shared matcher, derives the current bare leading `:` auto-tags again, no longer fails on omitted optional/alternation raw-regex captures, and now exposes a lightweight `PatternElement` graph introspection API; broader upstream pattern element-graph/runtime parity is still incomplete
- natural numeric ordering for list/prefix iteration, legacy list-variable loop aliases, all-values list-check semantics, and parse-time local variable type hints are now closed, and shared literal-pattern matches now follow stable class-info ordering too, but broader `Variables` and class-registry runtime semantics are still incomplete

## Active Workstreams

1. Maintain the current verified Fabric runtime and Stage 8 records without overstating parity.
2. Audit and close the upstream `ch/njol/skript` surface, starting with `lang` and its immediate dependencies.
3. After the upstream `ch/njol/skript` closure track is materially landed, resume the broader Bukkit-behavior parity drive until user-visible behavior matches upstream as closely as it can be verified.

## Documentation Policy

For every future slice in the new workstream:

1. compare the target package or class role against the upstream baseline
2. record exact local coverage and exact missing behavior
3. add or tighten real `.sk` plus Fabric GameTest coverage when user-visible behavior changes
4. update this status file, [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md), and [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md) in the same turn
