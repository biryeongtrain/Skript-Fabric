scope

- `src/main/java/ch/njol/skript/effects/**`
- direct runtime support/accessor checks for the M6 effect bundle only
- tightly matching effect tests only

latest slice

- landed classes:
  - `EffCopy`
  - `EffSort`
  - `EffToggle`
  - `EffExceptionDebug`
  - `EffClearEntityStorage`
  - `EffInsertEntityStorage`
  - `EffReleaseEntityStorage`
  - `EffEntityVisibility`
- runtime-eligible classes:
  - `EffCopy`
  - `EffSort`
  - `EffToggle`
  - `EffExceptionDebug`
  - existing `EffKeepInventory` remains runtime-implementable once bootstrap registration is added
- bootstrap registrations needed:
  - `EffKeepInventory`
  - `EffMakeSay`
  - `EffConnect`
  - `EffScriptFile`
  - `EffCopy`
  - `EffSort`
  - `EffToggle`
  - `EffExceptionDebug`
  - leave `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`, and `EffEntityVisibility` unregistered until runtime ownership exists
- blockers:
  - `EffMakeSay`: no confirmed Fabric chat/command injection bridge in owned scope
  - `EffConnect`: no proxy transfer/runtime channel bridge in owned scope
  - `EffScriptFile`: no dynamic script lifecycle manager in owned scope
  - `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`: entity-block-storage mutation backend is not exposed in the current Fabric layer
  - `EffEntityVisibility`: per-viewer entity hide/show backend and `ExprHiddenPlayers` support are outside the current owned surface

verification

- `./gradlew -I /tmp/m6-isolated-test.init.gradle isolatedM6EffectStorageUtilityTest --rerun-tasks`

next lead

- if coordinator wants a second M6 pass, the highest-value follow-up is wiring one live-activation path (`EffMakeSay` first) only if a server-safe Fabric chat bridge is explicitly approved

merge notes

- likely conflict files:
  - `src/main/java/ch/njol/skript/effects/EffCopy.java`
  - `src/main/java/ch/njol/skript/effects/EffSort.java`
  - `src/main/java/ch/njol/skript/effects/EffToggle.java`
  - `src/test/java/ch/njol/skript/effects/EffectStorageUtilityCompatibilityTest.java`
