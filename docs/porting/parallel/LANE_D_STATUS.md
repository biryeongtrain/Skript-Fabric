# Lane D Status

Last updated: 2026-03-08

## Scope

- upstream `CondAI` syntax import
- active Fabric runtime registration
- targeted AI condition coverage

## Owned Files

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondAI.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/AISyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricAIGameTest.java`
- `src/gametest/resources/skript/gametest/condition/has_ai_entity_names_entity.sk`
- `src/gametest/resources/skript/gametest/condition/no_ai_entity_names_entity.sk`
- `src/gametest/resources/fabric.mod.json`

## Goal For Next Session

- keep this slice merge-ready and let the coordinator fold the exact AI condition import into the canonical docs/status pass

## Work Log

- read the canonical porting docs plus the upstream `ch/njol/skript/conditions/CondAI.java` source before editing
- confirmed the upstream surface to import is the exact property-condition family:
  - `%livingentities% (has|have) (ai|artificial intelligence)`
  - `%livingentities% (doesn't|does not|do not|don't) have (ai|artificial intelligence)`
- added `org/skriptlang/skript/bukkit/base/conditions/CondAI.java` as the local Fabric-runtime condition implementation
  - uses the existing compatibility style from the already-landed base entity conditions
  - checks Mojang AI state through `Mob#isNoAi()` and renders as `has artificial intelligence`
- registered the exact upstream user-visible forms in `SkriptFabricBootstrap`
- added targeted unit coverage in `AISyntaxTest` proving exact parse coverage for:
  - `event-entity has ai`
  - `event-entity has artificial intelligence`
  - `event-entity does not have artificial intelligence`
- added dedicated real `.sk` + GameTest coverage proving live runtime behavior for both branches:
  - `event-entity has ai`
  - `event-entity does not have artificial intelligence`
  - plus direct runtime parse/evaluate checks for `event-entity has artificial intelligence` and `event-entity doesn't have ai`
- registered the new dedicated GameTest entrypoint in `src/gametest/resources/fabric.mod.json`
- this slice increased the full Fabric GameTest total from `220` to `223`

## Files Changed

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondAI.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/AISyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricAIGameTest.java`
- `src/gametest/resources/skript/gametest/condition/has_ai_entity_names_entity.sk`
- `src/gametest/resources/skript/gametest/condition/no_ai_entity_names_entity.sk`
- `src/gametest/resources/fabric.mod.json`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.AISyntaxTest --rerun-tasks`
  - passed
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - `223 / 223` required GameTests completed successfully

## Unresolved Risks

- the imported condition maps upstream Bukkit `LivingEntity#hasAI()` onto Fabric’s `Mob#isNoAi()` surface, so non-mob living entities still follow the local compatibility fallback instead of a broader upstream-equivalent API
- coverage proves the exact imported AI condition forms and live cow behavior, not wider future AI expression or effect families

## Merge Notes

- likely conflict surface:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/gametest/resources/fabric.mod.json`
- lower-risk but possible conflicts:
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricAIGameTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/AISyntaxTest.java`
