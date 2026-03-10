# Skript-Fabric Porting Status

Last updated: 2026-03-10

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
  - `./gradlew build --rerun-tasks` passed on 2026-03-10
  - build path executed `runGameTest` successfully on 2026-03-10
  - `230 / 230` scheduled Fabric GameTests completed without build failure
- Latest implementation batch:
  - latest verified inventory/container closure on 2026-03-10 adds 10 upstream classes:
    - `expressions`: `ExprChestInventory`, `ExprEnderChest`, `ExprInventory`, `ExprInventoryInfo`, `ExprInventorySlot`, `ExprItemsIn`, `ExprFirstEmptySlot`
    - `conditions`: `CondContains`, `CondItemInHand`, `CondIsWearing`
  - these additions are registered on the active Fabric runtime bootstrap and verified through targeted parser/unit coverage
  - the latest change keeps the existing `230 / 230` Fabric GameTest baseline while reducing the upstream core shortfall to `637 / 1189`
  - the measured shortfall is now `552`

## Priority Shift On 2026-03-08

Further Stage 8 package-local audit is temporarily deprioritized.
The immediate priority is now the broader upstream `ch/njol/skript` closure workstream itself.
Exact missing upstream syntax families remain important, but they no longer lead the worker plan.
Reduce the raw shortfall first by closing larger upstream package bundles.

Detailed tracking for this workstream lives in [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md).

## `ch/njol/skript` Inventory Snapshot

Baseline reference used for the new audit:

- upstream repository: `SkriptLang/Skript`
- branch intent: `master`
- snapshot commit captured locally on 2026-03-08: `e6ec744`

Measured source counts:

- upstream `src/main/java/ch/njol/skript`: `1189` Java files
- local `src/main/java/ch/njol/skript`: `637` Java files
- net missing local surface relative to that snapshot: `552` Java files

Top-level upstream packages missing locally entirely:

- `bukkitutil`
- `command`
- `hooks`
- `test`
- `timings`

Key local package counts versus upstream:

- `aliases`: local `3`, upstream `12`
- `lang`: local `86`, upstream `85`
- `expressions`: local `104`, upstream `391`
- `conditions`: local `105`, upstream `135`
- `classes`: local `21`, upstream `28`
- `util`: local `29`, upstream `57`
- `variables`: local `6`, upstream `11`
- `config`: local `20`, upstream `20`
- `registrations`: local `10`, upstream `10`
- `patterns`: local `14`, upstream `14`
- `log`: local `16`, upstream `17`
- `sections`: local `3`, upstream `10`
- `structures`: local `6`, upstream `10`
- `localization`: local `11`, upstream `11`
- `literals`: local `15`, upstream `16`
- `effects`: local `87`, upstream `123`
- `events`: local `37`, upstream `53`
- `entity`: local `37`, upstream `34`
- `doc`: local `14`, upstream `18`
- `update`: local `9`, upstream `10`

## Why `lang` Goes First

`ch/njol/skript/lang` is still the highest-leverage behavior slice:

- file-count parity is now closed: local `85`, upstream `85`
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

- user-visible upstream syntax import has now started:
  - base entity-state conditions now cover `%entities% are alive/dead`, `%entities% are silent`, and `%entities% are invulnerable/invincible`
  - base entity-control effects now cover `kill %entities%`, `silence %entities%`, `unsilence %entities%`, `make %entities% silent`, and `make %entities% (invulnerable|invincible|vulnerable)`
  - dedicated parser/bootstrap unit tests plus real `.sk` Fabric GameTests now verify both exact syntax registration and live runtime mutation paths for those forms
  - user-pattern class infos now honor regex-backed aliases again, so `%material%` and `%materials%` resolve through registered class info aliases instead of only codenames
  - exact upstream condition/effect forms now also cover `feed [the] %players% [by %-number% [beef[s]]]`, `%entities% (is|are) (burning|ignited|on fire)`, `%livingentities% (is|are) (invisible|visible)`, `%livingentities% (has|have) (ai|artificial intelligence)`, `%players% (is|are) sprinting`, `make %livingentities% invisible`, `make %livingentities% not visible`, `make %livingentities% visible`, `make %livingentities% not invisible`, and exact sprinting start/stop effect forms
  - dedicated parser/bootstrap unit tests plus real `.sk` Fabric GameTests now verify the newer feed, invisible/visible, burning, AI, and sprinting forms through the live resource-loader path
  - added the upstream entity glowing property expression (`[the] glowing of %entities%`, `%entities%'[s] glowing`) with a focused parser regression
- shortfall-focused helper-surface closure:
  - `ch/njol/skript/log` now restores the missing legacy handler stack through `LogHandler`, `BlockingLogHandler`, `FilteringLogHandler`, `CountingLogHandler`, and `RetainingLogHandler`, while wiring `ParseLogHandler` and `SkriptLogger` back through that compatibility surface
  - `ch/njol/skript/patterns/Keyword` now exists locally and `SkriptPattern` again applies the upstream-style keyword prefilter before the heavier matcher path
  - `ch/njol/skript/variables/TypeHints` now restores the legacy add/get/enter-scope/exit-scope/clear bridge on top of the active hint manager
  - `ch/njol/skript/classes/Parser`, `PatternedParser`, `Converter`, and `ch/njol/skript/registrations/Converters` now restore the missing wrapper and adapter surface expected by older parser/converter paths
- latest `lang` core closure batch:
  - `SectionNode` now refreshes mapped parent lookups when a child node key changes, so renamed nodes stay reachable through section lookup like upstream
  - compiled `SkriptPattern` matching now preserves placeholder whitespace instead of normalizing all inner spacing before capture
  - `FunctionRegistry` now keeps overload resolution ambiguous when different overloads only satisfy exact-type preference on different argument positions
  - `ParserInstance.setCurrentScript(...)` now clears transient parser bridge state on script switches so node/event/input-source data does not leak between scripts
  - retained severe parse-log fallback now uses semantic error quality instead of a generic quality bucket
  - `SkriptParser.parseStatic(...)` now matches legacy `SyntaxElementInfo` patterns with `ALL_FLAGS`, so expression-only placeholders such as `%~integer%` work again through the legacy parse path while placeholder-level masks still control literal versus expression acceptance
  - `Classes.getPatternInfos(...)` now only returns explicitly registered literal-pattern matches, so parser-backed class infos no longer appear as unparsed-literal candidates unless they also declare a literal pattern
  - `Classes.clone(...)` no longer reflectively clones arbitrary `Cloneable` values without an explicit classinfo cloner
  - `SkriptPattern` keyword prefiltering now runs on raw input before trim normalization, matching upstream leading/trailing whitespace behavior more closely
  - `TriggerItem.walk(...)` now rethrows non-`Exception` throwables while keeping `Exception` and `StackOverflowError` compatibility handling
  - `ScriptLoader.loadItems(...)` now validates skipped non-dispatch nodes before returning, so invalid config-only nodes still log the expected parse error
  - `Classes.parse(...)` now honors no-command converter contexts again through restored legacy `ParseContext` variants
  - `SkriptParser.validatePattern(...)` now restores plural placeholder normalization and upstream-style pipe-outside-group diagnostics
  - `FunctionReference.consign(...)` now keeps primitive arrays as scalar arguments instead of treating them like plural object-array payloads
  - `ParserInstance` now notifies registered parser-data bridges when current events are set or cleared
  - `SyntaxRegistryService.register(...)` now preserves `SyntaxInfo.priority()` ordering instead of using plain insertion order
  - `Classes.parseSimple(...)` now prefers registered class parsers over primitive fallback coercion
  - omitted optional alternation branches now require defaults for every omitted required placeholder
  - `ParserInstance.isRegistered(...)` now restores the upstream parser-data registration guard
  - explicit literal-pattern matches returned by `Classes.getPatternInfos(...)` now preserve upstream registration order instead of being re-sorted by class-info specificity/dependency order
  - `Classes.getClassInfo(...)` and `getClassInfoNoError(...)` are case-sensitive again, so registry-backed codename probes now match upstream instead of lowercasing arbitrary input
  - `FunctionRegistry` now prefers exact non-`Object` parameter matches over broader assignable overloads, so a literal `Integer` argument no longer makes an exact overload ambiguous with a wider `Number` branch
  - `Function.execute(Object[][])` now matches upstream keyed-default behavior again, so plural/keyed parameters zip omitted defaults only when the default produces a single value and leave multi-value defaults unkeyed
  - `Function.execute(Object[][])` now also rejects raw over-arity direct calls even for a single plural parameter, leaving argument condensation to the call path like upstream
  - `Parameter.parse(...)` now keeps commas inside quoted/default subexpressions and rejects duplicate parameter names under case-insensitive variable mode, while still falling back to the legacy built-in scalar type names in thin unit-test bootstraps
  - `TypePatternElement.getCombinations(true)` now preserves literal-only placeholders but collapses non-literal placeholders to `%*%` in clean pattern combinations
  - `Variables.setVariable("name::*", null, ...)` now deletes list descendants through the bridge path while preserving a direct parent value, matching upstream list deletion semantics more closely
  - `Classes.toString(Object[], ...)` now returns the upstream null sentinel for empty arrays instead of `""`
  - parser-scoped `DefaultValueData` now requires an exact type match, so omitted placeholders only consume defaults registered for their exact type instead of broader assignable defaults
  - `ParserInstance.isCurrentEvent(...)` now only accepts current-event subclasses for an expected type, matching upstream's one-way assignability rule
  - `TriggerItem.walk(...)` now catches runtime exceptions and returns `false`, matching the legacy bridge behavior instead of bubbling them out
  - `TriggerItem.walk(...)` now also catches `StackOverflowError` and returns `false`, matching upstream's fail-closed trigger bridge behavior
  - `Function.execute(Object[][])` now preserves an explicit empty argument slot for an ordinary optional parameter instead of replacing it with that parameter's default, while keeping the keyed-default special case intact
  - `Function.execute(Object[][])` now also lets direct `null` argument slots through the legacy `executeWithNulls` guard, while still rejecting empty-array slots on that path
  - `Parameter.newInstance(...)` now preserves keyed argument metadata for ordinary keyed parameters instead of re-zipping with numeric fallback keys
  - keyed plural defaults now still follow upstream semantics after that slice: single-value defaults zip to `KeyedValue[]`, while multi-value defaults remain unkeyed
  - `FunctionReference.parse(...)` now unescapes doubled quotes inside quoted function-call literal arguments
  - `DynamicFunctionReference.resolveFunction(...)` now preserves the source script for local references so stringified local forms still print `from local.sk`
  - `DynamicFunctionReference.parseFunction(...)` now drops unresolved `from missing.sk` suffixes before global fallback
  - `ScriptLoader.loadItems(...)` now leaves `ParserInstance.getNode()` anchored at the loaded section root after parsing
  - `ScriptLoader.loadItems(...)` now clears stale section `SEVERE` diagnostics when a later statement fallback succeeds on the same section line, so successful fallback does not replay an error from the failed section path
  - statement fallback now forces `EffectSection` parsing through statement mode when section-mode init rejects the body, preserving upstream effect-section fallback behavior
  - omitted placeholders now consume only exact classinfo defaults instead of broader superclass defaults
  - omitted placeholder default lookup now ignores placeholders on inactive alternation branches instead of forcing defaults or parse failure for expressions that were never selected
  - invalid required omitted-placeholder defaults now retain the upstream-style default-expression parse error instead of failing silently
  - `Classes.toString(...)` now uses registered legacy parser stringification for scalar and array values instead of falling back straight to raw `Object.toString()`
  - `Classes.toString(..., StringMode.VARIABLE_NAME)` now prefixes parser-less fallback values as `object:...` like upstream
  - `Classes.clone(...)` now respects registered classinfo cloners instead of returning the original object when no array clone path applies
  - legacy parser-backed debug stringification now wraps as `[codename:debug text]`
  - retained parse-failure selection now keeps earlier semantic parse errors over later lower-quality `NOT_AN_EXPRESSION` statement failures again
  - `SkriptParser.parseModern(...)` and `parseStatic(...)` now fail the whole pattern when a required placeholder is omitted through an optional branch and no parser or classinfo default exists, instead of constructing a `null` expression path that upstream rejects
  - `SkriptParser.parseModern(...)` and `parseStatic(...)` now also reject blank trimmed input before fully optional patterns can match
  - the current `Statement` / `ScriptLoader` / `Section` corpus was rerun in a separate lane audit and did not surface another mergeable mismatch in the green suite
  - focused regression coverage now locks the new registry, function, parser, config, and loader behaviors in `ClassesCompatibilityTest`, `UnparsedLiteralCompatibilityTest`, `FunctionOverloadDisambiguationTest`, `FunctionCoreCompatibilityTest`, `FunctionImplementationCompatibilityTest`, `FunctionCallCompatibilityTest`, `FunctionDefaultKeyedParameterCompatibilityTest`, `OmittedPlaceholderRequiredDefaultCompatibilityTest`, `SkriptParserBlankInputCompatibilityTest`, `SkriptParserStaticFlagsCompatibilityTest`, `SkriptParserRegistryTest`, `PatternCompilerCompatibilityTest`, `ParserCompatibilityDataAndStackTest`, `ParserInstanceCompatibilityTest`, `SectionNodeCompatibilityTest`, `TriggerItemCompatibilityTest`, and `ScriptLoaderCompatibilityTest`
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
  - `PatternElement` graph nodes now preserve grouped string/combinations parity through `toFullString()`, `getCombinations(...)`, and `getAllCombinations()`, and malformed grouped patterns now wrap through local `MalformedPatternException`
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
  - plain severe `LogEntry` construction now defaults to upstream-style `SEMANTIC_ERROR` quality again
  - `Statement.parse(...)` now stops on captured function/effect/condition parse errors and prints the retained diagnostic instead of falling through to a generic section fallback
  - `Statement.parse(...)` now also keeps failed effect/condition init diagnostics non-terminal until the plain registered-statement path has been tried, so a later same-pattern statement can still load while the best prior specific error is restored if nothing ultimately matches
  - `Statement.parse(...)` now also clears any inherited outer `Section.SectionContext` owner when parsing plain statements (`node == null`), so nested function/effect/condition arguments do not accidentally inherit an enclosing expression section
  - `Statement.parse(...)` no longer feeds the generic loader fallback text into the registered-statement parser path, and `ParseLogHandler.printError(...)` now prefers a retained specific severe error over that same generic fallback string
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
  - `parseSectionTriggerItem(...)` now opens a temporary non-section hint scope around `Section.parse(...)`, so failed section parsing clears temporary hints before statement fallback runs, while section lines that succeed through statement fallback keep the narrowed hint scope alive
  - `ScriptLoaderCompatibilityTest` now covers failed-section hint rollback, successful sibling propagation, stop-trigger scope freezing, and stop-section hint merging
- class/type registry closure:
  - `ClassInfo` and `Classes` now close the missing codename, literal-pattern, and supertype resolution behavior that the parser/runtime depends on
  - `Classes` now also computes stable class-info ordering that prefers narrower assignable types and honors `before(...)` / `after(...)` dependencies instead of using raw registration order
  - shared literal-pattern matches returned by `Classes.getPatternInfos(...)` now follow that same stable ordering when multiple class infos register the same alias
  - `Classes.getParser(...)` now also falls back through registered converters after direct parser lookup, so converter-backed parser owners can still satisfy requested class infos on the current compatibility surface
  - `Classes.parse(...)` now falls back through registered converters after direct parser lookup, so converter-backed source types can still satisfy requested class infos on the current compatibility surface
  - `Classes.parse(...)` now clears stale direct-parser failures before later parser or converter fallback success, so successful fallback no longer leaks earlier parser diagnostics
  - `Classes.getDefaultExpression(String)` and `Classes.getDefaultExpression(Class<T>)` now expose exact registered classinfo defaults through the upstream helper surface
- variable/runtime closure:
  - `SkriptParser` now recognizes `{...}` variable expressions directly
  - `SkriptParser` now also recognizes the upstream-prefixed forms `var {x}`, `variable {x}`, and `the variable {x}` on the current compatibility surface
  - `Variable.newInstance(...)` now consumes parse-time local variable type hints through `ParserInstance` / `HintManager`, narrows generic `%object%` locals when a concrete hint is known, and rejects incompatible typed local lookups with a diagnostic
  - `Variables` now defaults to case-insensitive storage/lookup with a compatibility switch for case-sensitive operation
  - `Variables.withLocalVariables(...)` now follows upstream copy-back semantics for nested section-event execution instead of restoring the previous target snapshot
  - `Variable` no longer recommends preserving source keys for list-to-list `set`, so keyed list sources are reindexed into numeric target slots instead of leaking source keys
  - prefix/list iteration now uses natural variable-name ordering, so numeric-like keys such as `2` and `10` no longer sort lexically during list reads or list-to-list `set` reindexing
  - raw list-variable reads through `Variables.getVariable("name::*", ...)` now reconstruct upstream-style nested maps, including `null` sentinel parent values when a direct parent and descendants coexist
  - list variables now expose the legacy loop aliases `index`, `var`, `variable`, and `value`, and their predicate checks again use upstream-style all-values `getAnd()` semantics
  - `EffChange` now forwards keyed deltas only when the source expression explicitly recommends keyed preservation
  - quoted string literals now remain string literals in generic `%object%` contexts instead of being consumed by registry-backed parsers during live script loading
- Patbox placeholder runtime closure:
  - `VariableString` now routes `StringMode.MESSAGE` through Patbox `TextPlaceholderAPI`, so exact `%namespace:path%` runtime placeholders resolve on live message/name paths when a Skript event context is active
  - `TriggerItem.walk(...)` now scopes the active event through `CurrentSkriptEvent`, which gives Patbox placeholder resolution access to the current server, world, player, entity, or command source without changing non-message string semantics
- classinfo-backed omitted-placeholder default closure:
  - `ClassInfo` now carries default expressions, and `SkriptParser.parseModern(...)` / `parseStatic(...)` now fall back from parser-scoped `DefaultValueData` to exact `ClassInfo` defaults for omitted non-optional placeholders
  - exact forms such as `default number [%number%]`, `default number`, and `default number 5` are now regression-covered through the registry-backed parser path
- variable-name validation closure:
  - `Variable.isValidVariableName(...)` now ignores `*` characters that appear inside paired `%...%` expression spans, so forms such as `result::%{source::*}%` and `result::%{source::*}%::*` are accepted again while invalid outer-asterisk forms stay rejected
  - real base `.sk` coverage now proves that a live script containing `set {result::%{source::*}%} to "ignored"` loads and executes
- built-in local hint-producer closure:
  - `EffChange.init(...)` now publishes parse-time local-variable hints when the exact built-in `set %object% to %object%` path successfully targets a hintable local variable
  - later sibling lines now see the built-in `set {_value} to 1` / `set {_value} to "text"` type flow through the same live loader path that the custom hint harnesses already exercised
- section-expression object-safe closure:
  - custom damage source, potion effect, and loot-context section expressions now tolerate object-backed local values instead of assuming typed runtime arrays
  - real expression `.sk` coverage now includes section-local propagation for custom damage source, potion effect, and loot context creation sections
- comment-aware runtime script parsing closure:
  - `Node.splitLine(...)` now strips inline comments, preserves quoted `#`, unescapes doubled `##`, and tracks `###` block comments
  - `SkriptRuntime.parseScript(...)` now uses that split logic, so trailing comments on section headers, option entries, conditions, and effects no longer break live `.sk` loading
  - real base `.sk` coverage now includes a comment-aware loader fixture with commented section headers, commented option entries, quoted hashes, and block-commented invalid syntax
- GameTest isolation closure:
  - locked runtime GameTests now clear Skript variables before and after each body through `Variables.clearAll()`, which prevents real `.sk` runs from leaking globals across suite order while leaving production variable semantics unchanged
- statement-fallback diagnostic closure:
  - `Statement.selectRetainedFailure(...)` now keeps an earlier higher-quality effect or condition parse error when a later plain statement fails with a lower-quality specific diagnostic on the same syntax line
  - real base `.sk` coverage now includes `higher_quality_parse_error_prefers_effect_test_block.sk`, which proves the retained loader diagnostic stays on the earlier higher-quality effect failure
- real `.sk` coverage added in the base GameTest slice:
  - options replacement path
  - comment-aware loader parsing path
  - mixed-case variable set/lookup path
  - variable-name expressions with inner list markers such as `set {result::%{source::*}%} to "ignored"`
  - built-in local hint flow through `set {_value} to 1`, `capture hinted integer {_value}`, `set {_value} to "gold_block"`, and `capture hinted text {_value}`
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
  - statement-fallback section-line hint propagation through `statement_fallback_section_hint_test_block.sk`
  - Patbox placeholder resolution through `%player:name%` in a live entity-name mutation fixture

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
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionOverloadDisambiguationTest --tests ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest --rerun-tasks` passed after closing explicit literal registration-order parity plus keyed default-parameter execution semantics
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing section-level execution-intent propagation
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after closing parse-time local variable type hints
- `./gradlew test --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after adding lightweight pattern element graph APIs
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after closing loader hint-scope lifecycle
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --rerun-tasks` passed after closing converter-backed parser helper fallback plus parser-owned default-value backfill
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest.loadItemsKeepsSpecificSectionOwnershipErrorForSetTrueSyntax --rerun-tasks` passed after locking the exact `set {_var} to true:` ownership regression
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging parse-log-aware `Classes.parse(...)`, ordered duplicate parser tags, and statement fallback after failed effect/condition init
- `./gradlew test --tests ch.njol.skript.lang.VariableStringCompatibilityTest --rerun-tasks` passed after routing message-mode runtime placeholders through Patbox `TextPlaceholderAPI`
- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after revalidating the variable runtime around the GameTest isolation harness change
- `./gradlew test --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging raw list-variable map reads, prefixed variable expression parsing, and higher-quality statement fallback diagnostics
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after merging classinfo-backed omitted defaults, inner-expression variable-name list-marker validation, and built-in `EffChange` local hints
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.function.FunctionOverloadDisambiguationTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --tests ch.njol.skript.lang.parser.OmittedPlaceholderRequiredDefaultCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after restoring case-sensitive classinfo lookup, exact-type overload preference, and fail-fast omitted-placeholder parsing
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.FeedSyntaxTest --tests org.skriptlang.skript.fabric.runtime.InvisibleSyntaxTest --rerun-tasks` passed after restoring `%material%` alias lookup plus exact upstream `feed` and invisible/visible effect forms
- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.InvisibleSyntaxTest --tests org.skriptlang.skript.fabric.runtime.BurningSyntaxTest --tests org.skriptlang.skript.fabric.runtime.AISyntaxTest --tests org.skriptlang.skript.fabric.runtime.SprintingSyntaxTest --rerun-tasks` passed after merging exact upstream invisible/visible condition, burning, AI, and sprinting condition/effect syntax
- `./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after restoring `Classes` default-expression lookup helpers, direct-parent variable null sentinels, pattern graph string/combinations parity, and statement-fallback section-line hint retention
- `./gradlew test --tests ch.njol.skript.classes.LegacyWrapperCompatibilityTest --tests ch.njol.skript.variables.TypeHintsCompatibilityTest --tests ch.njol.skript.variables.VariablesCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.log.LogHandlerCompatibilityTest --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks` passed after restoring the legacy log-handler stack, `patterns.Keyword`, `variables.TypeHints`, and parser/converter wrapper surfaces
- `./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --tests ch.njol.skript.lang.VariableCompatibilityTest --rerun-tasks` passed after restoring local function-first registry fallback, section-scope hint clearing, effect-candidate ownership reset, and retained section diagnostics on statement fallback success
- `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
- `./gradlew build --rerun-tasks` passed, including the full Fabric GameTest path and `230 / 230` scheduled Fabric GameTests

## Foundation Already Landed Before This Pivot

The repository is not starting from zero.
The following foundations were already built before this priority shift:

- compatibility-oriented `ch.njol.skript.lang` class surface, including expressions, statements, triggers, sections, parser helpers, simplification helpers, variable strings, and function namespaces
- parser and syntax registry bridge work, including modern and legacy pattern matching, typed placeholder parsing, section-aware statement parsing, and parser-stack tracking
- function compatibility scaffolding, including signatures, registries, dynamic references, expression/effect call wrappers, and namespace fallback behavior
- variable and literal compatibility primitives, including `Variable`, `Variables`, `LiteralString`, `UnparsedLiteral`, `InputSource`, and section-expression helpers
- foundational utility scaffolding in `classes`, `config`, `log`, `patterns`, `registrations`, `util`, and `variables`
- active Fabric runtime harness and Fabric GameTest suite with `220 / 220` passing tests on the last code-verification run
- Stage 8 parity-audited package-local Bukkit slice for `breeding`, `input`, and `interactions`

## Current Gaps

- most upstream `ch/njol/skript` packages are still absent or only minimally scaffolded locally
- several present core classes still contain behaviorally incomplete paths, placeholder returns, or minimal-stub contracts
- upstream comparison showed that some earlier suspected gaps were false positives and should not be reopened:
  - `TriggerSection.run(...)` throwing `UnsupportedOperationException` matches upstream behavior
  - `EffFunctionCall.init(...)` and `ExprFunctionCall.init(...)` returning `false` on direct wrapper instances also match upstream behavior
- current Stage 8 package-local audit for `org/skriptlang/skript/bukkit` remains valid, but it is no longer the only gating audit track
- missing-user-visible syntax import is now active, and follow-up slices for `%material%`, `feed`, invisible/visible condition plus effect forms, burning/on-fire forms, AI forms, and sprinting condition/effect forms are landed; most upstream condition/effect/expression families are still absent from the active Fabric registration set
- `Part 1A` and `Part 1B` remain active as enabling workstreams, and `Part 2` syntax import is now active too, but most parser, statement, loader, variable, type-system, and user-visible syntax closure work remains open
- generic registered section loading is now closed in `ScriptLoader`, `ScriptLoader` now also restores the more specific section-versus-statement fallback diagnostic, preserves retained non-default section diagnostics on successful statement fallback, propagates nested section-contained stop-trigger intent through loader/runtime, warns about unreachable code behind script-level warning suppression, plain conditions no longer masquerade as section headers, and now carries a first upstream-style hint-scope lifecycle; broader loader/config hint flow and built-in hint producers are still incomplete
- validator-backed recursive `options:` loading for runtime `EntryNode` trees and raw simple-entry trees is now closed, but broader structure/config validation behavior is still much thinner than upstream
- the parser no longer regresses the currently verified natural-script inline optional/alternation forms, now forwards general tags/XOR marks through the shared matcher, derives the current bare leading `:` auto-tags again, no longer fails on omitted optional/alternation raw-regex captures, now exposes a lightweight `PatternElement` graph introspection API, now resolves compatible omitted-placeholder parser/classinfo defaults, now restores upstream-style bare string literal fallback through `InputSource`, now rejects blank trimmed input before fully optional patterns can match, and now keeps clean placeholder combinations aligned with upstream `%*%` collapsing rules; broader upstream pattern element-graph/runtime parity is still incomplete
- function runtime now closes dynamic validation-cache discrimination, script-local-before-global registry lookup, parsed default expressions for function parameters, upstream-style parameter-list splitting/duplicate-name rejection in `Parameter.parse(...)`, direct over-arity rejection in `Function.execute(...)`, and missing-source normalization in parsed dynamic function references, but broader namespace/default-parameter parity is still incomplete
- natural numeric ordering for list/prefix iteration, legacy list-variable loop aliases, all-values list-check semantics, parse-time local variable type hints, section-only hint-scope clearing, list deletion through `Variables.setVariable("name::*", null, ...)`, and parser-less `VARIABLE_NAME` fallback prefixing in `Classes.toString(...)` are now closed, but broader `Variables` and class-registry runtime semantics are still incomplete

## Active Workstreams

1. Maintain the current verified Fabric runtime and Stage 8 records without overstating parity.
2. Import missing upstream user-visible syntax families first, starting with exact condition/effect/expression forms that the current parser/runtime can already support end-to-end.
3. Continue the upstream `ch/njol/skript` closure workstream where it directly unblocks the next missing syntax family, especially through `Part 1A` and `Part 1B`.
4. After the missing syntax surface is materially landed, resume the broader Bukkit-behavior parity drive until user-visible behavior matches upstream as closely as it can be verified.

## Documentation Policy

For every future slice in the new workstream:

1. compare the target package or class role against the upstream baseline
2. record exact local coverage and exact missing behavior
3. add or tighten real `.sk` plus Fabric GameTest coverage when user-visible behavior changes
4. update this status file, [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md), and [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md) in the same turn
