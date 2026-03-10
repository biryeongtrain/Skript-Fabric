# Surface F Event Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/events/**`
- nearby `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests only

## Latest Slice

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
- did not land the rest of the requested primary bundle because the current lane-owned runtime bridge still lacks corresponding dispatch handles for command/join/gamemode/move/teleport/resource-pack/player-command events; this slice keeps those classes parser/unit-verifiable without editing `org/...`

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
  - `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests ch.njol.skript.effects.EffectServerControlCompatibilityTest --rerun-tasks`
- likely conflicts:
  - `src/main/java/ch/njol/skript/events/*.java`
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/events/EventCompatibilityTest.java`
  - `src/test/java/ch/njol/skript/effects/EffectServerControlCompatibilityTest.java`
