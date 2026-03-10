# M6 Status

## Scope

- `expressions/**`
- minimal helper glue inside the same scope
- narrow tests for the assigned expression bundle

## Assigned Targets

- primary script/parser/collection bundle `15`:
  - `ExprArgument`
  - `ExprCaughtErrors`
  - `ExprConfig`
  - `ExprDequeuedQueue`
  - `ExprElement`
  - `ExprEventExpression`
  - `ExprEvtInitiator`
  - `ExprFilter`
  - `ExprFunction`
  - `ExprKeyed`
  - `ExprLoopIteration`
  - `ExprLoopValue`
  - `ExprNode`
  - `ExprNumbers`
  - `ExprParse`
- fallback parser/queue/script follow-up `16`:
  - `ExprParseError`
  - `ExprQueue`
  - `ExprQueueStartEnd`
  - `ExprRecursive`
  - `ExprRepeat`
  - `ExprResult`
  - `ExprScript`
  - `ExprScripts`
  - `ExprScriptsOld`
  - `ExprSets`
  - `ExprRawString`
  - `ExprRound`
  - `ExprPercent`
  - `ExprPlain`
  - `ExprRawString` follow-up if parser glue blocks
  - `ExprKeyed` parity follow-up if still needed

## Landed Classes

- landed `14`:
  - `ExprCaughtErrors`
  - `ExprDequeuedQueue`
  - `ExprEventExpression`
  - `ExprFilter`
  - `ExprFunction`
  - `ExprKeyed`
  - `ExprLoopIteration`
  - `ExprPercent`
  - `ExprQueue`
  - `ExprQueueStartEnd`
  - `ExprRecursive`
  - `ExprRepeat`
  - `ExprRound`
  - `ExprSets`

## Runtime-Eligible Classes

- compile-clean in this fork: same `14` landed classes
- deferred from the requested seed set:
  - `ExprEvtInitiator` is still Bukkit-only (`InventoryMoveItemEvent` / `Inventory`)
  - `ExprParseError` still depends on missing `ExprParse.lastError`

## Bootstrap Registrations Needed

- coordinator to decide after merge
- no bootstrap file edits made in this lane per instruction

## Targeted Tests

- added `src/test/java/ch/njol/skript/expressions/ExpressionMixedRuntimeM6CompatibilityTest.java`
- covers:
  - parse registration sweep for queue/keyed/function/recursive/repeat/round/caught-errors surfaces
  - direct behavior for `ExprCaughtErrors`, `ExprPercent`, `ExprRepeat`, `ExprRound`, `ExprQueue`, `ExprDequeuedQueue`, `ExprQueueStartEnd`, `ExprEventExpression`, `ExprLoopIteration`, `ExprKeyed`, `ExprRecursive`, `ExprSets`, `ExprFunction`
- verification:
  - `./gradlew compileJava --no-daemon`
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionMixedRuntimeM6CompatibilityTest --no-daemon`

## Blockers

- `ExprEvtInitiator`: not portable without a Fabric-side event/inventory compat surface
- `ExprParseError`: blocked on missing `ExprParse`
- `ExprFilter` and `ExprSets` compile and land cleanly, but broad parser-surface assertions were kept narrow because `%*classinfo%` matching is still greedy around unrelated literals in this fork

## Merge Note

- landed the local-feasible import-only subset and explicitly dropped the two blocked files instead of carrying dead runtime imports
