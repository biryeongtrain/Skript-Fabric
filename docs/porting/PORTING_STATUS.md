# Skript-Fabric Porting Status

Last condensed: 2026-03-11
Last full verification: 2026-03-11

## Snapshot

- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `84 / 84`
  - effects: `24 / 24`
- Stage 5 event backend rows active: `22 / 22`
- Stage 8 package-local audit: `23 / 214`
- Package-local parity-complete packages:
  - `breeding`: `12 / 12`
  - `input`: `5 / 5`
  - `interactions`: `6 / 6`
- Remaining package-local Stage 8 scope: `191 / 214`
- Top-level non-package Bukkit helpers outside that matrix: `4`
- Upstream core audit baseline:
  - upstream `ch/njol/skript` snapshot `e6ec744`: `1189`
  - local `ch/njol/skript`: `140`
  - shortfall: `1049`
- Latest full verification:
  - `./gradlew runGameTest --rerun-tasks` passed with `230 / 230`
  - `./gradlew build --rerun-tasks` passed

## Active Priority

1. Keep Stage 8 counts frozen and accurate.
2. Continue `Part 1A` (`lang` parser/loader/runtime closure).
3. Continue `Part 1B` (`variables` / `classes` / `config` / `log` dependency closure).
4. Resume broader syntax imports only when they no longer block `Part 1A` / `Part 1B`.

## Latest Closed Core Slice

- `SkriptParser.parseStatic(...)` now matches with `ALL_FLAGS`, so legacy `SyntaxElementInfo` paths accept expression-only placeholders again.
- `Classes.getPatternInfos(...)` now matches upstream by considering only explicit literal patterns and preserving registration order.
- `Function.execute(...)` now matches upstream keyed plural default behavior instead of zipping multi-value defaults into keyed pairs.
- New regressions lock:
  - `SkriptParserStaticFlagsCompatibilityTest`
  - `FunctionOverloadDisambiguationImplementationTest`
  - `FunctionDefaultKeyedParameterCompatibilityTest`

## Open Gaps

- Broader parser default-value and pattern-element parity.
- Broader statement/loader orchestration only when a concrete mismatch is reproduced.
- Function namespace/default-parameter/runtime parity beyond the current fixes.
- Variable runtime is still an in-memory bridge, not upstream-complete.
- Cross-cutting Stage 8 parity gap: ambiguous bare item-id compare, for example `event-item is wheat`.

## Reference Docs

- Upstream closure tracker: [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
- Stage tracker: [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- Event bridge: [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- Active syntax surface: [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
