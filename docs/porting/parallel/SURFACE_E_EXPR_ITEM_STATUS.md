# Surface E Expr Item Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- tightly matching tests for the item/block expression closure

## Latest Slice

- landed a 10-class upstream-backed item/block expression closure adapted onto the local Fabric compat surface:
  - `ExprBlockHardness`
  - `ExprBookAuthor`
  - `ExprBookPages`
  - `ExprBookTitle`
  - `ExprBrushableItem`
  - `ExprCharges`
  - `ExprCustomModelData`
  - `ExprDamagedItem`
  - `ExprDurability`
  - `ExprEgg`
- restored a local `expressions/base/EventValueExpression` to support event-backed imports inside the current expression lane
- added `ExpressionItemCompatibilityTest` covering stack-backed book, damage, durability, custom-model-data, and hardness behavior plus import instantiation checks for the world/event-backed expressions

## Verification

- `./gradlew -q compileJava --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionItemCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- if Lane E keeps pushing item expressions, the next nearby primaries are the still-open anvil and armor expressions, but they now need non-owned compat decisions around anvil inventory wrappers and richer slot/itemtype mutation support before they can land cleanly

## Merge Notes

- exact commands run:
  - `./gradlew -q compileJava --rerun-tasks`
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionItemCompatibilityTest --rerun-tasks`
- likely conflicts:
  - `src/main/java/ch/njol/skript/expressions/base/EventValueExpression.java`
  - `src/main/java/ch/njol/skript/expressions/ExprBookPages.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCustomModelData.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDurability.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionItemCompatibilityTest.java`
