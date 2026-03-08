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
  - `195 / 195` scheduled Fabric GameTests completed without build failure

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
- local `src/main/java/ch/njol/skript`: `118` Java files
- net missing local surface relative to that snapshot: `1071` Java files

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
- `variables`: local `1`, upstream `11`
- `config`: local `6`, upstream `20`
- `registrations`: local `2`, upstream `10`
- `patterns`: local `2`, upstream `14`
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
  - `ScriptLoaderCompatibilityTest` now proves that a valid effect used as a section keeps its specific ownership error without also logging `Can't understand this section`
- class/type registry closure:
  - `ClassInfo` and `Classes` now close the missing codename, literal-pattern, and supertype resolution behavior that the parser/runtime depends on
- variable/runtime closure:
  - `SkriptParser` now recognizes `{...}` variable expressions directly
  - `Variables` now defaults to case-insensitive storage/lookup with a compatibility switch for case-sensitive operation
  - `Variables.withLocalVariables(...)` now follows upstream copy-back semantics for nested section-event execution instead of restoring the previous target snapshot
  - `Variable` no longer recommends preserving source keys for list-to-list `set`, so keyed list sources are reindexed into numeric target slots instead of leaking source keys
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
  - plain-effect section ownership plus local-variable copy-back through `set {_component} to a blank equippable component:`

Targeted verification completed on 2026-03-08:

- `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed after closing comment-aware runtime script parsing
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --tests ch.njol.skript.config.SectionNodeCompatibilityTest --rerun-tasks` passed after closing config map semantics and validator-backed structure entry bridging
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --rerun-tasks` passed after preserving invalid nested `options:` diagnostics on the validator-backed path
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after closing plain-effect section ownership and section-local copy-back
- `./gradlew test --tests org.skriptlang.skript.bukkit.potion.elements.PotionEntityObjectCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocationCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContextCompatibilityTest --rerun-tasks` passed after closing object-backed section-expression locals for custom damage source / potion effect / loot context paths
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed after closing parse-log retention and statement diagnostic fallback behavior
- `./gradlew runGameTest --rerun-tasks` passed with `195 / 195`
- `./gradlew build --rerun-tasks` passed, including the full Fabric GameTest path

## Foundation Already Landed Before This Pivot

The repository is not starting from zero.
The following foundations were already built before this priority shift:

- compatibility-oriented `ch.njol.skript.lang` class surface, including expressions, statements, triggers, sections, parser helpers, simplification helpers, variable strings, and function namespaces
- parser and syntax registry bridge work, including modern and legacy pattern matching, typed placeholder parsing, section-aware statement parsing, and parser-stack tracking
- function compatibility scaffolding, including signatures, registries, dynamic references, expression/effect call wrappers, and namespace fallback behavior
- variable and literal compatibility primitives, including `Variable`, `Variables`, `LiteralString`, `UnparsedLiteral`, `InputSource`, and section-expression helpers
- foundational utility scaffolding in `classes`, `config`, `log`, `patterns`, `registrations`, `util`, and `variables`
- active Fabric runtime harness and Fabric GameTest suite with `195 / 195` passing tests on the last code-verification run
- Stage 8 parity-audited package-local Bukkit slice for `breeding`, `input`, and `interactions`

## Current Gaps

- most upstream `ch/njol/skript` packages are still absent or only minimally scaffolded locally
- several present core classes still contain behaviorally incomplete paths, placeholder returns, or minimal-stub contracts
- upstream comparison showed that some earlier suspected gaps were false positives and should not be reopened:
  - `TriggerSection.run(...)` throwing `UnsupportedOperationException` matches upstream behavior
  - `EffFunctionCall.init(...)` and `ExprFunctionCall.init(...)` returning `false` on direct wrapper instances also match upstream behavior
- current Stage 8 package-local audit for `org/skriptlang/skript/bukkit` remains valid, but it is no longer the only gating audit track
- `Part 1A` and `Part 1B` are both active, but most parser, statement, loader, variable, and type-system closure work remains open
- generic registered section loading is now closed in `ScriptLoader`, and `SecIf` now uses the section registry path with `parse if` / `else parse if`, multiline `if any` / `if all`, `then`, and implicit condition sections too; specific statement parse-error retention is now real, but broader loader/config hint flow and richer parser tag/mark parity are still incomplete
- validator-backed recursive `options:` loading for runtime `EntryNode` trees and raw simple-entry trees is now closed, but broader structure/config validation behavior is still much thinner than upstream
- the parser no longer regresses the currently verified natural-script inline optional/alternation forms, but broader upstream tag/mark/pattern parity is still incomplete

## Active Workstreams

1. Maintain the current verified Fabric runtime and Stage 8 records without overstating parity.
2. Audit and close the upstream `ch/njol/skript` surface, starting with `lang` and its immediate dependencies.
3. Resume additional Stage 8 package-local Bukkit slices after the first `lang` closure slice is landed, unless the user reprioritizes again.

## Documentation Policy

For every future slice in the new workstream:

1. compare the target package or class role against the upstream baseline
2. record exact local coverage and exact missing behavior
3. add or tighten real `.sk` plus Fabric GameTest coverage when user-visible behavior changes
4. update this status file, [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md), and [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md) in the same turn
