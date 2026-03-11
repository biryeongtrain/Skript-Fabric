# Implemented Syntax Inventory

Last condensed: 2026-03-11
Last full verification: 2026-03-11

This is a maintenance summary of the active Fabric runtime surface. It is not a parity claim.

## Snapshot

- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `84 / 84`
  - effects: `24 / 24`
- Verified Fabric GameTests: `230 / 230`
- Recent narrow closure:
  - legacy `parseStatic` flags
  - explicit-literal-only `Classes.getPatternInfos(...)`
  - keyed plural defaults
- Stage 8 package-local audit: `23 / 214`
- Open cross-cutting parity gap: ambiguous bare item-id compare, for example `event-item is wheat`

## Registration Sources

- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java`

## Event Families

- gametest
- server tick
- block break
- use block
- use entity
- use item
- attack entity
- damage
- breeding
- bucket catch
- love mode enter
- brewing start
- brewing complete
- brewing fuel
- entity potion effect
- fishing
- loot generate
- player input
- fuel burn
- smelting start
- furnace smelt
- furnace extract

## Core Runtime Features

- live `options:` replacement
- comment-aware loader parsing
- direct `{...}` variable parsing plus `var {x}` forms
- `InputSource` support (`input`, typed input, `input index`)
- parse-time local variable hints
- `SecIf` chain support
- section fallback diagnostics
- plain-effect section ownership
- list-variable reindexing and natural numeric ordering

## Recent User-Visible Families Already Landed

- alive/dead
- silent
- invulnerable
- `feed`
- invisible/visible
- burning/on-fire
- AI
- sprinting
- glowing

## Related Docs

- [PORTING_STATUS.md](PORTING_STATUS.md)
- [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- [CH_NJOL_SKRIPT_AUDIT.md](CH_NJOL_SKRIPT_AUDIT.md)
