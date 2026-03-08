# Lane D Status

Last updated: 2026-03-09

## Scope

- `ch/njol/skript/lang/function` compatibility only
- kept strictly inside the allowed lang/function closure
- no syntax-import or non-lane doc changes

### 2026-03-08 Upstream Syntax Import: Glowing Property

- imported the exact upstream user-visible property expression for entity glowing state
  - patterns: `[the] glowing of %entities%`, `%entities%'[s] glowing`
- added `ExprGlowing` under `org/skriptlang/skript/bukkit/base/expressions`
- registered the expression in `SkriptFabricAdditionalSyntax.register(...)`
- added a focused parser regression `GlowingSyntaxTest` that verifies the exact set-form parses to `EffChange`:
  - `set glowing of event-entity to true`

Changed files:

- `src/main/java/org/skriptlang/skript/bukkit/base/expressions/ExprGlowing.java`
- `src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java`
- `src/test/java/org/skriptlang/skript/fabric/runtime/GlowingSyntaxTest.java`

Exact commands and results:

```
./scripts/gradle-automation.sh test --tests org.skriptlang.skript.fabric.runtime.SilentSyntaxTest --rerun-tasks
  # sanity check harness — passed

./scripts/gradle-automation.sh test --tests "org.skriptlang.skript.fabric.runtime.*" --rerun-tasks
  # runtime test package — passed

./scripts/gradle-automation.sh runGameTest --rerun-tasks
  # GameTests — passed (230/230 scheduled)

./scripts/gradle-automation.sh build --rerun-tasks
  # full build — passed
```

Merge notes:

- slice is self-contained and limited to expression registration and a parser-only unit; no runtime behavior changes beyond the new property setter/getter
- zero conflicts expected; files are lane-local and additive

## Owned Files

- `src/main/java/ch/njol/skript/lang/function/Parameter.java`
- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionDefaultKeyedParameterCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Goal For This Slice

- find one remaining upstream-backed mismatch in function default-parameter behavior
- prioritize keyed/plural default execution semantics
- land one narrow fix with a focused regression

## What Landed

- upstream-backed keyed default semantics for plural parameters:
  - Only zip to keyed pairs when the default expression yields exactly one value (key "1").
  - When the default yields multiple values, they remain un-keyed (raw values), matching upstream `Function.execute(...)` logic.
- implemented this behavior in `Function.execute(...)` by distinguishing between provided arguments and defaulted ones for keyed parameters.
- added a focused regression proving the previous implementation incorrectly zipped all defaults for keyed plural parameters.

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Function.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionDefaultKeyedParameterCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Counts Changed

- source files changed: `1`
- test files changed: `1`
- test methods added: `2`
- canonical docs changed: `0`

## Exact Commands And Results

- `git checkout -b codex/lane-d-keyed-defaults`
  - created lane branch
- `rg --files src/main/java/ch/njol/skript/lang/function src/test/java/ch/njol/skript/lang/function docs/porting/parallel | sort`
  - enumerated in-scope files
- upstream reference for behavior: reviewed `Function.execute(...)` in baseline `master` (comment references PR-8135) and noted keyed-default special-casing
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest.keyedPluralDefaultWithMultipleValuesRemainsUnkeyed --rerun-tasks`
  - failed before fix (local code zipped defaults to KeyedValue[])
- edited `src/main/java/ch/njol/skript/lang/function/Function.java` to mirror upstream keyed-default handling
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionDefaultKeyedParameterCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests 'ch.njol.skript.lang.function.*' --rerun-tasks`
  - passed

## Verification

- required function compatibility suite passed:
  - `FunctionCoreCompatibilityTest`
  - `FunctionImplementationCompatibilityTest`
  - `FunctionCallCompatibilityTest`
  - new: `FunctionDefaultKeyedParameterCompatibilityTest`

## Unresolved Risks

- broader upstream function-package deltas remain out of scope
- keyed/default behavior may interact with other call sites not exercised here; additional upstream-driven reproductions should be added as they surface
