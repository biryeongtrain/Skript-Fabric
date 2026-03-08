# `ch/njol/skript` Audit And Closure Plan

Last updated: 2026-03-08

## Scope

This document tracks the new priority workstream for the upstream `ch/njol/skript` surface.
It exists because the local tree only contains a narrow compatibility-oriented subset today.

Baseline used for this audit:

- upstream repository: `SkriptLang/Skript`
- branch intent: `master`
- locally captured snapshot on 2026-03-08: `e6ec744`
- local comparison target: `/Users/qf/IdeaProjects/Skript-Fabric-port/src/main/java/ch/njol/skript`

## Execution Rules

For every future slice:

1. confirm the upstream class or package role from the baseline snapshot
2. compare it with the current local implementation, not just file presence
3. if user-visible behavior changes, verify it with real `.sk` plus Fabric GameTest
4. if the slice is infrastructure-only, still add or tighten unit coverage where practical
5. update this document, `PORTING_STATUS.md`, and `NEXT_AGENT_HANDOFF.md` in the same turn
6. keep Stage 8 package-local Bukkit audit counts separate from this workstream

## Inventory Snapshot

Measured Java source counts:

- upstream `ch/njol/skript`: `1189`
- local `ch/njol/skript`: `119`
- local shortfall versus the captured upstream snapshot: `1070`

Local top-level packages currently present:

- `classes`
- `conditions`
- `config`
- `expressions`
- `lang`
- `localization`
- `log`
- `patterns`
- `registrations`
- `sections`
- `structures`
- `util`
- `variables`

Upstream top-level packages currently absent locally:

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

## Top-Level Package Matrix

| Package | Upstream | Local | Status | Priority | Notes |
| --- | --- | --- | --- | --- | --- |
| `aliases` | `12` | `0` | absent | `P2` | likely needed after core parser/type closure; do not replace registry-backed parsing with large hardcoded tables |
| `bukkitutil` | `26` | `0` | absent | `P3` | Bukkit-specific helpers; audit only when a Fabric replacement path is justified |
| `classes` | `28` | `2` | partial shim | `P1` | foundational for parsing and stringification; `Classes` now covers codename/literal/supertype lookup plus class-info ordering, but the layer is still far thinner than upstream |
| `command` | `9` | `0` | absent | `P2` | command/runtime integration depends on core parser and function closure first |
| `conditions` | `135` | `1` | partial shim | `P2` | user-visible syntax surface; import only after `lang` and type system stabilize |
| `config` | `20` | `6` | partial shim | `P1` | loader/parser support dependency |
| `doc` | `18` | `0` | absent | `P3` | low runtime value; defer |
| `effects` | `123` | `0` | absent | `P2` | large user-visible syntax surface; depends on parser/runtime closure |
| `entity` | `34` | `0` | absent | `P2` | syntax/runtime adapters; defer until base language closure |
| `events` | `53` | `0` | absent | `P2` | event classes are mostly still outside this package in the active Fabric path today |
| `expressions` | `391` | `3` | partial shim | `P2` | very large user-visible syntax surface; do not import before parser/type closure |
| `hooks` | `32` | `0` | absent | `P3` | external integration layer; defer |
| `lang` | `85` | `81` | present but behavior-incomplete | `P0` | local delta is effectively only `package-info.java`; this is the highest-leverage closure slice |
| `literals` | `16` | `0` | absent | `P2` | depends on parser/type behavior |
| `localization` | `11` | `2` | partial shim | `P2` | not blocking initial parser closure, but still largely absent |
| `log` | `17` | `4` | partial shim | `P1` | parse/runtime diagnostics still thin |
| `patterns` | `14` | `3` | partial shim | `P1` | foundational parsing dependency |
| `registrations` | `10` | `2` | partial shim | `P1` | foundational registration dependency |
| `sections` | `10` | `1` | partial shim | `P1` | section behavior now includes chained `if / else if / else`, `parse if` / `else parse if`, multiline `if any` / `if all` plus `then`, implicit condition sections, generic section nodes through `ScriptLoader`, and `SecIf` through the section registry path; remaining gaps are broader statement/log orchestration and richer parser tag/mark parity |
| `structures` | `10` | `1` | partial shim | `P1` | now active because `options:` support has started; keep in the dependency-closure track |
| `test` | `42` | `0` | absent | `P3` | low shipping-runtime value; use local test harnesses instead |
| `timings` | `2` | `0` | absent | `P3` | defer |
| `update` | `10` | `0` | absent | `P3` | defer |
| `util` | `57` | `8` | partial shim | `P1` | many dependencies feed back into parser, classes, and variables |
| `variables` | `11` | `1` | partial shim | `P1` | current local store is an in-memory bridge, not upstream-complete behavior |

## `lang` Breakdown

`lang` is numerically close but behaviorally incomplete.

Current counts:

- local `lang` total: `81`
- upstream `lang` total: `85`
- local root files: `47`
- local `function`: `14`
- local `parser`: `4`
- local `simplification`: `2`
- local `util`: `14`
- upstream root files: `48`
- upstream `function`: `15`
- upstream `parser`: `5`
- upstream `simplification`: `2`
- upstream `util`: `15`

The numeric delta is only the missing `package-info.java` files:

- `ch/njol/skript/lang/package-info.java`
- `ch/njol/skript/lang/function/package-info.java`
- `ch/njol/skript/lang/parser/package-info.java`
- `ch/njol/skript/lang/util/package-info.java`

That means the real gap is behavior, not class presence.

## Current Blocker Clusters

| Cluster | Current local signals | Why it matters | First closure target |
| --- | --- | --- | --- |
| Parser flow | `SkriptParser` now uses a shared compiled matcher, forwards general parse tags plus XOR marks, keeps the current natural forms green, and derives the current bare leading `:` auto-tags again; broader upstream pattern element-graph parity is still open | parser behavior controls every syntax import after this | `Part 1A` |
| Statement loading | `Statement.parse(...)` now retains specific function/effect/condition parse errors and rejects plain conditions used as section headers, but broader orchestration and hint flow are still thin | statement ordering and section ownership determine real script semantics | `Part 1A` |
| Script loading | `ScriptLoader.replaceOptions(...)` is real now, `loadItems(...)` now handles registered section nodes before falling back to statements, plain effects can own section-managing expressions through `Effect.parse(...)`, section-versus-statement fallback now restores the better retained diagnostic, stopping statements now emit unreachable-code warnings behind script-level warning suppression, nested section-contained stop-trigger intent now propagates through loader/runtime while `stopSection` stays local, and the active Fabric runtime parser strips inline comments plus `###` block comments through `Node.splitLine(...)`; the broader upstream parse/log/hint flow is still much thinner than upstream | script preprocessing and trigger-item construction parity are still incomplete | `Part 1A` |
| If-section support | `SecIf` now executes chained `if / else if / else`, `parse if` / `else parse if`, multiline `if any` / `if all` plus `then`, and implicit conditional sections through a registered section path, and `Condition.parse(...)` now unwraps grouped outer parentheses | basic conditional-section behavior is now much closer to upstream; remaining gaps are broader statement/log orchestration and richer parser tag/mark parity beyond the minimal `implicit:` support | `Part 1A` |
| Input-source compatibility | `ExprInput` now supports `input`, typed `%classinfo% input`, and `input index`; broader source usage paths are still not closed | input expressions depend on this bridge | `Part 1A` |
| Variable runtime | `Variables` now covers case-insensitive storage, copy-back semantics, list-to-list reindexing, and natural numeric ordering for prefix/list iteration, but it is still an in-memory bridge only | variable semantics affect function calls, sections, and expressions | `Part 1B` |
| Type/parse registry | `Classes` now covers codename/literal/supertype lookup, stable class-info ordering, and shared literal-match ordering, but remains a small compatibility layer relative to upstream | typed literal and parser behavior depend on it | `Part 1B` |

## Part Tracker

| Part | Scope | Status | Notes |
| --- | --- | --- | --- |
| `Part 0` | inventory, documentation move, canonical doc layout | `completed` | completed on 2026-03-08 |
| `Part 1A` | `lang` parser/runtime closure | `in_progress` | active implementation slices now include shared pattern matching, parser metadata flow, script loading, and variable-expression parsing |
| `Part 1B` | `variables` / `classes` / `config` / `patterns` / `registrations` / `log` dependency closure | `in_progress` | already started because options, class-registry, and variable-storage behavior is now being tightened |
| `Part 2` | import or replace missing user-visible `ch/njol/skript` syntax packages in priority order | `pending` | start with packages that directly depend on the now-closed parser/runtime core |
| `Part 3` | low-priority support packages (`doc`, `test`, `timings`, `update`, selected hooks/utilities) | `pending` | only after runtime-relevant surfaces are settled |

## Part 0 Progress Log

### 2026-03-08

- confirmed the user priority change before additional Stage 8 package-local Bukkit audit work
- moved canonical long-lived porting docs under `docs/porting`
- kept root-level filenames as entrypoint pointers for prompt compatibility
- froze and recorded the current Stage 8 verified baseline:
  - `23 / 214` package-local audited
  - `197 / 197` Fabric GameTests on the last runtime verification
  - `./gradlew build --rerun-tasks` passed on the last runtime verification
- measured the upstream/local `ch/njol/skript` source gap:
  - upstream `1189`
  - local `119`
- wrote the first top-level package matrix and selected `lang` as the first closure slice
- landed the current concrete `Part 1A` and `Part 1B` code slices:
  - `ExprInput` now resolves current input values instead of acting as a pure stub
  - `SkriptParser` now resolves `input`, typed `%classinfo% input`, and `input index` in active `InputSource` context
  - `Classes` now normalizes compact, spaced, hyphenated, and plural user type names for parser-facing class-info lookup
  - targeted unit coverage now rechecks current-value resolution, typed-input resolution, spaced class-name matching, indexed-value resolution, and plural typed-input rejection
  - `EntryNode`, `StructOptions`, and `ScriptLoader.replaceOptions(...)` now close the first real `options:` path
  - `Node.remove()`, local `NodeMap`, and expanded `SectionNode` behavior now close the missing case-insensitive lookup, ordered replace/remove, parent-reassignment, and entry-conversion map-sync behavior used by config/structure compatibility code
  - `Skript.registerStructure(...)` now accepts `EntryValidator`, and `KeyValueEntryData` now accepts both `SimpleNode` and `EntryNode` inputs on the compatibility path
  - `StructOptions` now uses a recursive validator-backed load path, so runtime `EntryNode` trees and raw simple `key: value` nodes both load through the same compatibility surface while preserving `Invalid line in options` diagnostics
  - `ScriptLoader.loadItems(...)` now attempts registered `Section` parsing for section nodes before falling back to statements, so pure section syntax can load and execute children through the normal trigger-item flow
  - `Statement.parse(...)` now routes effect parsing through `Effect.parse(...)`, and `Effect.parse(...)` now opens `SectionContext` for plain effects with section-managing expressions so their section body can actually load and execute
  - `ClassInfo` / `Classes` now close codename, literal-pattern, and supertype-resolution behavior used by the parser/runtime
  - `SkriptParser` now recognizes `{...}` variable expressions
  - `Variables` now defaults to case-insensitive storage/lookup with explicit tests for case-sensitive fallback
  - `Variables.withLocalVariables(...)` now copies nested section-event local-variable mutations back into the provider scope instead of restoring the old target scope
  - `Variable` now matches upstream list-variable copy behavior by not recommending keyed preservation for list-to-list `set`
  - `EffChange` now applies keyed deltas only when the source expression actually recommends them, and treats empty `SET` payloads as deletions when the target supports it
  - quoted string literals now stay string literals in generic `%object%` contexts instead of being consumed by registry-backed parsers during live `.sk` loading
  - `SecIf` now executes chained `if / else if / else` sections and preserves the post-chain continuation path
  - `SkriptParser` now supports minimal raw regex captures in registered syntax patterns like `if <.+>`
  - `SkriptParser` now preserves required whitespace around omitted inline optional groups and inline alternation branches for the currently verified natural-script compatibility surface
  - `SecIf` is now registered as a real `Section`, uses `parseResult.regexes` in `init(...)`, and no longer depends on a dedicated `Statement.parse(...)` fallback
  - `SecIf` now supports `parse if` and `else parse if`, evaluates them at parse time against `ContextlessEvent`, and skips body loading when the parse-time condition is false
  - `SecIf` now supports multiline `if any` / `if all`, `else if any` / `else if all`, `then`, and `then run`
  - `SecIf` now supports implicit conditional sections such as `%condition%:`
  - `Condition.parse(...)` now unwraps grouped outer parentheses so parenthesized `if (...)` forms match upstream behavior more closely
  - `SkriptParser` now forwards the minimal leading `implicit:` tag required by those registered conditional section patterns
  - `CondCompare` now keeps generic-object variable comparisons available to `if` conditions without stealing entity-specific condition syntax
  - parser regression coverage now explicitly rechecks the current natural-script forms `%objects% can be equipped on[to] entities`, `%objects% will lose durability when injured`, and `make %entities% not breedable`
  - `SecIfCompatibilityTest` now proves parse-time false branches skip child-effect initialization, while true branches and `else parse if` still load and execute correctly
  - real `.sk` GameTests now cover options replacement, mixed-case variable set/lookup, conditional chain paths, parenthesized conditional chains, parse-time conditional chains, multiline `if any` / `if all` plus `then`, implicit conditional sections, parse-time skipped-invalid-body paths, list-variable reindexing on `set {target::*} to {source::*}`, and plain-effect section ownership plus local-variable copy-back through `set {_component} to a blank equippable component:`
  - section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed locals instead of assuming typed runtime arrays
  - real expression `.sk` GameTests now also cover section-local propagation for custom damage source, potion effect, and loot context creation sections
  - `Skript.error(...)`, `Skript.warning(...)`, and `Skript.debug(...)` now flow through a parse-log-aware `SkriptLogger`
  - `ParseLogHandler` now retains specific parse errors across nested parser scopes instead of acting as an empty shim
  - `Statement.parse(...)` now stops on captured function/effect/condition parse errors and prints the retained diagnostic instead of falling through to a generic section fallback
  - `ScriptLoaderCompatibilityTest` now proves that a valid effect used as a section keeps its specific ownership error without also logging `Can't understand this section`
  - `Node.splitLine(...)` now strips inline comments, preserves quoted `#`, unescapes doubled `##`, and tracks `###` block comments for the active runtime parser
  - `SkriptRuntime.parseScript(...)` now uses that split logic so commented section headers, commented option entries, commented conditions/effects, and block-commented invalid syntax load correctly in live `.sk`
  - `NodeCompatibilityTest` now covers quoted-hash, doubled-hash, and block-comment line splitting
  - `SectionNodeCompatibilityTest` now covers case-insensitive mapped lookup, move-between-parent behavior, ordered replacement, removal, and `convertToEntries(...)` map synchronization
  - `StructureEntryValidatorCompatibilityTest` now proves that validator-backed structures receive runtime-shaped `EntryNode` values and defaulted optional entries through the compatibility registration surface
  - `ScriptLoaderCompatibilityTest` now also proves that `StructOptions` accepts runtime-style `EntryNode` trees and still logs invalid nested option lines without rejecting valid sibling entries
  - real base `.sk` GameTests now also cover comment-aware loader parsing through commented section headers, commented option entries, quoted hashes, and block-commented invalid syntax
  - `PatternCompiler` / `SkriptPattern` now provide the shared matcher path used by both direct pattern compilation and `SkriptParser`, including placeholders, raw regex captures, optional groups, alternation, general `tag:` metadata, and XOR parse marks via `¦`
  - `SkriptParser.ParseResult` now carries `mark` and receives general parse tags from matched branches instead of only the prior hardcoded leading `implicit:` case
  - bare leading `:` metadata now auto-derives from following literal and choice branches on the current compatibility surface
  - `PatternCompilerCompatibilityTest` and new parser regressions now cover parse marks, branch-specific parse tags, and preserved natural-form optional whitespace
  - `ParseLogHandler` now exposes backup/restore helpers and retained-error accessors so `ScriptLoader` can compare section and statement fallback diagnostics without printing both
  - `ScriptLoader.loadItems(...)` now restores the more specific retained diagnostic when section-node fallback tries both `Section.parse(...)` and `Statement.parse(...)`
  - `Statement.parse(...)` now rejects plain conditions used as section headers instead of silently returning body-less condition items
  - `ScriptLoader.loadItems(...)` now emits unreachable-code warnings behind `ScriptWarning.UNREACHABLE_CODE` suppression when a previously loaded statement stops further execution
  - `Variables` now uses upstream-style natural variable-name ordering for prefix/list iteration, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set` reindexing
  - `VariableCompatibilityTest` now covers both direct key exposure and parsed `set {target::*} to {source::*}` under that natural numeric ordering
  - real base `.sk` GameTests now also cover numeric list ordering through a dedicated fixture where `{source::2}` lands in `{target::1}` ahead of `{source::10}`
  - `Classes` now sorts class infos by assignable-type specificity plus explicit `before(...)` / `after(...)` dependencies instead of raw registration order
  - `ClassesCompatibilityTest` now covers most-specific superclass lookup and dependency ordering
  - real base `.sk` GameTests now also cover loader unreachable-code warnings and stop-trigger short-circuiting through `unreachable_code_warning_stop_test_block.sk`
  - shared literal-pattern matches now also follow that stable class-info ordering when multiple class infos register the same alias
  - `TriggerItem.walk(...)` now honors nested `ExecutionIntent.stopTrigger()` and `ExecutionIntent.stopSection()` results, while `TriggerSection` now surfaces nested stop-trigger intent back to `ScriptLoader` for unreachable-code warnings
  - `ScriptLoaderCompatibilityTest` now covers registered sections that stop the outer trigger versus sections that only stop their own body
- reran verification after the code slice:
  - `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --tests ch.njol.skript.config.SectionNodeCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.structures.StructureEntryValidatorCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests org.skriptlang.skript.bukkit.potion.elements.PotionEntityObjectCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocationCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContextCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
  - `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed
  - `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
  - `./gradlew runGameTest --rerun-tasks` passed with `197 / 197`
  - `./gradlew build --rerun-tasks` passed
  - build path executed the full Fabric GameTest task successfully with `197` scheduled tests
- upstream cross-check corrected two false blockers:
  - `TriggerSection.run(...)` throwing `UnsupportedOperationException` matches upstream intent
  - function-call wrapper `init(...)` stubs match upstream direct-wrapper behavior and are not current blockers

## Part 1A Definition

Target classes for the first implementation slice:

- `ch/njol/skript/lang/SkriptParser`
- `ch/njol/skript/lang/Statement`
- `ch/njol/skript/ScriptLoader`
- `ch/njol/skript/sections/SecIf`
- `ch/njol/skript/expressions/ExprInput`

Part goals:

- replace obvious minimal-stub paths with real behavior where the class is already present locally
- align parser and statement execution flow more closely to upstream intent before importing more syntax packages
- keep existing Fabric runtime behavior green while expanding core compatibility
- add regression coverage for every changed behavior path
- add real `.sk` plus Fabric GameTest coverage when the new behavior changes user-visible script semantics

Exit criteria:

- no targeted `Part 1A` class still advertises itself as a minimal compatibility stub
- targeted placeholder or `UnsupportedOperationException` execution paths are either removed or justified and documented
- relevant tests cover the new parser/runtime behavior
- porting docs are updated with exact changed scope and exact remaining scope

## Part 1A Working Notes

Current local observations after the landed slices above:

- `SecIf` now uses the registered section path with minimal raw regex captures, minimal leading `implicit:` tag forwarding, `parse if` / `else parse if`, multiline `if any` / `if all` plus `then`, and implicit conditional sections; the remaining gap is broader statement/log orchestration plus fuller upstream pattern element-graph parity.
- `ExprInput` is now a working compatibility expression for `input`, typed `%classinfo% input`, and `input index`.
- `SkriptParser` now keeps the currently verified inline optional/alternation natural-script forms green, routes matching through the shared `patterns` package, forwards general parse tags plus XOR marks, and derives the current bare leading `:` auto-tags again; broader upstream pattern element-graph parity is still open.
- `options:` support is now real, the local config layer now has the `SectionNode` map semantics and validator-backed entry handling this path needs, the active runtime parser strips inline comments and `###` block comments through `Node.splitLine(...)`, section-node fallback now restores the better section-versus-statement diagnostic, stopping statements now emit unreachable-code warnings behind script-level warning suppression, and nested section-contained stop-trigger intent now propagates through loader/runtime while `stopSection` stays local; broader config diagnostics and hint flow are still not upstream-close.
- variable expressions, case-insensitive storage, list-variable reindexing on plain list-to-list `set`, and natural numeric ordering for prefix/list iteration now work, but broader `Variables` and `Statement` behavior is still incomplete.
- plain effects with section-managing expressions now own their section node through `Effect.parse(...)`, nested local-variable updates now copy back through `Variables.withLocalVariables(...)`, valid effects used as sections now keep their specific ownership diagnostic, and plain conditions no longer silently masquerade as section headers; broader statement/log hint flow is still incomplete.
- quoted string literals in generic `%object%` contexts are now protected from registry-backed parser capture.
- `SkriptParser` now has the minimal raw-regex capture and leading `implicit:` tag support needed for registered conditional sections, but it still lacks upstream tag/mark/pattern features required by richer modern patterns.
- section-managed custom damage source, potion effect, and loot-context expressions now tolerate object-backed locals, but when a section expression swaps the current event type the outer event payload still needs to be captured into locals before entering the section body.
- broader `InputSource` usage paths are still limited because no richer upstream input-source syntax packages are imported yet.
- `SkriptParser`, `Statement`, `SecIf`, `Classes`, and related helpers still contain conservative `null` fallbacks and incomplete orchestration paths.

Targeted verification already completed in this slice:

- `./gradlew test --tests ch.njol.skript.config.NodeCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests org.skriptlang.skript.bukkit.potion.elements.PotionEntityObjectCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprLootContextLocationCompatibilityTest --tests org.skriptlang.skript.bukkit.loottables.elements.expressions.ExprSecCreateLootContextCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests '*ClassesCompatibilityTest' --tests '*FunctionCoreCompatibilityTest' --tests '*FunctionImplementationCompatibilityTest' --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.InputSourceCompatibilityTest --tests ch.njol.skript.sections.SecIfCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed
- `./gradlew runGameTest --rerun-tasks` passed with `197 / 197`
- `./gradlew build --rerun-tasks` passed, including the full Fabric GameTest path

Do not reopen these as blockers without a new upstream mismatch:

- `TriggerSection.run(...)` throwing `UnsupportedOperationException`
- `EffFunctionCall.init(...)` returning `false`
- `ExprFunctionCall.init(...)` returning `false`

Future slices should keep replacing this section with exact implemented changes and updated blockers.
