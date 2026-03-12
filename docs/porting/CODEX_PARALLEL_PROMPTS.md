# Codex Parallel Prompts

Last condensed: 2026-03-11

Use one prompt per worktree. Keep prompts short and rely on canonical docs for context.

## Coordinator

```text
You are the coordinator for a parallel Codex run on /Users/qf/.codex/worktrees/c842/Skript-Fabric-port.

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md

Own merges, canonical docs, and final verification only.
Keep Stage 8 counts frozen unless explicitly reassigned.
Merge lanes in this order unless conflicts force otherwise: Lane C, Lane B, Lane A.
Reject any syntax slice that lacks a real `.sk` GameTest.
Reject any event hook slice whose only proof is direct `dispatch(...)` of the target compat handle.
After merge run:
- ./gradlew runGameTest --rerun-tasks
- ./gradlew build --rerun-tasks
Then refresh canonical docs with exact counts and results.
```

## Lane A

```text
You are Lane A in /Users/qf/IdeaProjects/Skript-Fabric-port-lane-a.

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_A_STATUS.md

Stay inside Statement / ScriptLoader / log compatibility.
Do not edit canonical docs.
If you land new syntax, add a real `.sk` GameTest that proves parse plus runtime execution.
If you touch event hooks, validate them through a real game-state or callback trigger, not by dispatching the target compat handle directly.
Record exact commands and results only in LANE_A_STATUS.md.
```

## Lane B

```text
You are Lane B in /Users/qf/IdeaProjects/Skript-Fabric-port-lane-b.

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_B_STATUS.md

Stay inside SkriptParser / parser-default-value / parser-facing regression work.
Do not edit canonical docs.
If you land new syntax, add a real `.sk` GameTest that proves parse plus runtime execution.
If you touch event hooks, validate them through a real game-state or callback trigger, not by dispatching the target compat handle directly.
Record exact commands and results only in LANE_B_STATUS.md.
```

## Lane C

```text
You are Lane C in /Users/qf/IdeaProjects/Skript-Fabric-port-lane-c.

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_C_STATUS.md

Stay inside variables/classes/config/structures and adjacent function-runtime dependency closure.
Do not edit canonical docs.
If you land new syntax, add a real `.sk` GameTest that proves parse plus runtime execution.
If you touch event hooks, validate them through a real game-state or callback trigger, not by dispatching the target compat handle directly.
Record exact commands and results only in LANE_C_STATUS.md.
```
