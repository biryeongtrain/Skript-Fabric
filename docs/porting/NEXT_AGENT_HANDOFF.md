# Next Agent Handoff

Last updated: 2026-03-09

## Read Order

1. [README.md](README.md)
2. this file
3. [PORTING_STATUS.md](PORTING_STATUS.md)
4. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
5. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) if running parallel workers
6. [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) if running parallel workers

## Current Headline

- latest verified runtime baseline: `230 / 230`
- latest full verification:
  - `./gradlew build --rerun-tasks` passed
- Stage 8 package-local audit remains frozen at `23 / 214`
- upstream `ch/njol/skript` snapshot: local `140 / 1189`, shortfall `1049`
- immediate priority: close upstream `ch/njol/skript` gaps first, then import exact missing user-visible syntax

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- `Classes.toString(..., StringMode.VARIABLE_NAME)` now prefixes parser-less fallback values as `object:...` like upstream
- `SkriptParser.parseStatic(...)` and `parseModern(...)` now reject blank trimmed input before optional patterns can match
- `DynamicFunctionReference.parseFunction(...)` now drops unresolved `from missing.sk` suffixes before global fallback
- `Statement.parse(...)`, `ParseLogHandler`, and default severe `LogEntry` quality now preserve a specific retained parse error over the generic `Can't understand this condition/effect: ...` fallback
- `Classes.clone(...)` now honors classinfo cloners instead of falling back to identity copies
- `FunctionReference.parse(...)` now unescapes doubled quotes inside quoted string literal arguments
- ordinary keyed function arguments now preserve their keyed metadata across `Parameter.newInstance(...)`
- keyed plural defaults now keep upstream behavior: single-value defaults zip, multi-value defaults stay unkeyed
- `DynamicFunctionReference.resolveFunction(...)` now preserves `from local.sk` for local dynamic references
- omitted placeholder defaults now ignore placeholders on inactive choice branches instead of treating them like omitted active placeholders
- invalid required omitted-placeholder defaults now retain the upstream-style default-expression parse error instead of failing silently
- statement fallback now keeps `EffectSection` parsing in statement mode when section-mode init rejects the body
- `ScriptLoader.loadItems(...)` now leaves `ParserInstance.getNode()` at the loaded section root instead of restoring an unrelated prior node
- `Classes.toString(...)` now routes legacy parser-backed values through parser stringification instead of raw `Object.toString()`
- legacy parser-backed debug strings now wrap as `[codename:debug text]`
- targeted regressions added:
  - [LegacyWrapperCompatibilityTest.java](../../src/test/java/ch/njol/skript/classes/LegacyWrapperCompatibilityTest.java)
  - [ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java)
  - [ScriptLoaderCompatibilityTest.java](../../src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java)
  - [SkriptParserBlankInputCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/parser/SkriptParserBlankInputCompatibilityTest.java)
  - [SkriptParserRegistryTest.java](../../src/test/java/ch/njol/skript/lang/SkriptParserRegistryTest.java)
  - [FunctionCallCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java)
  - [FunctionCoreCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java)
  - [TriggerItemCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/TriggerItemCompatibilityTest.java)

## Recent Closed Prereqs

These are already closed. Do not reopen without a new reproducer.

- legacy `parseStatic(...)` expression-placeholder flags
- explicit-literal-only `Classes.getPatternInfos(...)` candidate filtering
- case-sensitive classinfo lookup
- exact-type overload preference in `FunctionRegistry`
- required omitted-placeholder fail-fast parsing

## Next Targets

1. broader parser default-value and placeholder-omission parity beyond the now-closed exact classinfo-default, inactive-choice-placeholder, and invalid-default-diagnostic rules
2. broader classinfo/parser registry parity beyond the now-closed legacy parser stringification, classinfo-cloner, and variable-name fallback slices
3. deeper function runtime/default-parameter semantics beyond the now-closed explicit-empty-slot, direct-null-slot, keyed-metadata, keyed-default plural compatibility, doubled-quote literal, local dynamic-reference namespace, and missing-source normalization cases
4. `Statement` / `ScriptLoader` only if a new concrete reproducer appears beyond the now-closed effect-section statement-mode fallback, parser-node-root retention, and specific-error-over-fallback retention slices

## Parallel Defaults

- keep `Coordinator + 5 workers`
- worker reasoning default: `medium`
- use local upstream snapshot only
- one mismatch per lane
- no web
- worker docs stay minimal

## Lane Status Format

Lane files under `docs/porting/parallel/` should stay short:

1. scope
2. latest slice
3. verification
4. next lead
5. merge notes

## Verification

Latest targeted verification:

```bash
./gradlew test --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks
```

Full verification:

```bash
./gradlew build --rerun-tasks
```

## Main Worktree Notes

Keep unrelated dirty files untouched in `/Users/qf/IdeaProjects/Skript-Fabric-port`:

- `.codex/environments/environment-2.toml`
- `.codex/environments/environment.toml`
- `scripts/`
