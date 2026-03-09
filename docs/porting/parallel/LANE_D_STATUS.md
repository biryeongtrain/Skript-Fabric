# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` runtime and default-parameter parity only
- touched no canonical docs and no files outside lane ownership

## Latest Slice

- fixed one upstream-backed default-parameter mismatch in `Parameter.newInstance(...)`
- local behavior treated whitespace-only default text as if no default was provided, so invalid defaults like `x: number =   ` slipped through as required parameters
- upstream still parses the supplied default text and rejects blank defaults when they do not form a valid expression

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Parameter.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`

## Verification

- upstream reference: compared local `src/main/java/ch/njol/skript/lang/function/Parameter.java` against `/tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Parameter.java`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- continue upstream diff review for one remaining mergeable mismatch in keyed/default execution semantics or namespace fallback that stays inside `lang/function`

## Merge Notes

- low-conflict slice limited to `Parameter.java`, one focused regression, and this lane file
