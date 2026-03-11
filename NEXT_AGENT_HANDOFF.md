# Next Agent Handoff

Canonical document moved to [docs/porting/NEXT_AGENT_HANDOFF.md](docs/porting/NEXT_AGENT_HANDOFF.md).

Read this root file first if a prompt references it, then continue in the canonical handoff.

Current headline:

- keep Stage 8 package-local audit frozen at `23 / 214`
- do not start the next Bukkit package-local audit slice yet
- immediate priority is upstream `ch/njol/skript` closure first
- latest verified runtime baseline is targeted cycle JUnit plus `260 / 260` GameTests on 2026-03-12
- latest closed slice is the 2026-03-12d batch (`ExprTool`, `ExprWithFireResistance`, `ExprTimeState`, `ExprSlotIndex`, bootstrap/binding coverage, `PrivateItemEntityAccess` reflection cleanup); keep teleport/spawn cause work and fishing-hook `currentState` out until those targets are fixed
- Codex parallel-session docs:
  - `docs/porting/CODEX_PARALLEL_WORKFLOW.md`
  - `docs/porting/CODEX_PARALLEL_PROMPTS.md`
