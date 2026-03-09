# Lane F Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- continued the requested low-risk entity-state effects cluster on the current mapped entity handles with `EffPandaRolling` and `EffStriderShivering`
- mapped the upstream panda roll toggle onto Mojang's current `Panda.roll(boolean)` handle and the upstream strider shiver toggle onto `Strider.setSuffocating(boolean)`
- extended `EffectCompatibilityTest` so the new effects prove their parse-mode/tag handling alongside the prior panda/screaming slice
- confirmed the next adjacent goat/enderman follow-ups are not clean on the current shared handles:
  - `EffGoatHorns` cannot be imported exactly because Mojang exposes only `addHorns()` / `removeHorns()` rather than per-side horn setters
  - `EffEndermanTeleport` is blocked because `EnderMan.teleport()` is `protected` and `teleportTowards(...)` is package-private on the current mapped class

## Verification

- `./gradlew testClasses --rerun-tasks`
  - passed
- `./gradlew isolatedEffectCompatibilityTest --rerun-tasks`
  - passed
- attempted but not used:
  - `./gradlew test --tests 'ch.njol.skript.effects.EffectCompatibilityTest' --rerun-tasks`
    - failed because the default `test` task excludes the class's `isolated-registry` tag
  - `./gradlew test --tests '*EffectCompatibilityTest.*' --rerun-tasks`
    - failed for the same reason

## Next Lead

- next importable Lane F bundle is whichever additional `effects` or `events` cluster binds to existing mapped handles without new `org/...` bridge edits; goat/enderman state follow-ups are currently blocked on the mapped API shape, so the safer continuation is a small event or effect cluster that only uses existing shared scaffolding

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
  - `src/test/java/ch/njol/skript/effects/EffectCompatibilityTest.java`
