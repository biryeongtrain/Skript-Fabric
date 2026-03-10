## Landed

- Added primary entity/property expressions that map cleanly onto local Mojang/Fabric types:
  - `ExprActiveItem`
  - `ExprAge`
  - `ExprAllayJukebox`
  - `ExprArrowsStuck`
  - `ExprBeehiveFlower`
  - `ExprBeehiveHoneyLevel`
  - `ExprBreakSpeed`
  - `ExprCreeperMaxFuseTicks`
  - `ExprDomestication`
  - `ExprDuplicateCooldown`
  - `ExprEntityItemUseTime`
  - `ExprEyeLocation`
  - `ExprFoodLevel`
- Added clean fallback / same-scope closure:
  - `ExprEntityOwner`
  - `ExprEntitySize`
  - `ExprExperienceCooldown`
  - `ExprGlidingState`
  - `ExprHealth`
  - `ExprNoDamageTime`
  - `ExprPortalCooldown`
  - `ExprRemainingAir`

## Blockers

- `ExprFacing`
  - Blocked in this lane by the absence of a real local `Direction` runtime/class registration. The only `Direction` in-tree is a test-local enum, so porting the upstream expression would require widening scope into shared type infrastructure.
- `ExprAppliedEffect`
  - Blocked by missing local dedicated beacon-effect event syntax/handle. The current Fabric runtime exposes generic potion-effect causes, but there is no direct upstream-equivalent beacon-effect event value to bind this expression to without changing semantics.
- `ExprDomestication`
  - Partial compat landed. Current Mojang surface exposes `getMaxTemper()` but no corresponding setter, so `max domestication level` is readable but not changeable in this lane.
