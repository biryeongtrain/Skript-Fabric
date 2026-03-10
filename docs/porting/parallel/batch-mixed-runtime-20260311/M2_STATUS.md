# M2 Status

## Scope

- `expressions/**` inventory/container/enchantment bundle only
- narrow tests for assigned expressions

## Assigned Targets

- `20` expressions:
  - `ExprAnvilRepairCost`
  - `ExprAnvilText`
  - `ExprArmorChangeItem`
  - `ExprArmorSlot`
  - `ExprAppliedEnchantments`
  - `ExprBannerItem`
  - `ExprBannerPatterns`
  - `ExprCursorSlot`
  - `ExprEnchantItem`
  - `ExprEnchantmentLevel`
  - `ExprEnchantmentOffer`
  - `ExprEnchantmentOfferCost`
  - `ExprEnchantments`
  - `ExprHotbarButton`
  - `ExprHotbarSlot`
  - `ExprInventoryAction`
  - `ExprInventoryCloseReason`
  - `ExprItemFlags`
  - `ExprOpenedInventory`
  - `ExprPickupDelay`

## Landed Classes

- none in this pass
- attempted upstream-backed imports and rolled them back after verification:
  - `ExprAppliedEnchantments`
  - `ExprBannerItem`
  - `ExprBannerPatterns`
  - `ExprEnchantItem`
  - `ExprEnchantmentLevel`
  - `ExprEnchantmentOffer`
  - `ExprEnchantments`
  - `ExprItemFlags`
- compile-safe branch state kept to the already-present mixed-runtime inventory/container expressions:
  - `ExprAnvilRepairCost`
  - `ExprAnvilText`
  - `ExprArmorChangeItem`
  - `ExprArmorSlot`
  - `ExprCursorSlot`
  - `ExprHotbarButton`
  - `ExprHotbarSlot`
  - `ExprInventoryAction`
  - `ExprInventoryCloseReason`
  - `ExprOpenedInventory`
  - `ExprPickupDelay`

## Runtime-Eligible Classes

- no newly landed runtime-active classes in this pass
- already-present assigned expressions above remain the only compile-safe/runtime-eligible subset in this worktree

## Bootstrap Registrations Needed

- none from this lane
- no new bootstrap registration request because no additional syntax was made runtime-active
- coordinator should continue treating the missing enchant/banner/item-flag expressions as blocked imports, not bootstrap work

## Targeted Tests

- `./gradlew compileJava --console=plain`
  - result: `BUILD SUCCESSFUL`
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionInventoryImportCompatibilityTest --console=plain`
  - result: blocked in `compileTestJava` before the selected test ran
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionInventoryContainerCompatibilityTest --console=plain`
  - result: blocked in `compileTestJava` before the selected test ran
- blocking verification context:
  - unrelated pre-existing test compile failures remain outside the M2-owned files and prevented narrow test execution
  - the temporary upstream import attempt also failed `compileJava` immediately and was rolled back before final status

## Blockers

- missing mixed-runtime item/enchantment compatibility layer for the remaining assigned upstream expressions:
  - upstream imports depend on `ch.njol.skript.aliases.ItemType`
  - upstream imports depend on `ch.njol.skript.util.EnchantmentType`
  - upstream imports depend on Bukkit event/item APIs not present in this branch surface, including `org.bukkit.enchantments.EnchantmentOffer`, `org.bukkit.inventory.ItemFlag`, and enchantment event classes
- current branch uses the Fabric-side item model under `org.skriptlang.skript.bukkit.base.types` instead of the upstream Bukkit `ItemType` path, so straight upstream expression imports are not compile-safe
- `ExprBannerPatterns` also depends on broader Bukkit banner/block/slot item adaptation and would need a real mixed-runtime port, not a direct copy
- `ExprInventoryAction` and `ExprInventoryCloseReason` are already present via reflective handle access; newly blocked work is specifically the missing enchant/banner/item-flag classes:
  - `ExprAppliedEnchantments`
  - `ExprBannerItem`
  - `ExprBannerPatterns`
  - `ExprEnchantItem`
  - `ExprEnchantmentLevel`
  - `ExprEnchantmentOffer`
  - `ExprEnchantments`
  - `ExprItemFlags`

## Merge Note

- status-only lane result
- likely merge touchpoints are limited to [docs/porting/parallel/batch-mixed-runtime-20260311/M2_STATUS.md](/private/tmp/skript-mixed-runtime-20260311/m2/docs/porting/parallel/batch-mixed-runtime-20260311/M2_STATUS.md)
