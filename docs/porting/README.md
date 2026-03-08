# Porting Docs

Last updated: 2026-03-08

This directory is the canonical home for long-lived porting tracking documents.
Root-level files remain as entrypoints only and should point here.

## Read Order

1. [PORTING_STATUS.md](PORTING_STATUS.md)
2. [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md)
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) for multi-session Codex runs
8. [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) for copy-paste lane prompts

## Update Rules

- Keep exact counts and exact scope. Do not round or paraphrase progress.
- Record every priority change in both `PORTING_STATUS.md` and `NEXT_AGENT_HANDOFF.md`.
- For each implementation or audit slice, update the relevant matrix doc in the same turn.
- Do not mark parity complete without real `.sk` plus Fabric GameTest coverage when user-visible syntax is involved.
- Keep cross-cutting gaps separate from package-local audit counts.

## Active Workstreams

- Fabric runtime and Stage 8 parity tracking for `org/skriptlang/skript/bukkit`
- Upstream `ch/njol/skript` surface audit and closure tracking

## Parallel Session Docs

- [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md)
- [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md)
- [parallel/README.md](parallel/README.md)
