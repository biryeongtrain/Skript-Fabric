# Lane C Status

Last updated: 2026-03-09

## Scope

- effects import on the existing Fabric runtime
- allowed files only under `src/main/java/org/skriptlang/skript/**/effects/**`, `src/main/java/org/skriptlang/skript/fabric/runtime/**`, matching effect tests, matching `.sk` fixtures

## Latest Slice

- imported the exact upstream `EffMakeAdultOrBaby` syntax family on Fabric
- `SkriptFabricAdditionalEffects` now registers the upstream forms exactly:
  - `make %livingentities% [a[n]] (:adult|baby|child)`
  - `force %livingentities% to be[come] a[n] (:adult|baby|child)`
- `EffMakeAdultOrBaby` now keys `adult` off the upstream parse tag instead of local matched-pattern numbering
- added focused runtime fixture coverage for the second upstream form with `child` alias:
  - `src/gametest/resources/skript/gametest/effect/force_event_entity_to_become_child_marks_block.sk`
- tightened effect parsing/binding coverage so the new upstream form must load as `EffMakeAdultOrBaby` and bind baby mode

## Verification

- command:
  - `./gradlew -q test --no-daemon --console plain --tests org.skriptlang.skript.fabric.runtime.EffectSyntaxParsingTest --tests org.skriptlang.skript.fabric.runtime.EffectBindingTest --rerun-tasks`
- result:
  - passed

## Next Lead

- compare one remaining registered effect family in `SkriptFabricAdditionalEffects` against the upstream snapshot and prefer exact pattern imports that need no backend work

## Merge Notes

- likely conflict surface:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalEffects.java`
  - `src/main/java/org/skriptlang/skript/bukkit/breeding/elements/EffMakeAdultOrBaby.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/EffectSyntaxParsingTest.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/EffectBindingTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
