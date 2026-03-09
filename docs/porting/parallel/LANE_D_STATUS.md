# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one dynamic local-function unload edge in `DynamicFunctionReference.resolveFunction(...)`
- local string-resolved local references kept only the script name, so they could not retain the upstream-style `Script` validity guard and stayed callable as long as the function object itself was still reachable
- `Functions.registerSignature(...)` now records the active script object for the matching namespace, and `DynamicFunctionReference.resolveFunction(...)` reattaches that tracked `Script` when resolving `name ... from script.sk`
- added one narrow regression proving a string-resolved local dynamic reference becomes invalid and stops executing after its tracked script is invalidated

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `src/main/java/ch/njol/skript/lang/function/Functions.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Verification

- upstream reference: compared local `DynamicFunctionReference` source-tracking/unload behavior against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`; local compatibility needed the same script-backed validity path for string-resolved local references
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable primary mismatch in overload selection or default-parameter execution semantics; if none remain, keep narrowing namespace/dynamic-reference edges inside `lang/function`

## Merge Notes

- low-conflict slice limited to `DynamicFunctionReference.java`, `Functions.java`, one focused regression in `FunctionCallCompatibilityTest`, and this lane file
