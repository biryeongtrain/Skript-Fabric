# Next Agent Handoff

Last updated: 2026-03-07

## Current State

- Source-level condition port: `28 / 28` complete
- Source-level expression port: `84 / 84` complete
- Source-level effect port: `24 / 24` complete
- Event backend stage: `in_progress`
- Current Stage 5 closure: `22 / 22` = `100%`
- Stage 8 parity audit: `in_progress`
- Package-local Stage 8 audit progress: `23 / 214`
- Package-local parity-complete slice: `breeding (12 / 12)`, `input (5 / 5)`, `interactions (6 / 6)`
- Remaining package-local audit scope: `191 / 214`
- Latest verification:
  - `./gradlew runGameTest --rerun-tasks` passed
  - `181 / 181` required Fabric GameTests passed
  - `./gradlew build --rerun-tasks` passed

## Latest Slice That Just Landed

- Audited `breeding`, `input`, and `interactions` class-by-class against commit `145c3c9`.
- Added breeding `event-item` bridge support by tracking the love item on animals, piping it through the Fabric breeding handle, and rechecking it with a real `.sk` event fixture plus direct event-expression assertions.
- Added live coverage for `past current input keys of event-player` so the original past-state input event values are now rechecked through real `.sk`.
- Restored original interaction natural forms for player/date expressions and rechecked them through real `.sk`; `Date` now has value semantics so two equal timestamps compare equal during script evaluation.
- Wrote the first package-local Stage 8 coverage matrix into the tracking docs for `23 / 214` classes.
- Stage 5 implementation gaps remain closed in the active Fabric target surface.
- Found one cross-cutting residual Stage 8 gap outside the completed package-local slice: generic compare with ambiguous bare item ids is not parity-complete yet, for example `event-item is wheat`.

## Files Changed In The Latest Slice

- [NEXT_AGENT_HANDOFF.md](NEXT_AGENT_HANDOFF.md)
- [IMPLEMENTED_SYNTAX.md](IMPLEMENTED_SYNTAX.md)
- [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md)
- [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md)
- [AbstractSkriptFabricGameTestSupport.java](src/gametest/java/kim/biryeong/skriptFabricPort/gametest/AbstractSkriptFabricGameTestSupport.java)
- [SkriptFabricEventGameTest.java](src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricEventGameTest.java)
- [SkriptFabricExpressionGameTest.java](src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricExpressionGameTest.java)
- [breeding_event_item_marks_block.sk](src/gametest/resources/skript/gametest/event/breeding_event_item_marks_block.sk)
- [past_current_input_keys_marks_block.sk](src/gametest/resources/skript/gametest/expression/past_current_input_keys_marks_block.sk)
- [last_interact_player_names_entity.sk](src/gametest/resources/skript/gametest/expression/last_interact_player_names_entity.sk)
- [last_interact_time_names_entity.sk](src/gametest/resources/skript/gametest/expression/last_interact_time_names_entity.sk)
- [last_attack_time_names_entity.sk](src/gametest/resources/skript/gametest/expression/last_attack_time_names_entity.sk)
- [AnimalMixin.java](src/main/java/kim/biryeong/skriptFabric/mixin/AnimalMixin.java)
- [Date.java](src/main/java/ch/njol/skript/util/Date.java)
- [ExprCurrentInputKeys.java](src/main/java/org/skriptlang/skript/bukkit/input/elements/expressions/ExprCurrentInputKeys.java)
- [ExprLastInteractionDate.java](src/main/java/org/skriptlang/skript/bukkit/interactions/elements/expressions/ExprLastInteractionDate.java)
- [FabricBreedingItemSource.java](src/main/java/org/skriptlang/skript/fabric/compat/FabricBreedingItemSource.java)
- [FabricBreedingEventHandle.java](src/main/java/org/skriptlang/skript/fabric/runtime/FabricBreedingEventHandle.java)
- [FabricBreedingHandle.java](src/main/java/org/skriptlang/skript/fabric/runtime/FabricBreedingHandle.java)
- [SkriptFabricAdditionalSyntax.java](src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricAdditionalSyntax.java)
- [SkriptFabricEventBridge.java](src/main/java/org/skriptlang/skript/fabric/runtime/SkriptFabricEventBridge.java)
## User Preferences You Must Preserve

- Do not expose `fabric` in end-user Skript syntax unless absolutely necessary.
  - Use `on use item`, not `on fabric use item`.
- For item/block resource ids:
  - accept bare ids like `diamond_block`
  - if namespace is omitted, default to `minecraft:`
  - if namespace is present, keep it
  - avoid forcing quotes around normal ids in script fixtures
- For potion/status-effect ids:
  - keep them registry-backed
  - accept bare ids like `poison`
  - if namespace is omitted, default to `minecraft:`
  - if namespace is present, keep it
- Prefer registry lookups over hardcoded enums/tables when the Fabric side can be modded and dynamically extended.
- Do not claim parity that is not actually verified.
- Validate behavior through real `.sk` files and Fabric GameTest, not compile-only work.

## Important Context

- Event bridge status is tracked in [FABRIC_EVENT_MAPPING.md](FABRIC_EVENT_MAPPING.md).
- Stage progress is tracked in [FABRIC_PORT_STAGES.md](FABRIC_PORT_STAGES.md).
- Current active event classes live under:
  - `src/main/java/org/skriptlang/skript/fabric/syntax/event`
- Current active bridge/runtime code lives under:
  - `src/main/java/org/skriptlang/skript/fabric/runtime`
- Current Fabric GameTest layout lives under:
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/AbstractSkriptFabricGameTestSupport.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricBaseGameTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricConditionGameTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricExpressionGameTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricEffectGameTest.java`
  - `src/gametest/java/kim/biryeong/skriptFabricPort/gametest/SkriptFabricEventGameTest.java`
- Current Fabric GameTest script fixtures live under:
  - `src/gametest/resources/skript/gametest/base`
  - `src/gametest/resources/skript/gametest/condition`
  - `src/gametest/resources/skript/gametest/expression`
  - `src/gametest/resources/skript/gametest/effect`
  - `src/gametest/resources/skript/gametest/event`

## What Is Still Not Done

- Source-level `Condition` / `Expression` / `Effect` ports are complete, but overall parity is not.
- Tracked Stage 5 implementation gaps are closed in the active Fabric target surface.
- Deprecated-unused `PATROL_CAPTAIN` was intentionally dropped instead of emulated.
- The main remaining work is now the rest of the Stage 8 audit.
- Package-local parity is only written for `23 / 214` classes so far.
- Remaining package-local scope is `191 / 214`, plus `4` top-level non-package Bukkit helpers outside that matrix.
- The cross-cutting base compare gap for ambiguous bare item ids is still unresolved.
- No Bukkit event class row has been declared parity-complete yet in the event-mapping document.

## Recommended Next Priority

Continue the Stage 8 parity audit.

Recommended order:

1. keep auditing package-local Bukkit surface against `145c3c9`
2. continue the matrix with the next package-heavy surfaces that already have strong runtime coverage, for example `brewing` or `fishing`
3. keep the cross-cutting base compare gap (`event-item is wheat`) tracked separately so it does not get mistaken for a package-local breeding/input/interactions gap

For each audit slice:

1. compare the target syntax against commit `145c3c9`
2. verify the existing Fabric/Mojang bridge really covers it
3. add or tighten real `.sk` fixtures plus Fabric GameTests if coverage is missing
4. update `FABRIC_EVENT_MAPPING.md`, `FABRIC_PORT_STAGES.md`, `IMPLEMENTED_SYNTAX.md`, and this handoff with exact audited scope and exact remaining scope

## Verification Commands

Use these after the next slice:

```bash
./gradlew runGameTest --rerun-tasks
./gradlew build --rerun-tasks
```

If you are narrowing a parser/runtime issue first:

```bash
./gradlew test --tests org.skriptlang.skript.fabric.runtime.EffectBindingTest --tests org.skriptlang.skript.fabric.runtime.EffectSupportUnitTest --rerun-tasks
```

## Prompt To Give The Next Agent

```text
/Users/qf/IdeaProjects/Skript-Fabric-port/NEXT_AGENT_HANDOFF.md 를 먼저 읽고 그대로 이어서 작업해.

현재 상태:
- Condition 28/28 완료
- Expression 84/84 완료
- Effect 24/24 완료
- 181/181 Fabric GameTest 통과
- build 통과
- package-local Stage 8 audit progress 23/214
- 남은 핵심은 Stage 8 parity audit continuation

중요 제약:
- 사용자 Skript 문법에 fabric 접두/접미사를 넣지 마
- block/item resource id 는 bare id 허용, namespace 없으면 minecraft 기본값
- compile-only 작업 금지, 실제 .sk + Fabric GameTest 로 검증
- parity가 안 된 건 완료라고 말하지 마

다음 우선순위:
- Stage 8 parity audit 를 이어서 진행해
- `145c3c9` 기준 package-local Bukkit surface와 `FABRIC_EVENT_MAPPING.md` / `IMPLEMENTED_SYNTAX.md` / 실제 runtime dispatch를 대조해
- 지금 문서에는 breeding/input/interactions 23/214 matrix 가 반영되어 있으니 다음 slice는 그 다음 package들로 이어가
- cross-cutting base compare gap `event-item is wheat` 는 아직 parity-complete 로 선언하지 마
- deprecated-unused `PATROL_CAPTAIN` 은 이미 지원 표면에서 제거됐으니 gap으로 다시 올리지 마
- plain resource/token parsing은 가능한 한 registry 기반으로 유지해. Fabric 쪽은 모딩으로 item/entity/status effect 목록이 늘어날 수 있으니 새 하드코딩 enum/table을 늘리지 마
- GameTest는 이미 category별로 분리되어 있으니 새 테스트는 해당 카테고리 클래스와 fixture 폴더에 추가해
- 변경 후 `./gradlew runGameTest --rerun-tasks` 와 `./gradlew build --rerun-tasks` 를 돌리고, 무엇이 완료됐고 무엇이 남았는지 정확한 수치로 보고해
```
