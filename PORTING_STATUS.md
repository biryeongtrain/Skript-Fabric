# Porting Status

Canonical document moved to [docs/porting/PORTING_STATUS.md](docs/porting/PORTING_STATUS.md).

Current headline:

- Stage 5 event backend closure: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- latest verified runtime baseline: `230 / 230` scheduled Fabric GameTests and `./gradlew build --rerun-tasks` passed on 2026-03-09
- new immediate priority: upstream `ch/njol/skript` closure first (`140 / 1189` local versus upstream snapshot `e6ec744`), then exact missing user-visible syntax import on top of that closure
- latest closed core slice: worker-merged classes/loader/parser/runtime/function parity for invalid omitted-placeholder default diagnostics, parser-node root retention, local dynamic-reference namespace, legacy debug string wrapping, inactive-choice omitted-placeholder defaults, classinfo cloners, doubled-quote function literals, keyed argument metadata, keyed-default plural compatibility, statement-mode effect-section fallback, legacy parser stringification, stale section errors, exact omitted defaults, trigger fail-closed overflow handling, and direct null function slots
- Codex parallel-session runbook: `docs/porting/CODEX_PARALLEL_WORKFLOW.md`
