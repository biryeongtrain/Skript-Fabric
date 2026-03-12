# Agent Notes

- Worker agents in this repo must use `.codex/agents/workers.toml`.
- Keep worker execution on `workspace-write` with `approval_policy = "never"` and `web_search = "disabled"`.
- Worker agents must not ask the user questions. Make reasonable local assumptions and continue.
- If a worker is blocked or uncertain, it must report back to the coordinator in its result instead of asking the user directly.
- Use `/Users/qf/IdeaProjects/Skript` as the canonical local upstream reference for Skript parity checks.
- Do not use web search for upstream Skript lookups when the local clone is sufficient.
- Do not land new syntax without a real `.sk` GameTest that proves the syntax parses and executes in runtime.
- Do not count direct `dispatch(...)` of the target compat handle as event-hook validation; landed event hooks must be proven by a GameTest that triggers the real game-state or callback path.
- When a real-trigger GameTest replaces an event helper backfill case, remove that event from the helper-driven backfill suite in the same slice.
