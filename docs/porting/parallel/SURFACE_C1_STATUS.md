# Surface C1 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/conditions/**`
- tightly matching tests

## Latest Slice

- added a 15-class local-compatibility entity/player state condition bundle under `ch/njol/skript/conditions`:
  - `CondCanFly`
  - `CondCanPickUpItems`
  - `CondHasScoreboardTag`
  - `CondIsBlocking`
  - `CondIsClimbing`
  - `CondIsFlying`
  - `CondIsGliding`
  - `CondIsHandRaised`
  - `CondIsLeftHanded`
  - `CondIsOnGround`
  - `CondIsRiptiding`
  - `CondIsSleeping`
  - `CondIsSneaking`
  - `CondIsSwimming`
  - `CondIsTamed`
- kept upstream doc annotations where applicable, but rewrote the implementations onto the local `net.minecraft` / Fabric runtime types because Bukkit entity APIs are not on this compile classpath
- added `EntityStateConditionCompatibilityTest` for instantiation plus init/toString coverage of the imported condition set

## Verification

- `./gradlew compileJava --rerun-tasks`
  - first run failed: upstream verbatim Bukkit imports (`org.bukkit.entity.*`, `org.bukkit.metadata.*`, `org.bukkit.inventory.*`) are unavailable in this tree
  - second run passed after rewriting the kept conditions to local NMS/Fabric types
- `./gradlew test --tests ch.njol.skript.conditions.EntityStateConditionCompatibilityTest --rerun-tasks`
  - first run failed on a registry assertion after `@AfterEach` clearing removed one-time static registrations
  - second run passed after trimming that assertion

## Blockers

- `CondIsPathfinding` remains out: upstream implementation depends on Paper `Pathfinder`
- `CondHasMetadata` remains out: no clean local metadata-holder equivalent surfaced inside lane ownership
- `CondIsJumping` remains out: upstream Bukkit-only entity API does not map cleanly to a stable local compatibility check in this slice
- `CondIsPersistent` remains out: upstream entity/block persistence semantics cross into unsupported local block/runtime shapes for this lane
- `CondIsRiding` remains out: local `EntityData` uses Fabric/NMS entities, not upstream Bukkit vehicle checks

## Next Lead

- continue the nearby low-dependency state-property cluster that can be expressed directly against local `net.minecraft` entities without pulling in Bukkit wrappers or Paper-only APIs

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/conditions/**`
  - `src/test/java/ch/njol/skript/conditions/**`
