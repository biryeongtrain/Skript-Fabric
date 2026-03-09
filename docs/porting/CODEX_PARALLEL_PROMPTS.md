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
Priority is exact upstream user-visible syntax import on the existing Fabric-backed runtime.
Keep one lane on lang-core mop-up.

Worker merge order:
1. Lane D
2. Lane A
3. Lane B
4. Lane C
5. Lane E

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
- src/main/java/org/skriptlang/skript/**/expressions/**
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java
- matching expression runtime tests and .sk resources

Target:
- one exact upstream expression import backed by the current Fabric runtime
- primary: expression family with existing event/entity/backend data
- fallback: another exact expression family in the same owned files

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_A_STATUS.md, and use a conventional commit like `feat(expression): import ...`.
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
- src/main/java/org/skriptlang/skript/**/conditions/**
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java
- matching condition runtime tests and .sk resources

Target:
- one exact upstream condition import backed by the current Fabric runtime
- primary: condition family with existing backend state access
- fallback: another exact condition family in the same owned files

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_B_STATUS.md, and use a conventional commit like `feat(condition): import ...`.
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
- src/main/java/org/skriptlang/skript/**/effects/**
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java
- matching effect runtime tests and .sk resources

Target:
- one exact upstream effect import backed by the current Fabric runtime
- primary: effect family with existing mutable runtime target
- fallback: another exact effect family in the same owned files

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_C_STATUS.md, and use a conventional commit like `feat(effect): import ...`.
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
- src/main/java/org/skriptlang/skript/fabric/syntax/event/**
- tightly coupled event payload expressions and event registration files
- src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java
- matching event runtime tests and .sk resources

Target:
- one exact upstream event import for an already Fabric-backed event family
- primary: event family with verifiable payload semantics
- fallback: tightly coupled event payload adapter needed by that family

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_D_STATUS.md, and use a conventional commit like `feat(event): import ...`.
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
- src/main/java/ch/njol/skript/lang/**
- src/main/java/ch/njol/skript/log/**
- src/main/java/ch/njol/skript/variables/**
- src/main/java/ch/njol/skript/registrations/Classes.java
- tightly matching tests

Target:
- one concrete upstream-backed lang-core or parser/runtime bridge mismatch
- primary: parser omitted/default, function runtime long-tail, or loader/statement reproducer
- fallback: bridge mismatch in InputSource, ParserInstance, ExprInput, TriggerItem, or classes/variables parity

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest regression, record exact commands/results in LANE_E_STATUS.md, and use a conventional commit like `fix(parser): restore ...`.
```
