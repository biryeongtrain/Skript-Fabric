# Surface E Expr Item Status

Last updated: 2026-03-10

## Scope

- `src/main/java/ch/njol/skript/expressions/**`
- tightly matching tests for the item/block expression closure

## Latest Slice

- landed a second item/block closure slice adapted onto the local Fabric compat surface:
  - `ExprAmountOfItems`
  - `ExprExactItem`
  - `ExprItem`
  - `ExprItemAmount`
  - `ExprItems`
  - `ExprItemWithCustomModelData`
  - `ExprItemWithEnchantmentGlint`
  - `ExprItemWithLore`
  - `ExprItemWithTooltip`
- extended `FabricItemType` so imported item-returning expressions preserve prototype stack components instead of dropping lore/glint/tooltip/model-data on `toStack()`
- expanded `ExpressionItemCompatibilityTest` with focused coverage for:
  - prototype-preserving item expressions
  - item amount mutation on compat item types
  - inventory counting via `FabricInventory`
  - block/item iteration via `ExprItems`
  - import instantiation for the restored item expressions

## Blockers

- `ExprAnvilRepairCost` and `ExprAnvilText`
  - blocked on a lane-external anvil inventory compat surface; the local tree has no owned anvil inventory wrapper analogous to upstream Bukkit `AnvilInventory`
- `ExprArmorChangeItem` and `ExprArmorSlot`
  - blocked on armor-change event wiring and richer slot/equipment abstractions not present in this lane-owned compat layer
- `ExprBannerItem` and `ExprBannerPatterns`
  - blocked on banner/block-item component translation decisions; upstream depends on Bukkit banner APIs that do not map 1:1 onto the current local surface
- `ExprConsumedItem` and `ExprAppliedEnchantments`
  - blocked on missing event handle coverage for the relevant consume/enchant events in this lane bundle
- `ExprItemFlags` and `ExprEnchantments`
  - blocked on missing local enum/helper scaffolding for upstream Bukkit `ItemFlag` and `EnchantmentType`
- `ExprExactItem`
  - landed with base block-to-item conversion only; exact block-entity component carry-over still needs a confirmed local block-entity item export path
- `ExprItemCooldown`
  - still open; nearby local cooldown APIs exist, but I kept the current slice scoped to expression imports that already had unit coverage and no player-runtime wiring risk

## Verification

- `./gradlew -q compileJava --rerun-tasks`
  - passed
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionItemCompatibilityTest --rerun-tasks`
  - passed

## Next Lead

- if Lane E keeps pushing item expressions, the next realistic target is `ExprItemCooldown`; the remaining primaries are mostly blocked on non-owned anvil, armor, banner, and enchantment/event compat surfaces

## Merge Notes

- exact commands run:
  - `./gradlew -q compileJava --rerun-tasks`
  - `./gradlew test --tests ch.njol.skript.expressions.ExpressionItemCompatibilityTest --rerun-tasks`
- likely conflicts:
  - `src/main/java/org/skriptlang/skript/fabric/compat/FabricItemType.java`
  - `src/main/java/ch/njol/skript/expressions/ExprAmountOfItems.java`
  - `src/main/java/ch/njol/skript/expressions/ExprExactItem.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItem.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItemAmount.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItems.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItemWithCustomModelData.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItemWithEnchantmentGlint.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItemWithLore.java`
  - `src/main/java/ch/njol/skript/expressions/ExprItemWithTooltip.java`
  - `src/main/java/ch/njol/skript/expressions/base/EventValueExpression.java`
  - `src/main/java/ch/njol/skript/expressions/ExprBookPages.java`
  - `src/main/java/ch/njol/skript/expressions/ExprCustomModelData.java`
  - `src/main/java/ch/njol/skript/expressions/ExprDurability.java`
  - `src/test/java/ch/njol/skript/expressions/ExpressionItemCompatibilityTest.java`
