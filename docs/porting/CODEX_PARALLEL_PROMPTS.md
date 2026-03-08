# Codex Parallel Prompts

Last updated: 2026-03-09

These prompts are designed for the next Codex app session.
Use one prompt per worktree.
Do not run worker prompts inside the same working tree.

## Coordinator Prompt

```text
You are the coordinator for a parallel Codex run on /Users/qf/IdeaProjects/Skript-Fabric-port.

Before doing anything:
1. Read docs/porting/README.md
2. Read docs/porting/NEXT_AGENT_HANDOFF.md
3. Read docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. Read docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. Read docs/porting/CODEX_PARALLEL_PROMPTS.md

Your role:
- assign workers to Lane A, Lane B, Lane C, Lane D, and Lane E worktrees
- enforce the file-ownership matrix from CODEX_PARALLEL_WORKFLOW.md
- do not let worker lanes edit canonical docs under docs/porting/*.md
- merge lane branches in this order unless conflicts force a different order: Lane C, Lane B, Lane D, Lane E, Lane A
- apply only integration fixes in the coordinator worktree
- rerun final verification after merge:
  - ./gradlew runGameTest --rerun-tasks
  - ./gradlew build --rerun-tasks
- update canonical docs after integration:
  - docs/porting/PORTING_STATUS.md
  - docs/porting/NEXT_AGENT_HANDOFF.md
  - docs/porting/CH_NJOL_SKRIPT_AUDIT.md
  - docs/porting/FABRIC_PORT_STAGES.md
  - docs/porting/IMPLEMENTED_SYNTAX.md
- refresh root pointer headlines if headline numbers changed

Current priority:
- keep Stage 8 package-local audit frozen at 23 / 214 unless explicitly reassigned
- prioritize upstream ch/njol/skript closure
- continue Part 1A and Part 1B only

Collect from each lane before merge:
- changed file list
- exact tests run
- exact counts changed
- unresolved risks
```

## Lane A Prompt

```text
You are Lane A in a parallel Codex run.
Your worktree is /Users/qf/IdeaProjects/Skript-Fabric-port-lane-a.

Read in this order:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_A_STATUS.md

Your scope is only:
- Statement orchestration
- ScriptLoader flow
- parse-log / loader diagnostics
- matching tests and real .sk coverage if your changes affect user-visible script loading

Primary owned files:
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/ScriptLoader.java
- src/main/java/ch/njol/skript/log/**
- src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java

Do not edit:
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- src/main/java/ch/njol/skript/variables/**
- src/main/java/ch/njol/skript/config/**
- docs/porting/*.md canonical docs

Current target:
- continue Part 1A on broader Statement / ScriptLoader orchestration and loader hint flow
- keep the just-closed options validator/config slice intact
- do not reopen already-closed comment-aware loader parsing unless you have a new failing reproducer

Rules:
- do not claim parity-complete
- if user-visible .sk behavior changes, verify with real .sk plus GameTest
- update only docs/porting/parallel/LANE_A_STATUS.md for documentation
- record exact commands and results in that lane file
```

## Lane B Prompt

```text
You are Lane B in a parallel Codex run.
Your worktree is /Users/qf/IdeaProjects/Skript-Fabric-port-lane-b.

Read in this order:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_B_STATUS.md

Your scope is only:
- SkriptParser flow
- patterns package support
- parser tag / mark parity
- parser-facing tests and real .sk coverage if live parsing changes

Primary owned files:
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- src/main/java/ch/njol/skript/patterns/**
- parser-facing tests such as SkriptParserRegistryTest and related parser compatibility tests

Do not edit:
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/ScriptLoader.java
- src/main/java/ch/njol/skript/variables/**
- docs/porting/*.md canonical docs

Current target:
- continue Part 1A on richer parser tag / mark / pattern parity
- preserve the already-green natural-script forms
- preserve the already-closed SecIf and parse-if coverage

Rules:
- do not claim parity-complete
- if live .sk parsing changes, run real .sk verification
- update only docs/porting/parallel/LANE_B_STATUS.md for documentation
- record exact commands and results in that lane file
```

## Lane C Prompt

```text
You are Lane C in a parallel Codex run.
Your worktree is /Users/qf/IdeaProjects/Skript-Fabric-port-lane-c.

Read in this order:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_C_STATUS.md

Your scope is only:
- Variables
- Classes
- config
- structures
- matching tests and real .sk coverage if structure or variable runtime behavior changes

Primary owned files:
- src/main/java/ch/njol/skript/variables/**
- src/main/java/ch/njol/skript/registrations/Classes.java
- src/main/java/ch/njol/skript/config/**
- src/main/java/ch/njol/skript/structures/**
- matching tests under src/test/java/ch/njol/skript/**

Do not edit:
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- docs/porting/*.md canonical docs

Current target:
- continue Part 1B after the just-closed SectionNode / validator-backed options slice
- prioritize deeper Variables / Classes semantics and remaining config / structure behavior
- preserve the green runtime options path and invalid-line diagnostics

Rules:
- do not claim parity-complete
- if user-visible .sk behavior changes, verify with real .sk plus GameTest
- update only docs/porting/parallel/LANE_C_STATUS.md for documentation
- record exact commands and results in that lane file
```

## Lane D Prompt

```text
You are Lane D in a parallel Codex run.
Your worktree is /Users/qf/IdeaProjects/Skript-Fabric-port-lane-d.

Read in this order:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_D_STATUS.md

Your scope is only:
- lang/function runtime
- overload/default-parameter parity
- parser-default helper closure that stays inside lang/function-owned files
- matching tests and real .sk coverage if live function behavior changes

Primary owned files:
- src/main/java/ch/njol/skript/lang/function/**
- src/main/java/ch/njol/skript/lang/DefaultExpression.java
- src/main/java/ch/njol/skript/lang/DefaultExpressionUtils.java
- src/main/java/ch/njol/skript/lang/parser/DefaultValueData.java
- matching function/parser-default tests

Do not edit:
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/ScriptLoader.java
- src/main/java/ch/njol/skript/variables/**
- docs/porting/*.md canonical docs

Current target:
- continue Part 1A on broader function runtime and default-parameter parity
- preserve the already-green exact-type overload behavior
- do not widen scope into parser ownership; hand that back to the coordinator

Rules:
- do not claim parity-complete
- if live .sk behavior changes, run real .sk verification
- update only docs/porting/parallel/LANE_D_STATUS.md for documentation
- record exact commands and results in that lane file
```

## Lane E Prompt

```text
You are Lane E in a parallel Codex run.
Your worktree is /Users/qf/IdeaProjects/Skript-Fabric-port-lane-e.

Read in this order:
1. docs/porting/README.md
2. docs/porting/NEXT_AGENT_HANDOFF.md
3. docs/porting/CH_NJOL_SKRIPT_AUDIT.md
4. docs/porting/CODEX_PARALLEL_WORKFLOW.md
5. docs/porting/parallel/LANE_E_STATUS.md

Your scope is only:
- parser/runtime bridge files
- InputSource / ParserInstance parity
- upstream diff-driven reproductions that stay outside Lane A-D ownership
- matching tests and real .sk coverage if bridge behavior changes

Primary owned files:
- src/main/java/ch/njol/skript/lang/InputSource.java
- src/main/java/ch/njol/skript/lang/parser/ParserInstance.java
- src/main/java/ch/njol/skript/expressions/ExprInput.java
- src/main/java/ch/njol/skript/lang/TriggerItem.java
- src/main/java/ch/njol/skript/lang/TriggerSection.java
- tightly matching tests

Do not edit:
- src/main/java/ch/njol/skript/lang/SkriptParser.java
- src/main/java/ch/njol/skript/lang/Statement.java
- src/main/java/ch/njol/skript/ScriptLoader.java
- src/main/java/ch/njol/skript/variables/**
- src/main/java/ch/njol/skript/registrations/Classes.java
- docs/porting/*.md canonical docs

Current target:
- continue upstream diff-driven reproduction work on parser/runtime bridge files
- prefer one contained mismatch with a focused regression over broad exploratory edits
- stop and return to the coordinator if the reproducer needs parser, classes, or statement ownership files

Rules:
- do not claim parity-complete
- if live .sk behavior changes, run real .sk verification
- update only docs/porting/parallel/LANE_E_STATUS.md for documentation
- record exact commands and results in that lane file
```

## Post-Merge Coordinator Prompt

```text
All lane work is merged or ready to merge.

Now act only as the integration coordinator in /Users/qf/IdeaProjects/Skript-Fabric-port.

Tasks:
1. merge Lane C, then Lane B, then Lane D, then Lane E, then Lane A unless actual conflicts force a different order
2. resolve integration fallout only in the coordinator worktree
3. rerun:
   - ./gradlew runGameTest --rerun-tasks
   - ./gradlew build --rerun-tasks
4. update canonical docs with exact scope, exact counts, and exact verification numbers
5. refresh root pointer headlines if headline numbers changed
6. produce a final integration summary listing:
   - what each lane delivered
   - what was merged
   - what was deferred
   - exact test results
```
