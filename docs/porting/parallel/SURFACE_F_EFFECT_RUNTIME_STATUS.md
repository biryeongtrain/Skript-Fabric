# Surface F Effect Runtime Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/effects/**`
- tightly matching tests under `src/test/java/ch/njol/skript/effects/**`

## Latest Slice

- restored 23 more Lane F effect surfaces inside `ch/njol/skript/effects`:
  - runtime-backed or partially runtime-backed:
    - `EffBan`
    - `EffBlockUpdate`
    - `EffBreakNaturally`
    - `EffCancelDrops` (`loot generate` item-drop closure only)
    - `EffCancelItemUse`
    - `EffCommand`
    - `EffLidState`
    - `EffLook`
    - `EffOpenBook`
    - `EffOpenInventory`
    - `EffPvP` (server-global PvP toggle in vanilla/Fabric)
    - `EffSendBlockChange`
    - `EffStopServer`
    - `EffTooltip`
    - `EffWardenDisturbance`
    - `EffWorldSave`
  - parser-restored with explicit blocker errors at init time:
    - `EffCancelCooldown`
    - `EffCancelEvent`
    - `EffHidePlayerFromServerList`
    - `EffLoadServerIcon`
    - `EffPlayerInfoVisibility`
    - `EffRing`
    - `EffWorldLoad`
- wired the bundle into `SkriptFabricBootstrap` so the restored effects are visible in the runtime bootstrap rather than test-only
- extended `EffectRuntimeClosureCompatibilityTest` to cover parser/binding for the imported server/effect closure bundle
- kept doc annotations from upstream on the restored classes where they existed

## Blockers

- `EffCancelCooldown`: the local Fabric runtime still has no `ScriptCommandEvent`/command-cooldown event surface to mutate
- `EffCancelEvent`: event cancellation is still missing a lane-local cancellable handle contract and bridge return path
- `EffHidePlayerFromServerList`, `EffLoadServerIcon`, `EffPlayerInfoVisibility`: no server-list ping/event/icon bridge exists in the local Fabric runtime
- `EffRing`: upstream depends on `Direction` plus bell-specific server hooks that are not present locally
- `EffWorldLoad`: dynamic world load/unload needs server storage/bootstrap hooks outside this effect slice
- `EffCancelDrops`: block-break/death xp/item cancellation still needs pre-drop Fabric event handles; only loot-table item cancellation landed here
- `EffPvP`: upstream was per-world, but vanilla/Fabric only exposes a server-global PvP toggle without a wider world-specific bridge
- `EffTooltip`: "additional tooltip" support is only approximated via tooltip component hiding because upstream `ItemFlag` parity is not present locally

## Verification

- `./gradlew isolatedEffectRuntimeClosureCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- the next high-value follow-on is a small cancellable Fabric event bridge so `EffCancelEvent` and the remaining drop-control cases can become runtime-backed instead of parser-only
- after that, the remaining server-list and world-load surfaces need dedicated non-effect runtime handles rather than more effect-only imports

## Merge Notes

- exact verification command that passed:
  - `./gradlew isolatedEffectRuntimeClosureCompatibilityTest --rerun-tasks`
- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricBootstrap.java`
  - `src/main/java/ch/njol/skript/effects/EffectRuntimeSupport.java`
  - `src/main/java/ch/njol/skript/effects/Eff*.java`
  - `src/test/java/ch/njol/skript/effects/*.java`
