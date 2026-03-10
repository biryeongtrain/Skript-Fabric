# Mixed Runtime Batch 2026-03-11

Coordinator batch for the next syntax-heavy worker cycle.

- coordinator branch baseline: `0bcc55673`
- priority: reduce raw syntax shortfall in `conditions`, `expressions`, `effects`, and active `events`
- minimum assigned scope this cycle: `124` syntax-equivalent targets across 6 workers
- worker rules:
  - use local upstream snapshots only
  - no canonical doc edits
  - no `SkriptFabricBootstrap.java` edits
  - add representative real `.sk` GameTest coverage if a lane makes syntax newly active in runtime
  - targeted verification only
- coordinator duties after worker completion:
  - merge/no-op judgment
  - bootstrap integration
  - canonical docs
  - final `./gradlew build --rerun-tasks`

Lane split:

- `M1`: remaining conditions + concrete event activation and event expansion
- `M2`: inventory/container/enchantment expressions
- `M3`: player/server/chat expressions
- `M4`: event-payload/combat/entity expressions
- `M5`: block/world/location expressions
- `M6`: remaining effects + import-only effect activation
