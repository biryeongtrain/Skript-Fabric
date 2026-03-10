# Surface F Event Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/events/**`
- nearby `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests only

## Latest Slice

- added 18 lane-local upstream-style event restorations in `ch/njol/skript/events`:
  - `EvtBookEdit`
  - `EvtBookSign`
  - `EvtClick`
  - `EvtEntityShootBow`
  - `EvtEntityTarget`
  - `EvtEntityTransform`
  - `EvtExperienceSpawn`
  - `EvtFirework`
  - `EvtGameMode`
  - `EvtHarvestBlock`
  - `EvtHealing`
  - `EvtLeash`
  - `EvtMoveOn`
  - `EvtPlayerArmorChange`
  - `EvtPortal`
  - `EvtResourcePackResponse`
  - `EvtWeatherChange`
  - `EvtWorld`
- added 3 lane-local support classes under `ch/njol/skript/events` to keep the bundle mergeable without opening `org/...`:
  - `EventClassInfoRegistrar`
  - `EventSyntaxRegistry`
  - `FabricEventCompatHandles`
- added 9 player/runtime event classes in `ch/njol/skript/events` backed by lane-local handle scaffolding:
  - `EvtCommand`
  - `EvtFirstJoin`
  - `EvtLevel`
  - `EvtMove`
  - `EvtPlayerChunkEnter`
  - `EvtPlayerCommandSend`
  - `EvtSpectate`
  - `EvtTeleport`
  - `EvtExperienceChange`
- added 2 nearby fallback effects in `ch/njol/skript/effects`:
  - `EffEnforceWhitelist`
  - `EffRespawn`
- added shared lane-local handle scaffolding for the new bundle:
  - `FabricPlayerEventHandles`
  - `FabricEffectEventHandles`
- extended focused parser/check coverage in:
  - `src/test/java/ch/njol/skript/events/EventCompatibilityTest`
  - `src/test/java/ch/njol/skript/effects/EffectServerControlCompatibilityTest`
- kept the new bundle on lane-local handles instead of `org/skriptlang/skript/fabric/runtime/**`; the added events are parser/unit-verifiable now without claiming live bridge completeness
- exact blockers that remain for full upstream parity:
  - no shared Bukkit/Paper API on this branch's compile classpath, so these ports cannot reuse upstream Bukkit event classes directly
  - no lane-owned live dispatch path from `org/skriptlang/skript/fabric/runtime/**` into the new event families, so runtime execution still depends on a later bridge pass outside this worker's allowed scope
  - `EvtClick`, `EvtHarvestBlock`, and `EvtMoveOn` are currently lane-local item/entity variants only; upstream blockdata and Bukkit deduplication behavior still need broader compatibility plumbing
  - `EvtPlayerArmorChange` is restored as armor-slot syntax closure, but Paper-specific event-value parity is still blocked by the missing Paper event surface in this branch

## Verification

- `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests ch.njol.skript.effects.EffectServerControlCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- if Lane F stays on `events/**` without opening `org/...`, the nearest follow-up is either:
  - more parser-only event imports that can live on lane-local handles, or
  - the remaining fallback effects that only need server/player APIs
- if the coordinator wants these events to execute in live runtime scripts, the missing step is a later `org/skriptlang/skript/fabric/runtime/**` bridge pass outside this worker's allowed scope

## Merge Notes

- exact verification command:
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --rerun-tasks`
- likely conflicts:
  - `src/main/java/ch/njol/skript/events/*.java`
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/events/EventCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/effects/EffectServerControlCompatibilityTest.java`
