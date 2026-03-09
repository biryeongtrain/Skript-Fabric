# Lane E Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- `src/main/java/ch/njol/skript/conditions/**`

## Latest Slice

- kept the low-dependency text/numeric cluster moving and closed the adjacent character/codepoint bundle
- restored `ExprCharacters`, `ExprCodepoint`, and `ExprCharacterFromCodepoint`
- kept `ExprSubstring` and `ExprNumberOfCharacters` in the same compatibility slice and expanded the shared expression test around the full text-character surface
- ported `ExprCharacters` without adding a new `commons-lang` dependency by using local array compaction instead of upstream `ArrayUtils.reverse(...)`

## Verification

- `./gradlew test --tests ch.njol.skript.expressions.ExpressionTextCollectionCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- next importable Lane E bundle is still the nearby low-dependency expression/condition surface that avoids aliases or inventory/container crossings; `CondContains` still looks worse than adjacent bundles because it pulls that wider surface back into scope, so the next low-risk follow-up is another standalone text/numeric helper cluster before container-aware conditions

## Merge Notes

- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/ExprCharacters.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCodepoint.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCharacterFromCodepoint.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionTextCollectionCompatibilityTest.java`
