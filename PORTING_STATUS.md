# Porting Status

Canonical document moved to [docs/porting/PORTING_STATUS.md](docs/porting/PORTING_STATUS.md).

Current headline:

- Stage 5 event backend closure: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- latest verified runtime baseline: targeted cycle JUnit suite and `260 / 260` scheduled Fabric GameTests passed on 2026-03-12
- immediate priority: upstream `ch/njol/skript` closure first (`907 / 1189` exact-path present versus upstream snapshot `e6ec744`)
- latest closed core slice: 2026-03-12 cycle landed `ExprTool`, `ExprWithFireResistance`, `ExprTimeState`, `ExprSlotIndex`, bootstrap/binding coverage for those expressions, and the remaining `PrivateItemEntityAccess` reflection cleanup; teleport/spawn cause hooks and fishing-hook `currentState` stayed out
- Codex parallel-session runbook: `docs/porting/CODEX_PARALLEL_WORKFLOW.md`
