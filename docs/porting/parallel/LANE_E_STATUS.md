# Lane E Status

Last condensed: 2026-03-11
Last verified slice date: 2026-03-08

Historical extra lane; not part of the default A/B/C workflow.

## Scope

- dependency-closure support around `HintManager`

## Latest Slice

- Restored upstream section-scope semantics in `HintManager.clearScope(level, true)`.
- Removed leaked section-frame behavior that left copied hints visible to later parsing scopes.

## Verification

- `./gradlew test --tests ch.njol.skript.lang.VariableCompatibilityTest`
- result: passed
