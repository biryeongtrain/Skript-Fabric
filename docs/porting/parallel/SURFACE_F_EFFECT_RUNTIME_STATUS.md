# Surface F Effect Runtime Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests under `src/test/java/ch/njol/skript/effects/**`

## Latest Slice

- landed 10 upstream-backed effect classes inside `ch/njol/skript/effects`:
  - `EffEndermanTeleport`
  - `EffForceAttack`
  - `EffPathfind`
  - `EffPersistent`
  - `EffToggleFlight`
  - `EffTransform`
  - `EffVehicle`
  - `EffZombify`
  - `EffCommandBlockConditional`
  - `EffGlowingText`
- added shared reflection helpers in `EffectRuntimeSupport` so the runtime-facing imports stay inside the effect package without widening lane scope
- added `EffectRuntimeClosureCompatibilityTest` to cover parser/binding for the full 10-class bundle
- this slice intentionally stopped short of runtime bootstrap wiring because the current lane rules limited edits to `effects/**` plus tightly matching tests

## Verification

- `./gradlew test --rerun-tasks`
  - passed
- attempted but Gradle filter did not match even pre-existing effect unit classes in this worktree:
  - `./gradlew test --tests ch.njol.skript.effects.EffectRuntimeClosureCompatibilityTest --rerun-tasks`
  - `./gradlew test --tests ch.njol.skript.effects.EffectCompatibilityTest --rerun-tasks`

## Next Lead

- if lane ownership widens, wire the new effect registrations into the Fabric bootstrap and then add narrow runtime `.sk` coverage for the subset that already has a concrete local handle (`toggle flight`, `pathfind`, `persistent`, `vehicle`, `command block conditional`)
- if scope stays effect-only, the next nearby bundle is the remaining movement/runtime set that can be expressed without pulling in `Direction` or additional world/player bridge files

## Merge Notes

- exact verification command that passed:
  - `./gradlew test --rerun-tasks`
- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/EffectRuntimeSupport.java`
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
  - `src/test/java/ch/njol/skript/effects/*.java`
