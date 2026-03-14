# Porting Status

Canonical document moved to [docs/porting/PORTING_STATUS.md](docs/porting/PORTING_STATUS.md).

Current headline:

- Stage 5 event backend closure: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- latest verified runtime baseline: targeted cycle JUnit suite and `340 / 340` scheduled Fabric GameTests passed on 2026-03-14
- immediate priority: upstream `ch/njol/skript` closure first (`973 / 1189` exact-path present versus upstream snapshot `e6ec744`)
- latest closed core slice: 2026-03-14 command system stabilized — permission checks use LuckPerms/string-based verification, cooldowns persist across reloads, `ExprCommandInfo` returns ScriptCommand metadata, `ExprCmdCooldownInfo` landed; remaining 41 missing expressions need deep Bukkit infra (ClassInfo/event-value bindings)
- Codex parallel-session runbook: `docs/porting/CODEX_PARALLEL_WORKFLOW.md`
