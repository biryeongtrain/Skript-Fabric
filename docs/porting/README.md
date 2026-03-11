# Porting Docs

Last condensed: 2026-03-11
Last full verification: 2026-03-11

Canonical porting docs live here. Keep them short. Keep history in git.

## Read Order

1. [PORTING_STATUS.md](PORTING_STATUS.md)
2. [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md)
3. [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
4. [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
5. [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
6. [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
7. [CODEX_PARALLEL_WORKFLOW.md](CODEX_PARALLEL_WORKFLOW.md) and [CODEX_PARALLEL_PROMPTS.md](CODEX_PARALLEL_PROMPTS.md) only for parallel runs

## Update Rules

- Preserve exact counts and exact verification results.
- Update `PORTING_STATUS.md` and `NEXT_AGENT_HANDOFF.md` together when priorities change.
- Keep `CH_NJOL_SKRIPT_AUDIT.md` limited to current closure state, not work logs.
- Keep lane detail in `docs/porting/parallel/*.md`; keep canonical docs summary-only.
