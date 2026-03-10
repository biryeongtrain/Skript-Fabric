# Surface F2 Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests and effect fixtures
- minimal bootstrap registration wiring needed to expose the imported effects

## Latest Slice

- added a 10-class player/server feedback effect bundle:
  - `EffActionBar`
  - `EffBroadcast`
  - `EffKick`
  - `EffMessage`
  - `EffOp`
  - `EffPlaySound`
  - `EffResetTitle`
  - `EffSendResourcePack`
  - `EffSendTitle`
  - `EffStopSound`
- added package-local runtime glue in `EffectRuntimeSupport`
- wired the new effects through `SkriptFabricBootstrap`
- added isolated parsing coverage plus runtime script-loader fixture coverage for the new syntax
- left `EffTooltip`, `EffHidePlayerFromServerList`, and `EffPlayerInfoVisibility` out of this slice because they still depend on item-meta or server-list event surfaces that are not present in the current Fabric runtime

## Verification

- `./gradlew test --tests ch.njol.skript.effects.EffectPresentationCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.PresentationEffectBindingTest --rerun-tasks`
  - passed

## Commands

- `./gradlew test --tests ch.njol.skript.effects.EffectPresentationCompatibilityTest --tests org.skriptlang.skript.fabric.runtime.PresentationEffectBindingTest --rerun-tasks`
  - passed

## Next Lead

- next owned closure should stay in nearby player/server presentation effects only if they can ride the current packet/runtime surface without introducing shared server-list or item-meta bridges

## Merge Notes

- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/effects/*.java`
  - `src/test/java/ch/njol/skript/effects/*.java`
  - `src/test/java/org/skriptlang/skript/fabric/runtime/*.java`
