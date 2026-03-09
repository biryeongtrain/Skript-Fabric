# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one function namespace/runtime mismatch in `Functions.clearFunctions(...)`
- local behavior removed script namespaces from the legacy `Functions` maps but left the same signatures/functions registered in the compatibility `FunctionRegistry`
- that let cleared local functions continue to resolve through registry-backed lookups after unload, diverging from the expected post-clear state
- `Functions.clearFunctions(...)` now removes each cleared signature from `FunctionRegistry` before queuing cross-script call revalidation
- added one narrow regression proving `clearFunctions("script.sk")` leaves both signature and function retrieval as `NOT_REGISTERED`

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Functions.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Verification

- upstream reference: compared local `Functions.clearFunctions(...)` unload flow against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Functions.java`; local compatibility needed the same unload effect to reach `FunctionRegistry`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable primary mismatch in overload selection or default-parameter execution semantics; if none remain, keep narrowing namespace/dynamic-reference edges inside `lang/function`

## Merge Notes

- low-conflict slice limited to `Functions.java`, one focused regression in `FunctionCoreCompatibilityTest`, and this lane file
