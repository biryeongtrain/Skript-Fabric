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

- `HintManager` now restores the upstream snapshot/rollback API through `backup()` and `restore(...)`, so parse-time local hint mutations can roll back cleanly
- local `DynamicFunctionReference` instances that only retain a source namespace string now also invalidate when `Functions.clearFunctions(...)` removes that namespace entry
- previously closed parser/classinfo baseline still stands: exact classinfo-parser lookup, tagged-branch suffix omitted-default enforcement, parser section-slice helpers, and the temporary scalar `Classes.parseSimple(...)` fallback after registered parsers
- verification: `./gradlew build --rerun-tasks`

## Recent Closed Prereqs

These are already closed. Do not reopen without a new reproducer.

- legacy `parseStatic(...)` expression-placeholder flags
- explicit-literal-only `Classes.getPatternInfos(...)` candidate filtering
- case-sensitive classinfo lookup
- exact-type overload preference in `FunctionRegistry`
- split exact-overload ambiguity retention
- required omitted-placeholder fail-fast parsing

## Next Targets

1. broader parser default-value and placeholder-omission parity beyond the now-closed exact classinfo-default, inactive-choice-placeholder, invalid-default-diagnostic, mixed-default-diagnostic, and tagged-branch suffix-default rules
2. broader classinfo/parser registry parity beyond the now-closed legacy parser stringification, object-array stringification, exact-parser lookup, empty-option-value handling, specific-parser precedence, classinfo-cloner, variable-name fallback, renamed-node map-sync, and transitive priority-ordering slices
3. deeper function runtime/default-parameter semantics beyond the now-closed explicit-empty-slot, direct-null-slot, keyed-metadata, keyed-default plural compatibility, one-based plural local indexing, doubled-quote literal, blank-default rejection, lazy global-reference execution, local dynamic-reference namespace, missing-source normalization, split-exact-overload ambiguity cases, tracked-script unload, and untracked local-reference unload for dynamic function references
4. `Statement` / `ScriptLoader` only if a new concrete reproducer appears beyond the now-closed effect-section statement-mode fallback, parentless-root node normalization, whitespace-only-line diagnostics, config-only-node skip behavior, stale-section-warning drop, specific-error-over-fallback retention, semantic fallback-quality slices, parser-fallback inflation slice, and retained parse-error tie handling

## Parallel Defaults

- keep `Coordinator + 5 workers`
- worker reasoning default: `medium`
- use local upstream snapshot only
- one primary mismatch plus one fallback mismatch per lane
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

Optional targeted verification while narrowing a lane:

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
