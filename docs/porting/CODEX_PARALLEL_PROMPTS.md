# Codex Parallel Prompts

Last updated: 2026-03-10

Use these as compact prompts for the next parallel run.

## Common Worker Rules

Apply these to every lane:

- reasoning: `medium`
- compare only against `/tmp/skript-upstream-e6ec744-2` or `/tmp/upstream-skript`
- do not browse the web
- work in a package bundle, not one tiny syntax slice
- use one primary bundle and one fallback bundle inside the lane's ownership
- if both still leave owned work open, continue into the next same-scope sub-bundle before stopping
- land as many upstream-backed classes or closures as the lane can verify without leaving its ownership area
- do not stop after the first small win; aim for at least `20` class-equivalent additions/restorations and preferably roughly `20-60`, or `2-4` verifiable commits unless the owned bundle is clearly blocked or exhausted
- if primary, fallback, and one more same-scope sub-bundle produce no mergeable work, do a short no-op lane update
- update only your lane status file under `docs/porting/parallel/`
- commit as many times as needed if code lands
- parser/unit coverage is enough for import-only syntax, but if you make syntax active in the runtime instead of leaving it import-only, you must also add representative real `.sk` GameTest coverage in the same batch
- use conventional-style commit messages without lane prefixes:
  - `fix(parser): ...`
  - `fix(function): ...`
  - `fix(classes): ...`
  - `fix(loader): ...`
  - `feat(expressions): ...`
  - `feat(conditions): ...`
  - `feat(effects): ...`
  - `feat(events): ...`
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

Run 6 workers at medium reasoning.
Use local upstream snapshots only.
Keep Stage 8 frozen at 23 / 214.
Priority is reducing the raw `ch/njol/skript` shortfall by closing upstream package bundles.
Do not spend worker time polishing already-landed syntax unless that directly blocks new upstream imports.
When running a syntax-heavy mixed-runtime cycle, assign at least `100` syntax-class or live-activation targets across the six workers if that much plausible surface remains.

Worker merge order:
1. Lane A
2. Lane B
3. Lane C
4. Lane D
5. Lane E
6. Lane F

Coordinator owns:
- merge and integration fixes
- canonical docs under docs/porting/*.md
- root pointer headline updates
- the batch-close gate for runtime activation:
  - if newly active runtime syntax does not have representative real `.sk` GameTest coverage, add that coverage in the coordinator pass or leave the syntax import-only
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
- src/main/java/ch/njol/skript/classes/**
- src/main/java/ch/njol/skript/registrations/**
- src/main/java/ch/njol/skript/patterns/**
- tightly matching tests

Target:
- primary bundle: close more of `classes` + `registrations` + `patterns`
- fallback bundle: continue with another upstream-backed missing class cluster in the same scope
- import multiple upstream classes if they belong to the same bundle and stay verifiable

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_A_STATUS.md, and use conventional commits like `fix(classes): ...` or `fix(patterns): ...`.
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
- src/main/java/ch/njol/skript/config/**
- src/main/java/ch/njol/skript/util/**
- src/main/java/ch/njol/skript/localization/**
- tightly matching tests

Target:
- primary bundle: close more of `config` + `util` + `localization`
- fallback bundle: continue with another upstream-backed missing class cluster in the same scope
- import multiple upstream classes if they belong to the same bundle and stay verifiable

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_B_STATUS.md, and use conventional commits like `fix(config): ...` or `fix(util): ...`.
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
- src/main/java/ch/njol/skript/sections/**
- src/main/java/ch/njol/skript/structures/**
- src/main/java/ch/njol/skript/aliases/**
- src/main/java/ch/njol/skript/literals/**
- tightly matching tests

Target:
- primary bundle: close more of `variables` + `sections` + `structures`
- fallback bundle: continue with `aliases` or `literals` imports in the same lane
- import multiple upstream classes if they belong to the same bundle and stay verifiable

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_C_STATUS.md, and use conventional commits like `fix(variables): ...` or `feat(literals): ...`.
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
- src/main/java/ch/njol/skript/lang/**
- src/main/java/ch/njol/skript/log/**
- tightly matching tests

Target:
- primary bundle: close remaining `lang` blocker imports
- fallback bundle: close `log` or function/parser support classes that unblock broader imports
- import multiple upstream classes if they belong to the same bundle and stay verifiable

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_D_STATUS.md, and use conventional commits like `fix(parser): ...` or `fix(log): ...`.
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
- src/main/java/ch/njol/skript/expressions/**
- src/main/java/ch/njol/skript/conditions/**
- tightly matching tests

Target:
- primary bundle: import larger upstream-backed `expressions` class clusters
- fallback bundle: continue with larger upstream-backed `conditions` class clusters
- if both are still moving, keep going into the next same-scope sub-bundle before stopping
- prefer shared bases, abstract helpers, and common runtime glue before leaf syntax classes

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_E_STATUS.md, and use conventional commits like `feat(expressions): ...` or `feat(conditions): ...`.
```

## Lane F Prompt

```text
You are Lane F.
Worktree: /Users/qf/IdeaProjects/Skript-Fabric-port-lane-f

Read:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_F_STATUS.md

Scope only:
- src/main/java/ch/njol/skript/effects/**
- src/main/java/ch/njol/skript/events/**
- src/main/java/ch/njol/skript/entity/**
- tightly matching tests

Target:
- primary bundle: import larger upstream-backed `effects` class clusters
- fallback bundle: continue with larger upstream-backed `events` or `entity` class clusters
- if both are still moving, keep going into the next same-scope sub-bundle before stopping
- prefer shared bases, abstract helpers, event scaffolding, and common runtime glue before leaf syntax classes

Do not edit canonical docs or files owned by other lanes.
If code lands, add the narrowest matching regressions, record exact commands/results in LANE_F_STATUS.md, and use conventional commits like `feat(effects): ...`, `feat(events): ...`, or `feat(entity): ...`.
```
