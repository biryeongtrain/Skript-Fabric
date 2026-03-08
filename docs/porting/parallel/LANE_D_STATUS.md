# Lane D Status

Last updated: 2026-03-08

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
- `docs/porting/parallel/LANE_D_STATUS.md`

## Goal For This Slice

- compare local function lookup behavior against `/tmp/skript-upstream-e6ec744-2`
- restore one contained observable gap in local-vs-global function resolution
- restore one contained optional/default-parameter parity gap in legacy function defaults
- prove the gaps with focused compatibility regressions

## What Landed

- restored upstream lookup precedence where local function/signature matches win before considering global candidates
- fixed `FunctionRegistry.getSignature(...)` and `FunctionRegistry.getFunction(...)` so globals are fallback-only when a script namespace already has a matching result
- added a registry-level regression proving a local compatible overload no longer becomes ambiguous because of a broader global candidate
- added a call-binding regression proving `FunctionReference.validateFunction(true)` now binds to the local signature instead of failing on mixed local/global ambiguity
- restored `Parameter.newInstance(...)` so legacy function defaults are parsed as expressions instead of literal-only `Classes.parse(...)` values
- added a regression proving a registered integer expression can be used as a function parameter default

## Files Changed

- `src/main/java/ch/njol/skript/lang/function/Parameter.java`
- `src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`
- `src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
- `docs/porting/parallel/LANE_D_STATUS.md`

## Counts Changed

- source files changed: `2`
- test files changed: `2`
- test methods added: `3`
- canonical docs changed: `0`

## Exact Commands And Results

- `git status --short --branch`
  - confirmed branch `codex/lane-d-20260308m`
- `git rev-parse HEAD`
  - confirmed start point `7b27f6bc3572e37d29756fb2d99507fa8c2e979a`
- `rg --files src/main/java/ch/njol/skript/lang/function src/test/java/ch/njol/skript/lang/function docs/porting/parallel | sort`
  - enumerated the in-scope function files and lane docs
- `git diff --no-index -- src/main/java/ch/njol/skript/lang/function/Functions.java /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Functions.java`
  - reviewed upstream delta for function facade behavior
- `git diff --no-index -- src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
  - identified the local/global fallback-order mismatch as a contained target
- `git diff --no-index -- src/main/java/ch/njol/skript/lang/function/FunctionReference.java /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/FunctionReference.java`
  - confirmed the mismatch was observable through call binding
- `sed -n '1,260p' src/main/java/ch/njol/skript/lang/function/FunctionRegistry.java`
  - reviewed current registry lookup implementation
- `sed -n '1,260p' src/main/java/ch/njol/skript/lang/function/FunctionReference.java`
  - reviewed call-site validation flow
- `sed -n '1,260p' src/main/java/ch/njol/skript/lang/function/Parameter.java`
  - reviewed optional/default parameter behavior for nearby context
- `sed -n '1,320p' src/main/java/ch/njol/skript/lang/function/Signature.java`
  - reviewed min/max parameter behavior for nearby context
- `sed -n '1,260p' src/test/java/ch/njol/skript/lang/function/FunctionCoreCompatibilityTest.java`
  - reviewed existing core compatibility coverage
- `sed -n '1,320p' src/test/java/ch/njol/skript/lang/function/FunctionImplementationCompatibilityTest.java`
  - reviewed implementation compatibility coverage
- `sed -n '1,320p' src/test/java/ch/njol/skript/lang/function/FunctionCallCompatibilityTest.java`
  - reviewed call compatibility coverage
- `sed -n '1,260p' src/main/java/ch/njol/skript/lang/function/Function.java`
  - reviewed runtime parameter/default handling for context
- `git diff --no-index -- src/main/java/ch/njol/skript/lang/function/Parameter.java /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Parameter.java`
  - compared adjacent default-argument behavior
- `git diff --no-index -- src/main/java/ch/njol/skript/lang/function/Signature.java /tmp/skript-upstream-e6ec744-2/src/main/java/ch/njol/skript/lang/function/Signature.java`
  - compared adjacent signature/min-arity behavior
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest.parameterParsesRegisteredExpressionAsDefaultValue`
  - failed before the fix; `Parameter.newInstance(...)` rejected a registered integer expression default
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest.functionRegistryPrefersLocalMatchBeforeGlobalCandidates --rerun-tasks`
  - failed before the fix; the new regression reproduced the ambiguity bug
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest.functionReferencePrefersLocalSignatureOverCompatibleGlobalCandidate --rerun-tasks`
  - failed before the fix; local call binding was blocked by the same ambiguity
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest.parameterParsesRegisteredExpressionAsDefaultValue`
  - passed after parsing function defaults through `SkriptParser`
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest.functionRegistryPrefersLocalMatchBeforeGlobalCandidates --rerun-tasks`
  - passed after the registry fix
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest.functionReferencePrefersLocalSignatureOverCompatibleGlobalCandidate --rerun-tasks`
  - passed after the registry fix
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.lang.function.FunctionCoreCompatibilityTest --tests ch.njol.skript.lang.function.FunctionImplementationCompatibilityTest --tests ch.njol.skript.lang.function.FunctionCallCompatibilityTest`
  - passed after the default-expression fix

## Verification

- required compatibility suite passed:
  - `FunctionCoreCompatibilityTest`
  - `FunctionImplementationCompatibilityTest`
  - `FunctionCallCompatibilityTest`

## Unresolved Risks

- this slice only restores local-first fallback behavior in the legacy registry APIs; broader upstream deltas in the function package remain
- this slice restores parsed default-expression handling for function parameters, but does not yet chase broader signature-parser parity such as duplicate-name validation
- verification is limited to the targeted compatibility tests above
