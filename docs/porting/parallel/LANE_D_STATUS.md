# Lane D Status

Last condensed: 2026-03-11
Last verified slice date: 2026-03-11

Historical extra lane; not part of the default A/B/C workflow.

## Scope

- `lang/function` compatibility
- one narrow default-parameter/runtime slice

## Latest Slice

- Restored keyed plural default behavior in `Function.execute(...)`:
  - single-value defaults still zip to keyed pairs
  - multi-value defaults remain unkeyed
- This now matches upstream behavior for keyed plural parameters with default expressions.

## Regression Added

- `FunctionDefaultKeyedParameterCompatibilityTest`

## Verification

- `./gradlew test --tests 'ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest' --tests 'ch.njol.skript.lang.function.*' --rerun-tasks`
- result: passed

## Remaining Risk

- broader function-package deltas remain out of scope
