# S4 Status

## landed classes

- `EffApplyBoneMeal`
- `EffEntityUnload`
- `EffForceEnchantmentGlint`
- `EffKeepInventory`
- `EffReplace`

## runtime-eligible classes

- `EffApplyBoneMeal`
- `EffEntityUnload`
- `EffForceEnchantmentGlint`
- `EffReplace`

## bootstrap registrations needed

- none for this lane slice; all five classes are lane-local imports/tests only and are not wired into `SkriptFabricBootstrap`

## targeted tests

- `./gradlew test --tests ch.njol.skript.effects.EffectMutationCompatibilityTest --rerun-tasks`
  - failed in sandbox before Gradle startup: `FileNotFoundException` on `/Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/.../gradle-9.2.1-bin.zip.lck` (`Operation not permitted`)
- `GRADLE_USER_HOME=/tmp/gradle-s4 ./gradlew test --tests ch.njol.skript.effects.EffectMutationCompatibilityTest --rerun-tasks`
  - failed in sandbox while wrapper tried to download Gradle: `UnknownHostException: services.gradle.org`
- `GRADLE_USER_HOME=/tmp/gradle-s4 /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle --no-daemon -Dorg.gradle.cache.internal.locklistener=false test --tests ch.njol.skript.effects.EffectMutationCompatibilityTest --rerun-tasks`
  - failed in sandbox before build execution: `Could not create service of type FileLockContentionHandler` caused by `java.net.SocketException: Operation not permitted`

## blockers

- `EffColorItems`: needs confirmed 1.21 item-component mutation coverage for dyed armor, potion color, and map color; not safe to guess without a successful compile/test loop
- `EffEnchant`: local tree has no active `enchantmenttype` classinfo/runtime bridge in lane scope, so the upstream syntax cannot be wired cleanly here without cross-scope type work
- `EffEquip`: depends on a larger equipment/slot abstraction pass across multiple entity families and newer item components; too large for this lane slice
- `EffDrop`: upstream syntax depends on `Direction`; local `Direction` compat is still outside lane scope
- `EffHealth`: upstream implementation depends on missing `bukkitutil` helpers (`HealthUtils`, `DamageUtils`, `ItemUtils`) outside lane scope
- `EffTeleport`: depends on missing/unfinished `Direction` and teleport-flag support plus coordinator-owned runtime registration decisions
- `EffWakeupSleep`: depends on missing local `Direction` support and broader sleep/wakeup runtime mapping work
- `EffTree`: depends on missing local `StructureType`
- `EffKeepInventory`: landed as parser/runtime-handle compatibility scaffolding only; real Fabric death-handle wiring still needs confirmation in integration
- targeted Gradle verification is currently blocked in this sandbox by wrapper lock permissions, no network for wrapper download, and Gradle local socket restrictions
- `git commit` is blocked in this sandbox because Git cannot create `/Users/qf/IdeaProjects/Skript-Fabric-port/.git/worktrees/s4/index.lock`

## merge-note

- likely conflicts:
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/effects/EffectMutationCompatibilityTest.java`
  - `docs/porting/parallel/batch-syntax-core-20260310/S4_STATUS.md`
- commit SHAs:
  - none; commit blocked by sandboxed Git metadata permissions
