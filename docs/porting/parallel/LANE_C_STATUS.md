# Lane C Status

Last updated: 2026-03-09

## Scope

- Variables/Classes/config/structures only
- prioritize classinfo/parser registry parity or deeper variable semantics
- find exactly one upstream-backed mismatch after literal-only `getPatternInfos` closure

## Owned Files

- `src/main/java/ch/njol/skript/variables/**`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/main/java/ch/njol/skript/config/**`
- `src/main/java/ch/njol/skript/structures/**`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Goal For This Session

- locate one concrete upstream-backed mismatch in `Classes`/`Variables`/`config`/`structures`
- land a narrow fix with a focused regression and verification commands

## Work Log

- compared local `StructOptions.OptionEntryData` with upstream `ch/njol/skript/structures/StructOptions#init` + `SectionNode#convertToEntries`
- mismatch found: upstream accepts `options:` entries with empty values such as `blank:`, but the local validator rejected those runtime/simple nodes as invalid because `OptionEntryData.canCreateWith(...)` required at least one character after `:`
- reproduced via `ScriptLoaderCompatibilityTest` with `options: blank:` and nested `blocks: nested:`
- applied minimal fix: `StructOptions` now accepts any non-empty key with `:` for option entries, matching upstream empty-string option loading

- compared local `Node.setKey(...)` / `SectionNode` lookup maintenance with upstream `ch/njol/skript/config/Node#rename` and `SectionNode#renamed`
- mismatch found: renaming a mapped child node updated the node key locally but left `SectionNode`'s case-insensitive lookup map stale, so `get(...)` still resolved the old key and missed the new one
- reproduced via `SectionNodeCompatibilityTest` by renaming an `EntryNode` after `add(...)`
- applied minimal fix: `Node.setKey(...)` now notifies the parent section, and `SectionNode` now refreshes the mapped key through a narrow `renamed(...)` helper like upstream
- compared local `Variables.setVariable(...)` with upstream `ch/njol/skript/variables/Variables#setVariable`
- mismatch found: upstream treats `setVariable("name::*", null, ...)` as list deletion, but the local flat-map bridge only removed the literal `name::*` key and left descendants intact
- reproduced via `VariablesCompatibilityTest` with a direct parent value plus nested descendants under `scores::*`
- applied minimal fix: route `name::*` + `null` through `removePrefix(...)`, which deletes descendants while preserving a direct parent value like upstream `VariablesMap#setVariable(...)`
- compared local `Classes.toString(Object[], boolean)` with upstream `ch/njol/skript/registrations/Classes#toString(Object[], boolean, ...)`
- mismatch found: upstream returns the null sentinel for empty arrays, while the local bridge returned an empty string
- applied minimal fix: empty `Object[]` stringification now delegates to `toString(null, StringMode.MESSAGE)`; added a focused compatibility regression
- compared local `Classes.toString(Object, StringMode)` with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream routes registered legacy parser types through parser-backed string rendering, while the local bridge always fell back to `Object.toString()` and array joins bypassed parser formatting too
- reproduced via `LegacyWrapperCompatibilityTest` with a registered legacy `Parser<LegacyValue>` whose message/debug rendering differs from `record` `toString()`
- applied minimal fix: `Classes.toString(...)` now uses registered legacy parsers for message/debug/variable-name rendering, and `Object[]` joins delegate each element through the same path
- compared local fallback `Classes.toString(Object, StringMode.VARIABLE_NAME)` with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream prefixes unparsed fallback values as `object:...`, while the local bridge returned raw `Object.toString()`
- applied minimal fix: variable-name fallback stringification now returns `object:` + value when no registered parser matches
- compared local `Classes.toString(Object, StringMode)` array handling with upstream `ch/njol/skript/registrations/Classes#toString(Object, StringMode, ...)`
- mismatch found: upstream formats object-typed arrays as bracketed element strings, while the local bridge fell through to Java array identity text
- applied minimal fix: `Classes.toString(Object, StringMode)` now detects arrays up front and recursively formats elements like upstream
- compared local `Classes.parseSimple(...)` with upstream `ch/njol/skript/registrations/Classes#parseSimple`
- mismatch found: upstream iterates sorted registered classinfos and therefore prefers the most specific compatible parser first, while the local bridge short-circuited through an exact base-type classinfo before later subtype parsers
- applied minimal fix: `Classes.parseSimple(...)` now follows sorted classinfo order for registered parsers, so subtype parsers win over broader base-type parsers like upstream
- compared local `Classes.clone(...)` with upstream `ch/njol/skript/registrations/Classes#clone(Object)`
- mismatch found: upstream stops after the registered `ClassInfo` cloner and otherwise returns the original value, while the local bridge fell through to reflective `Cloneable#clone()` and could duplicate values that upstream leaves unchanged
- applied minimal fix: `Classes.clone(...)` now matches upstream by removing the reflective `Cloneable` fallback; added a focused compatibility regression
- compared local `Classes.parse(...)` converter fallback with upstream `ch/njol/skript/registrations/Classes#parse`
- mismatch found: upstream skips converters flagged `CONVERTER_NO_COMMAND_ARGUMENTS` in `ParseContext.COMMAND` and `ParseContext.PARSE`, while the local bridge still applied those converters in all contexts
- applied minimal fix: restored upstream parse-context gating for flagged converters and reintroduced the missing compatibility enum values needed to express those contexts
- compared local primitive handling in `Classes.parseSimple(...)` with upstream `ch/njol/skript/registrations/Classes#parseSimple`
- mismatch found: upstream lets registered `ClassInfo` parsers for primitive-backed types run before any fallback coercion, while the local bridge short-circuited `String`/number/boolean parsing first and skipped registered parsers entirely
- applied minimal fix: `Classes.parseSimple(...)` now consults registered classinfos before the primitive fallback, so custom `String`/numeric parsers behave like upstream
- compared local parser-registry `org.skriptlang.skript.util.Priority` ordering with upstream `Priority` / `PriorityImpl`
- mismatch found: upstream preserves transitive relative ordering like `Priority.after(Priority.before(base)) < base`, while the local integer-backed priority model collapsed that value back to the base priority and let parser-registry insertion order drift
- applied minimal fix: replaced the collapsed integer priority with the upstream relationship-based implementation so transitive parser-registry ordering compares like upstream again

## Files Changed

- `src/main/java/ch/njol/skript/lang/ParseContext.java`
- `src/main/java/ch/njol/skript/structures/StructOptions.java`
- `src/test/java/ch/njol/skript/ScriptLoaderCompatibilityTest.java`
- `src/main/java/ch/njol/skript/config/Node.java`
- `src/main/java/ch/njol/skript/config/SectionNode.java`
- `src/test/java/ch/njol/skript/config/SectionNodeCompatibilityTest.java`
- `src/main/java/ch/njol/skript/registrations/Classes.java`
- `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
- `src/main/java/org/skriptlang/skript/util/Priority.java`
- `src/main/java/org/skriptlang/skript/util/PriorityImpl.java`
- `src/test/java/org/skriptlang/skript/registration/SyntaxRegistryServiceTest.java`
- `docs/porting/parallel/LANE_C_STATUS.md`

## Exact Counts Changed

- Java source file count changed in `src/main/java/ch/njol/skript/structures`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript`: `0` added, `1` modified
- Java source file count changed in `src/main/java/ch/njol/skript/config`: `0` added, `2` modified
- Java test file count changed in `src/test/java/ch/njol/skript/config`: `0` added, `1` modified
- Java source file count changed in `src/main/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java source file count changed in `src/main/java/ch/njol/skript/lang`: `0` added, `1` modified
- Java test file count changed in `src/test/java/ch/njol/skript/registrations`: `0` added, `1` modified
- Java source file count changed in `src/main/java/org/skriptlang/skript/util`: `1` added, `1` modified
- Java test file count changed in `src/test/java/org/skriptlang/skript/registration`: `0` added, `1` modified
- real `.sk` fixture count changed: `0`

## Verification

- Repro (before fix): parsing `options:` with `blank:` logged `Invalid line in options` and left `{@blank}` unresolved instead of loading an empty-string option like upstream
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.ScriptLoaderCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms empty-value options load through the validator path for both top-level and nested entries
- Repro (before fix): after `node.add(entry); entry.setKey("Beacon");`, `node.get("beacon")` returned `null` and `node.get("marker")` still returned the renamed node
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.config.SectionNodeCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms renamed node keys refresh `SectionNode` lookups like upstream
- Repro (before fix): `Classes.toString("fallback", StringMode.VARIABLE_NAME)` returned `fallback` instead of upstream `object:fallback`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms upstream variable-name fallback prefix parity
- Repro (before fix): `Classes.toString((Object) new Object[]{"alpha", "beta"}, StringMode.MESSAGE)` returned Java array identity text instead of upstream `[alpha, beta]`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms object-typed arrays use upstream bracketed element stringification
- Repro (before fix): parsing `"shared"` as `SpecificBaseType` returned the exact base-type parser result even when a registered `SpecificChildType` parser should win by upstream classinfo specificity ordering
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms `Classes.parseSimple(...)` prefers the most specific registered compatible parser over an earlier exact base-type parser
- Repro (before fix): `Classes.clone(...)` invoked a value's reflective `clone()` method even when the registered `ClassInfo` had no cloner, producing a distinct object where upstream returns the original instance
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms `Cloneable` values are not reflectively cloned without an explicit `ClassInfo` cloner
- Repro (before fix): `Classes.parse("flagged-9", ..., ParseContext.COMMAND)` and `ParseContext.PARSE` still converted through a converter flagged `NO_COMMAND_ARGUMENTS`, while upstream rejects that converter outside `DEFAULT`
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms flagged converters still work in `ParseContext.DEFAULT` but are skipped in `COMMAND` and `PARSE`
- Repro (before fix): a registered `ClassInfo<String>` parser never ran because `Classes.parseSimple(...)` returned the raw input through the primitive fallback before checking classinfos, while upstream gives the registered parser first shot
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests ch.njol.skript.registrations.ClassesCompatibilityTest --rerun-tasks`
- After fix: targeted command passes; regression confirms registered primitive-backed parsers override the fallback coercion path like upstream
- Repro (before fix): `Priority.after(Priority.before(base))` compared equal to `base`, so parser-registry entries with transitive "before" priorities could register after the base entry instead of before it
- Targeted tests and commands:
  - `./gradlew -q test --no-daemon --console plain --tests org.skriptlang.skript.registration.SyntaxRegistryServiceTest --rerun-tasks`
- After fix: targeted command passes; regression confirms transitive relative priorities stay ordered before their base parser-registry entries like upstream

## Unresolved Risks

- none observed within this narrow surface; broader `Variables`/`Classes` parity remains ongoing

## Merge Notes

- likely conflict surface:
  - `src/main/java/ch/njol/skript/lang/ParseContext.java`
  - `src/main/java/ch/njol/skript/registrations/Classes.java`
  - `src/test/java/ch/njol/skript/registrations/ClassesCompatibilityTest.java`
  - `src/main/java/org/skriptlang/skript/util/Priority.java`
  - `src/test/java/org/skriptlang/skript/registration/SyntaxRegistryServiceTest.java`
  - `docs/porting/parallel/LANE_C_STATUS.md`
