# Parallel Lane Docs

Last condensed: 2026-03-11

Lane files are short, lane-local notes for parallel Codex runs.

- Workers update only their own lane file.
- Coordinator reads lane files, merges branches, then updates `docs/porting/*.md`.
- Keep exact commands and results.
- Keep conflict notes explicit.
