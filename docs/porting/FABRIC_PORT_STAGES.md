# Fabric Port Stages

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Snapshot

- `src/main/java/org/skriptlang/skript/bukkit`: `199` classes
- direct `org.bukkit` / Paper references in `src/main/java`: `0`
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
  - `./gradlew build --rerun-tasks` passed

## Current Priority

- Do not start another large Stage 8 package-local audit slice yet.
- Current priority is upstream `ch/njol/skript` closure (`Part 1A` / `Part 1B`) tracked in [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md).

## Stage Table

| Stage | Status | Current note |
| --- | --- | --- |
| `1` baseline restore | `completed` | Fabric baseline is the active source path |
| `2` runtime harness | `completed` | real `.sk` loading and execution path is active |
| `3` core type mappings | `completed` | core Bukkit-to-Mojang adapters are active |
| `4` base type/info layer | `completed` | base type registrations are restored |
| `5` event backend | `in_progress` | `22 / 22` tracked rows active; see [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md) |
| `6` syntax packages | `in_progress` | source-complete: conditions `28 / 28`, expressions `84 / 84`, effects `24 / 24` |
| `7` GameTest suite | `in_progress` | full suite green at `230 / 230` |
| `8` parity audit | `in_progress` | `23 / 214` package-local classes audited, plus `4` top-level helpers outside the matrix |

## Current Notes

- Stage 5:
  - no missing event families remain in the currently tracked Fabric target surface
  - remaining risk is parity depth, not missing event rows
- Stage 6 recent runtime-closure work:
  - legacy `parseStatic` flags
  - explicit-literal-only `Classes.getPatternInfos(...)`
  - keyed plural defaults
- Stage 8 completed packages:
  - `breeding`
  - `input`
  - `interactions`
- Open Stage 8 parity gap:
  - ambiguous bare item-id compare, for example `event-item is wheat`
