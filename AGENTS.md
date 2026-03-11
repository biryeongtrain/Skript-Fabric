# Agent Notes

- Worker agents in this repo must use `.codex/agents/workers.toml`.
- Keep worker execution on `workspace-write` with `approval_policy = "never"` and `web_search = "disabled"`.
- Use `/Users/qf/IdeaProjects/Skript` as the canonical local upstream reference for Skript parity checks.
- Do not use web search for upstream Skript lookups when the local clone is sufficient.
