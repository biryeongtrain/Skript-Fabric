# Parallel Lane Docs

Last updated: 2026-03-09

This directory is for worker-lane notes during a parallel Codex session.

Rules:

- Workers update only their own lane file.
- Coordinator reads all lane files, merges branches, and then updates canonical docs under `docs/porting/`.
- Keep lane docs short.
- Keep numbers exact.
- Record exact commands and exact results.
- Preferred format:
  - `Scope`
  - `Latest Slice`
  - `Verification`
  - `Next Lead`
  - `Merge Notes`

Lane files:

- `LANE_A_STATUS.md`
- `LANE_B_STATUS.md`
- `LANE_C_STATUS.md`
- `LANE_D_STATUS.md`
- `LANE_E_STATUS.md`
