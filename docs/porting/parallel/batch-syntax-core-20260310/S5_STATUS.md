# S5 Status

## landed classes
- `EffDetonate`
- `EffWorldBorderExpand`
- `EffLog`
- `EffRun`
- `EffSuppressWarnings`
- `EffSuppressTypeHints`
- `EffMakeSay`
- `EffConnect`
- `EffScriptFile`

## runtime-eligible classes
- `EffDetonate`
- `EffWorldBorderExpand`
- `EffLog`
- `EffRun`
- `EffSuppressWarnings`
- `EffSuppressTypeHints`

## bootstrap registrations needed
- `SkriptFabricBootstrap.java` registration follow-up for:
  - `EffDetonate`
  - `EffWorldBorderExpand`
  - `EffLog`
  - `EffRun`
  - `EffSuppressWarnings`
  - `EffSuppressTypeHints`
- additional runtime bridge work still required before bootstrap registration for:
  - `EffMakeSay`
  - `EffConnect`
  - `EffScriptFile`

## targeted tests
- added narrow parser/init coverage in `src/test/java/ch/njol/skript/effects/EffectWorldServerCompatibilityTest.java`
- attempted:
  - `./gradlew test --tests ch.njol.skript.effects.EffectWorldServerCompatibilityTest --rerun-tasks`
    - failed in sandbox before Gradle startup: `FileNotFoundException ... gradle-9.2.1-bin.zip.lck (Operation not permitted)`
  - `GRADLE_USER_HOME=/tmp/gradle-s5 ./gradlew test --tests ch.njol.skript.effects.EffectWorldServerCompatibilityTest --rerun-tasks`
    - failed because wrapper download is blocked: `UnknownHostException: services.gradle.org`
  - `GRADLE_USER_HOME=/tmp/gradle-s5 /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle -Dorg.gradle.cache.internal.locklistener=false test --tests ch.njol.skript.effects.EffectWorldServerCompatibilityTest --rerun-tasks`
    - failed in sandbox before build execution: `Could not create service of type FileLockContentionHandler ... SocketException: Operation not permitted`

## blockers
- `EffMakeSay`: no current Fabric runtime bridge for forcing player chat / command-send semantics from `ServerPlayer`
- `EffConnect`: no current proxy transfer or plugin-message bridge in the local Fabric runtime
- `EffScriptFile`: no local runtime ownership for enable/disable/reload/unload script files inside worker scope
- `EffEntityVisibility`: would require viewer/entity hide-show ownership outside the current imported runtime surface
- `EffClearEntityStorage`, `EffInsertEntityStorage`, `EffReleaseEntityStorage`: beehive/entity-storage runtime path needs deeper block-entity ownership and version-safety verification than fit this lane slice
- targeted Gradle verification is blocked in this sandbox by wrapper lock/network/file-lock socket restrictions

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/effects/EffectWorldServerCompatibilityTest.java`
  - `docs/porting/parallel/batch-syntax-core-20260310/S5_STATUS.md`
- commit SHAs:
  - commit blocked in sandbox: `git commit -m "feat: import world and server effect compatibility bundle"` failed with `Unable to create .../.git/worktrees/s5/index.lock: Operation not permitted`
