# Codex Parallel Prompts

Last updated: 2026-03-09

Use these as compact prompts for the next parallel run.

## Common Worker Rules

Apply these to every lane:

- reasoning: `medium`
- compare only against `/tmp/skript-upstream-e6ec744-2` or `/tmp/upstream-skript`
- do not browse the web
- fix exactly one mismatch at most
- if no mergeable mismatch exists, do docs-only no-op
- update only your lane status file under `docs/porting/parallel/`
- commit only if code lands
- use conventional-style commit messages without lane prefixes:
  - `fix(parser): ...`
  - `fix(function): ...`
  - `fix(classes): ...`
  - `fix(loader): ...`
  - `docs(porting): ...`

## Coordinator Prompt

```text
You are the coordinator for a parallel Codex run on /Users/qf/IdeaProjects/Skript-Fabric-port.

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/PORTING_STATUS.md
4. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
5. docs/porting/CODEX_PARALLEL_WORKFLOW.md
6. docs/porting/CODEX_PARALLEL_PROMPTS.md

Run 5 workers at medium reasoning.
Use local upstream snapshots only.
Keep Stage 8 frozen at 23 / 214.
Priority is upstream ch/njol/skript closure first.

Worker merge order:
1. Lane C
2. Lane B
3. Lane D
4. Lane E
5. Lane A

Coordinator owns:
- merge and integration fixes
- canonical docs under docs/porting/*.md
- root pointer headline updates
- final verification:
  - ./gradlew build --rerun-tasks
- fast-forward main after green verification

Preserve unrelated dirty files in the main worktree.
```

## Lane A Prompt

```text
You are Lane A.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-a

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_A_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/ScriptLoader.java
- src/main/java/ch/njol/skript/log/**
- src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java

Target:
- one concrete upstream-backed Statement/ScriptLoader/log mismatch
- prefer retained diagnostics, section fallback, hint flow, or section ownership

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_A_STATUS.md, and use a conventional commit like `fix(loader): preserve ...`.
```

## Lane B Prompt

```text
You are Lane B.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-b

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_B_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- src/main/java/ch/njol/skript/patterns/**
- parser-facing tests

Target:
- one concrete upstream-backed parser mismatch
- prefer default-value / omission parity, static-vs-modern parity, or matcher edge cases

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_B_STATUS.md, and use a conventional commit like `fix(parser): restore ...`.
```

## Lane C Prompt

```text
You are Lane C.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-c

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_C_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/variables/**
- src/main/java/ch/njol/skript/registrations/Classes.java
- src/main/java/ch/njol/skript/config/**
- src/main/java/ch/njol/skript/structures/**
- matching tests

Target:
- one concrete upstream-backed Variables / Classes / config / structures mismatch
- prioritize classinfo/parser registry or deeper variable semantics

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_C_STATUS.md, and use a conventional commit like `fix(classes): align ...`.
```

## Lane D Prompt

```text
You are Lane D.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-d

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_D_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/lang/function/**
- src/main/java/ch/njol/skript/lang/DefaultExpression.java
- src/main/java/ch/njol/skript/lang/DefaultExpressionUtils.java
- src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java
- matching tests

Target:
- one concrete upstream-backed function runtime or default-parameter mismatch
- prefer overload resolution, namespace fallback, keyed/plural/default execution semantics

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_D_STATUS.md, and use a conventional commit like `fix(function): preserve ...`.
```

## Lane E Prompt

```text
You are Lane E.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-e

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_E_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/lang/InputSource.java
- src/main/java/ch/njol/skript/lang/parser/ParserInstance.java
- src/main/java/ch/njol/skript/expressions/ExprInput.java
- src/main/java/ch/njol/skript/lang/TriggerItem.java
- src/main/java/ch/njol/skript/lang/TriggerSection.java
- tightly matching tests

Target:
- one concrete upstream-backed bridge mismatch
- prefer InputSource, ParserInstance, ExprInput, or TriggerItem/TriggerSection behavior

If the fix crosses into parser/classes/statement ownership, stop and hand it back to the coordinator.
Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_E_STATUS.md, and use a conventional commit like `fix(runtime): catch ...`.
```
