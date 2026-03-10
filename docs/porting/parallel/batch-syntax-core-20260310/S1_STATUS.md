# S1 Status

## landed classes
- `CondItemEnchantmentGlint`
- `CondIsFuel`
- `CondIsOfType`
- `CondIsResonating`
- `CondEntityStorageIsFull`
- `CondWillHatch`

## runtime-eligible classes
- `CondItemEnchantmentGlint`
- `CondIsFuel`
- `CondIsOfType`
- `CondWillHatch`

## bootstrap registrations needed
- none for this slice; these are upstream-import compatibility classes only and were not wired into `SkriptFabricBootstrap`

## targeted tests
- `GRADLE_USER_HOME=/tmp/gradle-s1 ./gradlew test --tests ch.njol.skript.conditions.ConditionSyntaxS1CompatibilityTest --rerun-tasks`
  - failed before configuration because the sandbox blocked `/Users/qf/.gradle/wrapper/dists/.../gradle-9.2.1-bin.zip.lck`
- `GRADLE_USER_HOME=/tmp/gradle-s1 /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle --no-daemon -Dorg.gradle.cache.internal.locklistener=false test --tests ch.njol.skript.conditions.ConditionSyntaxS1CompatibilityTest --rerun-tasks`
  - failed during Gradle startup because the sandbox blocked `FileLockContentionHandler` socket setup (`java.net.SocketException: Operation not permitted`)
- `javac --release 21 -cp \"$(cat /tmp/s1-javac-cp.txt)\" -sourcepath src/main/java -d /tmp/s1-javac-out-main ...`
  - failed in unrelated pre-existing sourcepath compilation on `org/skriptlang/skript/fabric/compat/FabricInventory.java` and `org/skriptlang/skript/fabric/placeholder/SkriptTextPlaceholders.java` due missing intermediary-mapped dependency classes (`net.minecraft.class_3917`, `class_2561`, `class_1735`, `class_2168`)

## blockers
- `CondIsEnchanted` still blocked by missing `EnchantmentType` compat support outside this lane
- `CondLeashWillDrop` still blocked because the local `FabricEntityUnleashEventHandle` exposes only `setDropLeash(...)`, not a readable current-state accessor
- `CondRespawnLocation` still blocked because the local respawn event marker has no readable bed-vs-anchor handle in-lane
- `CondFromMobSpawner` still blocked by missing local entity-side mob-spawner provenance support
- `CondLeashed` was not added because the local tree already carries the equivalent runtime condition as `CondIsLeashed`; importing the upstream-named class would duplicate the same syntax registration
- `CondHasClientWeather`, `CondHasResourcePack`, and `CondResourcePack` still need player/event state surfaces that are not locally exposed in-lane (`resourcepackstate` class info is also absent for the upstream `CondResourcePack` shape)
- `CondIsSpawnable` still needs an `EntityData.canSpawn(...)` compat path outside `conditions/**`

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/CondItemEnchantmentGlint.java`
  - `src/main/java/ch/njol/skript/conditions/CondIsFuel.java`
  - `src/main/java/ch/njol/skript/conditions/CondIsOfType.java`
  - `src/main/java/ch/njol/skript/conditions/CondIsResonating.java`
  - `src/main/java/ch/njol/skript/conditions/CondEntityStorageIsFull.java`
  - `src/main/java/ch/njol/skript/conditions/CondWillHatch.java`
  - `src/test/java/ch/njol/skript/conditions/ConditionSyntaxS1CompatibilityTest.java`
- commit SHAs:
  - not created; `git commit` was blocked by the sandbox trying to write `/Users/qf/IdeaProjects/Skript-Fabric-port/.git/worktrees/s1/index.lock`
