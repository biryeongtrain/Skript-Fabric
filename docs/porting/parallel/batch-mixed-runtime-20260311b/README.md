# Mixed Runtime Batch 2026-03-11B

Coordinator batch for the next syntax-heavy worker cycle.

- coordinator branch baseline: `111974a24`
- runtime verification baseline before launch: `245 / 245` Fabric GameTests through `./gradlew build --rerun-tasks`
- raw `ch/njol/skript` snapshot before launch: local `798 / 1189`, shortfall `391`
- priority: reduce the remaining syntax-heavy shortfall in `expressions`, plus live activation and runtime closure for imported `events` and `effects`
- minimum assigned scope this cycle: `119` syntax-equivalent targets across 6 workers
- worker rules:
  - use local upstream snapshots only
  - no canonical doc edits
  - no `SkriptFabricBootstrap.java`, `SkriptFabricAdditionalSyntax.java`, or `SkriptFabricAdditionalEffects.java` edits
  - add representative real `.sk` GameTest coverage if a lane makes syntax newly active in runtime
  - targeted verification only
- coordinator duties after worker completion:
  - merge/no-op judgment
  - runtime registration integration
  - canonical docs
  - final `./gradlew build --rerun-tasks`

Lane split:

- `M1`: active event expansion plus import-only effect activation follow-up
- `M2`: inventory, item, and enchantment expressions
- `M3`: player, server, chat, and permission expressions
- `M4`: entity, combat, and event-context expressions
- `M5`: block, world, and location expressions
- `M6`: script, parser, queue, and collection expressions
