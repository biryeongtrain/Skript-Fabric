# Implemented Syntax Inventory

Last condensed: 2026-03-14
Last full verification: 2026-03-14

This is a maintenance summary of the active Fabric runtime surface. It is not a parity claim.

## Snapshot

- Source ports complete:
  - conditions: `28 / 28`
  - expressions: `85 / 85`
  - effects: `24 / 24`
  - sections: `9 / 9`
- Verified Fabric GameTests: `340 / 340`
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
- connect
- join / login
- kick
- quit / disconnect / leave
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
- `SecFilter` — filter variable lists with conditions (match any/all)
- `SecFor` — for-each loop with key/value variable extraction
- `ExprTransform` — list transformation/mapping with input expressions
- `ExprValueWithin` — extract typed values from lists/variables
- `SecCatchErrors` — catch runtime errors section with `ExprCaughtErrors`
- `SecWhile` — while/do-while loop section
- `ExprSecCreateWorldBorder` — virtual world border creation section expression
- `EffSecSpawn` — entity spawning with optional section for pre-spawn configuration
- `EffSecShoot` — projectile/entity shooting with optional section
- `RuntimeErrorManager` — full runtime error producer/consumer/filter/frame infrastructure
- `EntityData.spawn()` — Fabric-native entity spawning from EntityData

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
