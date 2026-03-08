# Lane B Status

Last updated: 2026-03-08

## Scope

- upstream syntax-import slices assigned to Lane B
- exact user-visible condition/effect forms on the active Fabric runtime
- focused parser/bootstrap tests and live `.sk` GameTest coverage for those slices

## Owned Files

- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/**`
- `src/test/java/org/skriptlang/skript/fabric/runtime/**`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/**`
- `src/gametest/resources/skript/gametest/**`

## Goal For Next Session

- Wait for coordinator merge or the next upstream syntax-import assignment; this lane’s burning-condition slice is implemented, verified, and committed.

## Work Log

### Latest Slice

- imported the exact upstream `CondIsBurning` syntax family into the active Fabric runtime instead of renaming or collapsing aliases
- upstream target confirmed from `/tmp/skript-upstream-ueogiz/src/main/java/ch/njol/skript/conditions/CondIsBurning.java`
  - exact upstream property aliases: `burning`, `ignited`, `on fire`
  - upstream semantics: true when the entity has active fire ticks
- added a local Fabric runtime condition at `org/skriptlang/skript/bukkit/base/conditions/CondIsBurning.java`
  - follows the current local condition pattern instead of upstream static self-registration
  - evaluates against Mojang `Entity.isOnFire()`
- registered the condition in `SkriptFabricBootstrap` with the exact upstream visible forms:
  - `%entities% (is|are) (burning|ignited|on fire)`
  - `%entities% (isn't|is not|aren't|are not) (burning|ignited|on fire)`
- added targeted parser/bootstrap unit coverage proving the exact aliases parse to the new condition, including the negative `not on fire` path
- added real `.sk` + GameTest runtime coverage for all three upstream aliases
  - `event-entity is burning`
  - `event-entity is ignited`
  - `event-entity is on fire`
  - live runtime checks set Mojang fire ticks on a cow and verify the condition executes through the resource loader path

## Files Changed

- `src/main/java/org/skriptlang/skript/bukkit/base/conditions/CondIsBurning.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/BurningSyntaxTest.java`
- `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBurningGameTest.java`
- `src/gametest/resources/skript/gametest/condition/burning_entity_names_entity.sk`
- `src/gametest/resources/skript/gametest/condition/ignited_entity_names_entity.sk`
- `src/gametest/resources/skript/gametest/condition/on_fire_entity_names_entity.sk`
- `docs/porting/parallel/LANE_B_STATUS.md`

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.BurningSyntaxTest --rerun-tasks`
  - passed
  - `build/test-results/test/TEST-org.skriptlang.skript.fabric.runtime.BurningSyntaxTest.xml` reports `2` tests, `0` failures, `0` errors
- `./gradlew runGameTest --rerun-tasks`
  - passed
  - `build/run/gameTest/logs/latest.log` reports `All 220 required tests passed :)`

## Exact Syntax Exercised

- unit parse forms:
  - `event-entity is burning`
  - `event-entity is ignited`
  - `event-entity is on fire`
  - `event-entity is not on fire`
- live `.sk` forms:
  - `event-entity is burning`
  - `event-entity is ignited`
  - `event-entity is on fire`

## Unresolved Risks

- this slice only imports the upstream burning condition family; any adjacent fire-related effects or expressions still depend on separate assignments
- local semantics use Mojang `Entity.isOnFire()`, which matches the current active runtime API and should track upstream fire-tick intent, but no additional parity work was done around edge cases such as transient fire immunity windows

## Merge Notes

- highest conflict risk is `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java` because recent syntax-import slices also add registrations there
- lower conflict risk is the shared runtime test area under `src/test/java/org/skriptlang/skript/fabric/runtime` and the shared GameTest tree under `src/gametest/java/kim/biryeong/skriptFabricPort/gametest`
- the new burning slice is otherwise self-contained
