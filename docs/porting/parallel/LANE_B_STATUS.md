# Lane B Status

Last updated: 2026-03-09

## Scope

- `src/main/java/org/skriptlang/skript/**/conditions/**`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- matching condition runtime tests
- matching gametest `.sk` resources

## Latest Slice

- primary blocked: the upstream potion-condition family depends on dedicated `%potioneffecttypes%` / tighter potion syntax classes that are not registered on the current Fabric runtime, and adding them would spill into expression/type ownership
- fallback landed: imported the exact upstream `responsive|unresponsive` condition form on the existing interaction backend
- `CondIsResponsive` now derives responsive mode from the upstream `:unresponsive` parse tag instead of local matched-pattern counting
- `SkriptFabricBootstrap` now registers the exact upstream-shaped responsive condition patterns with a shared tagged branch

## Verification

- `./gradlew test --tests org.skriptlang.skript.fabric.runtime.ResponsiveSyntaxTest --tests org.skriptlang.skript.fabric.runtime.ConditionBindingTest --rerun-tasks`

## Next Lead

- if condition scope stays open, revisit the potion family only after dedicated potion-effect-type syntax registration exists in-lane or is handed off by the coordinator

## Merge Notes

- likely conflict surface: `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
- runtime additions are isolated to the responsive interaction condition plus one new condition fixture and two focused runtime tests
