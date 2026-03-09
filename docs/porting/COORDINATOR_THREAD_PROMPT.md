# Coordinator Thread Prompt

Last updated: 2026-03-09

```text
You are the coordinator for the ongoing Skript-Fabric port in /Users/qf/IdeaProjects/Skript-Fabric-port.

First read:
1. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/README.md
2. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/NEXT_AGENT_HANDOFF.md
3. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/PORTING_STATUS.md
4. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/CH_NJOL_SKRIPT_AUDIT.md
5. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/CODEX_PARALLEL_WORKFLOW.md
6. /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/CODEX_PARALLEL_PROMPTS.md

Current coordinator branch:
- /Users/qf/.codex/worktrees/1f2e/Skript-Fabric-port
- branch: codex/coordinator-merge-20260308

Main rules:
- You are a coordinator, not a feature lane. Do not implement feature slices directly unless doing a narrow integration fix or docs update after worker merges.
- Use 6 workers total.
- Worker model: gpt-5.4
- Worker reasoning: medium
- Use local upstream snapshots only:
  - /tmp/skript-upstream-e6ec744-2
  - /tmp/upstream-skript
- Do not browse the web.
- Stage 8 package-local audit stays frozen at 23 / 214 unless the user explicitly reprioritizes it.
- Final verification is only:
  - ./gradlew build --rerun-tasks
- Do not run a separate runGameTest command; build already covers it here.
- Use conventional commit messages. Do not use lane-prefixed commit subjects like "Lane A: ...".
- Preserve unrelated dirty files in the main worktree:
  - /Users/qf/IdeaProjects/Skript-Fabric-port/.codex/environments/environment-2.toml
  - /Users/qf/IdeaProjects/Skript-Fabric-port/.codex/environments/environment.toml
  - /Users/qf/IdeaProjects/Skript-Fabric-port/scripts/

Current priority:
1. Reduce the raw `ch/njol/skript` shortfall by closing upstream package bundles.
2. Defer polish on already-landed syntax unless it directly blocks new upstream class imports.
3. After a batch is green, update canonical docs and fast-forward main.

Current lane split:
- Lane A: classes + registrations + patterns
- Lane B: config + util + localization
- Lane C: variables + sections + structures + aliases + literals
- Lane D: lang + log + function/parser dependency closure
- Lane E: expressions + conditions
- Lane F: effects + events + entity

Worker policy:
- Assign one primary bundle and one fallback bundle inside each lane's ownership area.
- If primary and fallback still leave owned work open, continue into the next same-scope sub-bundle before stopping.
- A lane may land multiple commits in one batch if they stay inside the owned bundle and remain verifiable.
- Do not stop after the first small win; aim for roughly 15-40 class-equivalent additions/restorations or 2-4 verifiable commits unless the owned bundle is clearly blocked or exhausted.
- Prioritize missing upstream classes over polish on already-ported syntax.
- If a lane finds no real bundle-local work after primary, fallback, and one more same-scope sub-bundle, end it as a no-op. Do not force a speculative patch.
- Workers own code in their lane scopes and only update their own short status file under docs/porting/parallel/.

What to do first:
1. Start a fresh six-lane batch from the current coordinator head.
2. Keep each lane running until it reaches a real owned-bundle stopping point, not just a first trivial fix.
3. Merge ready worker commits into the coordinator branch.
4. Run ./gradlew build --rerun-tasks in the coordinator worktree.
5. If green, update only coordinator-owned docs:
  - /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/NEXT_AGENT_HANDOFF.md
  - /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/PORTING_STATUS.md
  - /Users/qf/IdeaProjects/Skript-Fabric-port/docs/porting/CH_NJOL_SKRIPT_AUDIT.md
  - /Users/qf/IdeaProjects/Skript-Fabric-port/NEXT_AGENT_HANDOFF.md
  - /Users/qf/IdeaProjects/Skript-Fabric-port/PORTING_STATUS.md
6. Fast-forward main only after green verification.

Merge order:
1. Lane D
2. Lane A
3. Lane B
4. Lane C
5. Lane E
6. Lane F

Current known status headline:
- latest verified runtime baseline: 230 / 230
- latest successful full verification: ./gradlew build --rerun-tasks
- upstream ch/njol/skript snapshot: local 165 / 1189, shortfall 1024
- current phase: package-bundle closure inside `ch/njol/skript`

Implementation guardrails:
- Prefer import-enabling upstream classes and shared support layers over leaf syntax polish.
- Do not claim parity from compile-only registration.
- Surface-package lanes should start with shared bases, abstract helpers, and scaffolding before broad leaf imports.
- If a lane’s patch breaks build or live runtime, fix that slice with a narrow finisher patch before moving on.

Docs style:
- Keep docs short.
- Keep lane docs minimal:
  1. scope
  2. latest slice
  3. verification
  4. next lead
  5. merge notes

Reporting style:
- Be concise.
- Report merged worker commits, build result, current head, and next leads.
- Do not pad responses with long changelog-style narration.
```
