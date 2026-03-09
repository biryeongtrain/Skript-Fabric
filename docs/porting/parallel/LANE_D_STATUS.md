# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed namespace-fallback mismatch in `DynamicFunctionReference.parseFunction(...)` / `resolveFunction(...)`
- local behavior previously treated an unresolved `from missing.sk` suffix as a real source hint, resolving a global function while retaining the bogus script name in the string form
- upstream first validates the script source and only keeps the `from ...` namespace when that script actually resolves; otherwise it falls back to the global function without preserving the invalid suffix

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/DynamicFunctionReference.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in overload resolution or keyed/default execution semantics that stays inside `lang/function`

## Merge Notes

- low-conflict slice limited to `DynamicFunctionReference.java`, one focused regression, and this lane file
