# Lane F Status

Last updated: 2026-03-09

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- landed an entity compatibility bundle plus Fabric-backed event/effect glue that stays inside lane ownership
- restored `EntityData`, `SimpleEntityData`, `EntityType`, and `EntityDataRegistry`
- restored `EvtBreeding`, `EvtBucketCatch`, and `EvtDamage`
- restored `EffFeed`, `EffInvisible`, `EffInvulnerability`, `EffKill`, `EffSilence`, and `EffSprinting`
- added focused compatibility coverage in `EntityCompatibilityTest`, `EventCompatibilityTest`, and `EffectCompatibilityTest`

## Verification

- `./gradlew test --tests ch.njol.skript.entity.EntityCompatibilityTest --tests ch.njol.skript.effects.EffectCompatibilityTest --tests ch.njol.skript.events.EventCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane F bundle is whichever additional `effects` / `events` / `entity` cluster can sit on the existing Fabric runtime without crossing into Lane E expression/condition ownership or non-owned `org/...` edits

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/entity/*`
  - `src/main/java/ch/njol/skript/events/Evt*.java`
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
