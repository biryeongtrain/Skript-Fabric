# S2 Status

## landed classes
- `ExprItemCooldown`
- `ExprCommandBlockCommand`
- added narrow syntax coverage in `ExpressionSyntaxS2CompatibilityTest`

## runtime-eligible classes
- `ExprItemCooldown`
- `ExprCommandBlockCommand`
- neither is active in the Fabric runtime yet because this worker did not touch `SkriptFabricBootstrap`

## bootstrap registrations needed
- register `ExprItemCooldown`
- register `ExprCommandBlockCommand`

## targeted tests
- `./gradlew -q compileJava --rerun-tasks`
  - failed in sandbox: wrapper lock under `/Users/qf/.gradle/wrapper/dists/.../gradle-9.2.1-bin.zip.lck` was not writable
- `GRADLE_USER_HOME=/private/tmp/syntax-core-20260310/s2/.gradle-local ./gradlew -q compileJava --rerun-tasks`
  - failed in sandbox: wrapper tried to download `https://services.gradle.org/distributions/gradle-9.2.1-bin.zip` and network access is disabled
- `GRADLE_USER_HOME=/private/tmp/syntax-core-20260310/s2/.gradle-local /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle -q compileJava --rerun-tasks`
  - failed in sandbox: Gradle could not create `FileLockContentionHandler` because local socket creation is not permitted
- `GRADLE_USER_HOME=/private/tmp/syntax-core-20260310/s2/.gradle-local /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle test --tests ch.njol.skript.expressions.ExpressionSyntaxS2CompatibilityTest --rerun-tasks`
  - failed with the same `FileLockContentionHandler` socket restriction
- `javac --release 21 -cp \"$(cat /tmp/s2_classpath.txt)\" -sourcepath src/main/java:src/test/java -d /tmp/s2-javac-out src/main/java/ch/njol/skript/expressions/ExprItemCooldown.java src/main/java/ch/njol/skript/expressions/ExprCommandBlockCommand.java src/test/java/ch/njol/skript/expressions/ExpressionSyntaxS2CompatibilityTest.java`
  - failed because the cached Fabric Loom Minecraft jars are obfuscated (`aa.class`-style), so manual `javac` outside Gradle could not resolve remapped Minecraft classes

## blockers
- `ExprInventoryAction`
  - still blocked on missing local inventory-click event value wiring / compat event classes
- `ExprInventoryCloseReason`
  - still blocked on missing inventory-close event handle and close-reason compat surface
- `ExprCursorSlot`
  - still blocked on missing cursor-slot abstraction and inventory-click event surface
- `ExprHotbarButton`
  - still blocked on missing inventory-click event surface
- `ExprHotbarSlot`
  - still blocked on missing held-item event wiring and richer slot abstractions
- `ExprArmorChangeItem`
  - still blocked on armor-change event wiring
- `ExprArmorSlot`
  - still blocked on richer armor/equipment slot abstractions
- `ExprItemFlags`
  - still blocked on missing item-flag enum/helper scaffolding
- `ExprBannerItem`
  - still blocked on banner component translation
- `ExprBannerPatterns`
  - still blocked on banner component translation
- `ExprAnvilRepairCost`
  - still blocked on missing anvil inventory compat
- `ExprAnvilText`
  - still blocked on missing anvil inventory compat
- verification blocker
  - Gradle cannot run in this sandbox because the local file-lock contention listener needs socket creation permissions that are denied here

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/ExprItemCooldown.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCommandBlockCommand.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionSyntaxS2CompatibilityTest.java`
  - `docs/porting/parallel/batch-syntax-core-20260310/S2_STATUS.md`
- commit SHAs:
  - none; `git commit` was blocked by sandbox write restrictions on `/Users/qf/IdeaProjects/Skript-Fabric-port/.git/worktrees/s2/index.lock`
