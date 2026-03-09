# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- primary diff review did not expose a new mergeable overload/default-parameter mismatch beyond the already-closed slices in this lane
- fixed one fallback dynamic-reference unload edge in `DynamicFunctionReference(Function<?>)`
- locally, a resolved local function wrapped directly into `new DynamicFunctionReference<>(function)` dropped the tracked source `Script`, so it stayed valid after script invalidation unlike upstream
- `DynamicFunctionReference(Function<?>)` now reattaches the registered source script through `Functions.getScript(signature.namespace())`
- added one narrow regression proving a direct-function local dynamic reference becomes invalid and stops executing after its tracked script is invalidated

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Verification

- upstream reference: compared local constructor-time source-tracking/unload behavior against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`; upstream resolves the source `Script` even for already-resolved function objects, while local only did so for string-resolved references
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable primary mismatch in overload selection or default-parameter execution semantics; if none remain, keep narrowing namespace/dynamic-reference unload edges inside `lang/function`

## Merge Notes

- low-conflict slice limited to `DynamicFunctionReference.java`, one focused regression in `FunctionCallCompatibilityTest`, and this lane file
