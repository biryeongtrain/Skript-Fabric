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
- immediate priority: import exact upstream user-visible syntax on the existing Fabric-backed runtime, while keeping one lane on remaining `lang-core` mop-up

## Local Upstream Reference

Use local upstream sources only. Do not browse.

- `/tmp/skript-upstream-e6ec744-2`
- `/tmp/upstream-skript`

## Latest Closed Slice

- implementation phase is now active on top of the existing `lang-core` baseline
- exact upstream interaction-dimensions plural expressions now register on the live Fabric runtime
- exact upstream responsive/unresponsive interaction condition syntax now registers and is covered by focused runtime tests
- exact upstream `make %entities% adult` / `make %entities% baby` syntax now registers on the live Fabric runtime
- `ParserInstance` now restores the upstream parser-delay-state bridge for remaining `lang-core` compatibility callers
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

1. exact upstream `expressions` imports that already have a live Fabric backend and can be verified with focused runtime tests plus real `.sk` coverage where needed
2. exact upstream `conditions` imports on the same rule
3. exact upstream `effects` imports on the same rule
4. selective `events` imports only for existing Fabric-backed families with payload and cancellation semantics we can verify
5. keep one mop-up lane on remaining `lang-core` long-tail: parser omitted/default, function runtime/default-parameter, and new loader/statement reproducers only

## Parallel Defaults

- keep `Coordinator + 5 workers`
- worker reasoning default: `medium`
- use local upstream snapshot only
- one primary mismatch plus one fallback mismatch per lane
- no web
- worker docs stay minimal
- lane split for the current phase:
  - `Lane A`: expressions
  - `Lane B`: conditions
  - `Lane C`: effects
  - `Lane D`: events
  - `Lane E`: `lang-core` mop-up

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
