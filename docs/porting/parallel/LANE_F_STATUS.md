# Lane F Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- imported the small upstream control-flow effect cluster with local-style `EffContinue` and `EffExit`
- adapted both effects onto the current `ExecutionIntent` / `TriggerItem.walk(...)` surface so loop/section exits use the existing stop-section plumbing without new runtime bridge edits
- extended `EffectCompatibilityTest` with parser-context coverage for:
  - `continue the 1st loop`
  - `stop 2 loops`
  - `stop trigger`
- adjacent Lane F blockers are unchanged:
  - `EffGoatHorns` still cannot be imported exactly because Mojang exposes only `addHorns()` / `removeHorns()` rather than per-side horn setters
  - `EffEndermanTeleport` is still blocked because `EnderMan.teleport()` is `protected` and `teleportTowards(...)` is package-private on the current mapped class

## Verification

- `./gradlew testClasses --rerun-tasks`
  - passed
- `./gradlew isolatedEffectCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane F bundle is whichever additional `effects` or `events` cluster binds to existing mapped handles without new `org/...` bridge edits; with the control-flow base effects now landed, the safer continuation is still a small event/effect cluster that reuses existing shared scaffolding rather than the blocked goat/enderman follow-ups

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
  - `src/test/java/ch/njol/skript/effects/EffectCompatibilityTest.java`
