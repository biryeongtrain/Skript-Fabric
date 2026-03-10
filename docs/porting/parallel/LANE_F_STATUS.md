# Lane F Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- `src/main/java/ch/njol/skript/events/**`
- `src/main/java/ch/njol/skript/entity/**`

## Latest Slice

- imported upstream-style script lifecycle events:
  - `ch/njol/skript/events/EvtScript`
  - `ch/njol/skript/events/EvtSkript`
- wired `on load` / `on unload` through the existing `Structure.postLoad()` and `Structure.unload()` path with no new Fabric bridge layer
- wired `on skript start` / `on skript stop` at the runtime transition boundary:
  - start fires once when the in-memory runtime goes from empty to loaded
  - stop fires before `clearScripts()` begins unloading structures
- extended targeted compatibility/runtime coverage for parser rendering plus runtime execution of all four lifecycle triggers
- nearest same-scope follow-ons are currently blocked by missing clean local scheduler/player/world bridges:
  - `EvtPeriodical` depends on scheduled world-aware events and task cancellation surfaces that do not exist in the Fabric runtime yet
  - `EvtRealTime` depends on delayed main-thread scheduling and a synthetic timed event surface
  - `EvtFirstJoin` and the wider `SimpleEvents` cluster depend on larger player/server event bridges outside this closure

## Verification

- `./gradlew test --tests ch.njol.skript.events.EventCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.ScriptLifecycleRuntimeTest`
  - passed

## Next Lead

- next importable Lane F bundle should stay on event/effect/entity surfaces that already have a concrete Fabric handle or pure structure lifecycle route; the remaining nearby event backlog is no longer a small closure without adding scheduler/player/world bridges

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/events/*.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptRuntime.java`
