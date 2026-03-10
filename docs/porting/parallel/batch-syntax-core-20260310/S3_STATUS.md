# S3 Status

## landed classes
- `ExprAttacked`
- `ExprAttacker`
- `ExprDamage`
- `ExprDamageCause`
- `ExprFinalDamage`
- `ExprLastDamageCause`
- `ExprExperience`
- `ExprHealReason`

## runtime-eligible classes
- `ExprAttacked`
- `ExprAttacker`
- `ExprDamage`
- `ExprDamageCause`
- `ExprFinalDamage`
- `ExprLastDamageCause`
- `ExprExperience`
- `ExprHealReason`
- note: current lane lands these as parser/unit-ready compatibility expressions; live script availability still needs bootstrap registration outside this worker's allowed files

## bootstrap registrations needed
- register `ExprAttacked`
- register `ExprAttacker`
- register `ExprDamage`
- register `ExprDamageCause`
- register `ExprFinalDamage`
- register `ExprLastDamageCause`
- register `ExprExperience`
- register `ExprHealReason`

## targeted tests
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionCombatContextCompatibilityTest --rerun-tasks`
  - failed in sandbox before task execution: Gradle wrapper lock file under `/Users/qf/.gradle/...` was not writable
- `GRADLE_USER_HOME=/tmp/s3-gradle-home ./gradlew test --tests ch.njol.skript.expressions.ExpressionCombatContextCompatibilityTest --rerun-tasks`
  - failed in sandbox before task execution: wrapper attempted network download of `https://services.gradle.org/distributions/gradle-9.2.1-bin.zip`
- `GRADLE_USER_HOME=/tmp/s3-gradle-home /Users/qf/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle test --tests ch.njol.skript.expressions.ExpressionCombatContextCompatibilityTest --rerun-tasks`
  - failed in sandbox before task execution: Gradle file-lock contention handler hit `java.net.SocketException: Operation not permitted`
- `git diff --check`
  - passed

## blockers
- `ExprAffectedEntities` blocked: no lane-local compat handle or event bridge for area-effect-cloud affected entity lists
- `ExprConsumedItem` blocked: current `FabricEventCompatHandles.EntityShootBow` does not expose the consumed item
- `ExprDrops` blocked: upstream depends on alias/item-drop event data not present in this lane and harvest compat handles do not expose mutable drop inventories
- `ExprExplodedBlocks` blocked: no lane-local explode compat handle in `ch/njol/skript/events/**`
- `ExprExperienceCooldownChangeReason` blocked: no lane-local experience-cooldown-change event handle, and the only nearby player experience-change handle is package-private
- `ExprHealAmount` blocked: current healing compat handle exposes entity and reason but not amount
- primary/fallback mutable semantics remain partial: current damage/experience compat handles are immutable records, so `ExprDamage`, `ExprFinalDamage`, and `ExprExperience` land as read-only compatibility expressions here
- exact targeted Gradle test execution is blocked by the sandbox's no-network and no-socket constraints

## merge-note
- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/*.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionCombatContextCompatibilityTest.java`
  - `docs/porting/parallel/batch-syntax-core-20260310/S3_STATUS.md`
- commit SHA(s):
  - no commit created in this sandbox: `git commit` failed because the worktree git dir at `/Users/qf/IdeaProjects/Skript-Fabric-port/.git/worktrees/s3` is outside the writable roots
