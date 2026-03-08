# Lane C Status

Last updated: 2026-03-08

## Scope

- exact upstream sprinting syntax import for the active Fabric runtime

## Owned Files

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsSprinting.java`
- `src/main/java/org/skriptlang/skript/bukkit/base/effects/EffSprinting.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/SprintingSyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricSprintingGameTest.java`
- `src/gametest/resources/skript/gametest/condition/sprinting_player_names_player.sk`
- `src/gametest/resources/skript/gametest/effect/make_player_start_sprinting_names_player.sk`
- `src/gametest/resources/skript/gametest/effect/make_player_stop_sprinting_names_player.sk`
- `src/gametest/resources/fabric.mod.json`

## Goal For Next Session

- merge this lane branch cleanly into the main porting line without renaming the imported sprinting forms

## Work Log

- imported the missing upstream sprinting condition as a player-only compatibility class:
  - `CondIsSprinting` now accepts `%players% (is|are) sprinting` and `%players% (isn't|is not|aren't|are not) sprinting`
  - runtime evaluation uses the Mojang `ServerPlayer.isSprinting()` flag
- imported the missing upstream sprinting effect as a player-only compatibility class:
  - `EffSprinting` now accepts the exact upstream effect forms
  - positive forms:
    - `make %players% (start sprinting|sprint)`
    - `force %players% to (start sprinting|sprint)`
  - negative forms:
    - `make %players% (stop sprinting|not sprint)`
    - `force %players% to (stop sprinting|not sprint)`
  - runtime execution uses `ServerPlayer.setSprinting(...)`
- registered both syntax families explicitly in `SkriptFabricBootstrap` so they are live in the active Fabric runtime
- added focused parser/bootstrap coverage proving the exact upstream surface parses on the active runtime bootstrap
- added real `.sk` GameTest coverage proving:
  - `event-player is sprinting` gates trigger execution in a live script
  - `force event-player to start sprinting` sets the live Mojang sprinting flag
  - `force event-player to not sprint` clears the live Mojang sprinting flag
- did not claim parity complete

## Files Changed

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsSprinting.java`
- `src/main/java/org/skriptlang/skript/bukkit/base/effects/EffSprinting.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/SprintingSyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricSprintingGameTest.java`
- `src/gametest/resources/skript/gametest/condition/sprinting_player_names_player.sk`
- `src/gametest/resources/skript/gametest/effect/make_player_start_sprinting_names_player.sk`
- `src/gametest/resources/skript/gametest/effect/make_player_stop_sprinting_names_player.sk`
- `src/gametest/resources/fabric.mod.json`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.SprintingSyntaxTest --rerun-tasks`
  - passed
  - verified exact upstream sprinting condition/effect forms through the active Fabric bootstrap
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - verified the full Fabric GameTest suite with the new live sprinting `.sk` coverage green on the resource-loader path
  - suite result: `223 / 223` required tests passed

## Unresolved Risks

- sprinting on a stationary player still inherits Mojang’s runtime quirks that upstream documents; this slice preserves syntax and flag mutation, not a deeper normalization layer over vanilla sprint behavior
- live coverage currently proves `start sprinting`, `not sprint`, and the condition form; the parser/bootstrap unit test covers the remaining exact synonym forms

## Merge Notes

- likely conflict surface:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/gametest/resources/fabric.mod.json`
- added real-script fixtures:
  - `src/gametest/resources/skript/gametest/condition/sprinting_player_names_player.sk`
  - `src/gametest/resources/skript/gametest/effect/make_player_start_sprinting_names_player.sk`
  - `src/gametest/resources/skript/gametest/effect/make_player_stop_sprinting_names_player.sk`
- lane-local status update:
  - `docs/porting/parallel/LANE_C_STATUS.md`
