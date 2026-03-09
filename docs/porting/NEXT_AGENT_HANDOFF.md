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
  - `./gradlew runGameTest --rerun-tasks` passed
  - `./gradlew build --rerun-tasks` passed
- Stage 8 package-local audit remains frozen at `23 / 214`
- upstream `ch/njol/skript` snapshot: local `140 / 1189`, shortfall `1049`
- immediate priority: close upstream `ch/njol/skript` gaps first, then import exact missing user-visible syntax

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- `Classes.getPatternInfos(...)` now keeps explicit literal-pattern matches in upstream registration order
- `Function.execute(Object[][])` now matches upstream keyed-default behavior:
  - single omitted default for keyed/plural param -> zip to `KeyedValue[]`
  - multi-value omitted default for keyed/plural param -> keep unkeyed
- targeted regressions added:
  - [ClassesCompatibilityTest.java](../../src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java)
  - [FunctionDefaultKeyedParameterCompatibilityTest.java](../../src/test/java/ch/njol/skript/lang/function/FunctionDefaultKeyedParameterCompatibilityTest.java)

## Recent Closed Prereqs

These are already closed. Do not reopen without a new reproducer.

- legacy `parseStatic(...)` expression-placeholder flags
- explicit-literal-only `Classes.getPatternInfos(...)` candidate filtering
- case-sensitive classinfo lookup
- exact-type overload preference in `FunctionRegistry`
- required omitted-placeholder fail-fast parsing

## Next Targets

1. broader parser default-value and placeholder-omission parity
2. broader classinfo/parser registry parity
3. deeper function runtime/default-parameter semantics
4. `Statement` / `ScriptLoader` only if a new concrete reproducer appears

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
./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionOverloadDisambiguationTest --tests ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest --rerun-tasks
./gradlew test --tests ch.njol.skript.registrations.ClassesCompatibilityTest --tests ch.njol.skript.lang.UnparsedLiteralCompatibilityTest --tests ch.njol.skript.lang.parser.SkriptParserStaticFlagsCompatibilityTest --tests ch.njol.skript.lang.SkriptParserRegistryTest --tests ch.njol.skript.patterns.PatternCompilerCompatibilityTest --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks
```

Full verification:

```bash
./gradlew runGameTest --rerun-tasks
./gradlew build --rerun-tasks
```

## Main Worktree Notes

Keep unrelated dirty files untouched in `/Users/qf/IdeaProjects/Skript-Fabric-port`:

- `.codex/environments/environment-2.toml`
- `.codex/environments/environment.toml`
- `scripts/`
