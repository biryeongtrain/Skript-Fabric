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

- landed compile-safe mixed-runtime ports from the assigned bundle:
  - `ExprAnvilRepairCost`
  - `ExprAnvilText`
  - `ExprArmorChangeItem`
  - `ExprArmorSlot`
  - `ExprCursorSlot`
  - `ExprEnchantmentOfferCost`
  - `ExprHotbarButton`
  - `ExprHotbarSlot`
  - `ExprInventoryAction`
  - `ExprInventoryCloseReason`
  - `ExprOpenedInventory`
  - `ExprPickupDelay`
- support glue added for the landed subset:
  - `ReflectiveHandleAccess`

## Runtime-Eligible Classes

- compile-safe and bootstrap-eligible if the coordinator chooses to activate them with real `.sk` coverage:
  - `ExprAnvilRepairCost`
  - `ExprAnvilText`
  - `ExprArmorChangeItem`
  - `ExprArmorSlot`
  - `ExprCursorSlot`
  - `ExprEnchantmentOfferCost`
  - `ExprHotbarButton`
  - `ExprHotbarSlot`
  - `ExprInventoryAction`
  - `ExprInventoryCloseReason`
  - `ExprOpenedInventory`
  - `ExprPickupDelay`
- still import-only in this lane because no bootstrap wiring was added here

## Bootstrap Registrations Needed

- none landed as runtime-active in this lane
- no `SkriptFabricBootstrap.java` changes were made
- if the coordinator decides to activate the landed subset later, those registrations still need coordinator-owned bootstrap wiring plus representative real `.sk` GameTests in the merge pass

## Targeted Tests

- `./gradlew compileJava --console=plain`
  - result: `BUILD SUCCESSFUL`
- `./gradlew test --tests ch.njol.skript.expressions.ExpressionInventoryContainerCompatibilityTest --console=plain`
  - result: `BUILD SUCCESSFUL`
  - coverage includes:
    - parser/init coverage for `ExprOpenedInventory`, `ExprArmorSlot`, `ExprCursorSlot`, `ExprHotbarSlot`, `ExprPickupDelay`
    - reflective holder/event coverage for `ExprAnvilRepairCost`, `ExprAnvilText`, `ExprArmorChangeItem`, `ExprEnchantmentOfferCost`, `ExprHotbarButton`, `ExprInventoryAction`, `ExprInventoryCloseReason`
    - `ItemEntity.pickupDelay` read/write coverage for `ExprPickupDelay`

## Blockers

- missing mixed-runtime item/enchantment compatibility layer for the remaining assigned expressions:
  - upstream imports depend on `ch.njol.skript.aliases.ItemType`
  - upstream imports depend on `ch.njol.skript.util.EnchantmentType`
  - upstream imports depend on Bukkit event/item APIs not present in this branch surface, including `org.bukkit.enchantments.EnchantmentOffer`, `org.bukkit.inventory.ItemFlag`, and enchantment event classes
- the current branch uses the Fabric-side item model instead of the upstream Bukkit `ItemType` / `EnchantmentType` path, so straight upstream copies are not compile-safe
- `ExprBannerPatterns` also depends on broader banner/block/item adaptation and would need a real mixed-runtime port instead of a direct import
- blocked assigned targets in this lane:
  - `ExprAppliedEnchantments`
  - `ExprBannerItem`
  - `ExprBannerPatterns`
  - `ExprEnchantItem`
  - `ExprEnchantmentLevel`
  - `ExprEnchantmentOffer`
  - `ExprEnchantments`
  - `ExprItemFlags`

## Merge Note

- likely merge touchpoints:
  - [ExprAnvilRepairCost.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprAnvilRepairCost.java)
  - [ExprAnvilText.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprAnvilText.java)
  - [ExprArmorChangeItem.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprArmorChangeItem.java)
  - [ExprArmorSlot.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprArmorSlot.java)
  - [ExprCursorSlot.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprCursorSlot.java)
  - [ExprEnchantmentOfferCost.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprEnchantmentOfferCost.java)
  - [ExprHotbarButton.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprHotbarButton.java)
  - [ExprHotbarSlot.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprHotbarSlot.java)
  - [ExprInventoryAction.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprInventoryAction.java)
  - [ExprInventoryCloseReason.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprInventoryCloseReason.java)
  - [ExprOpenedInventory.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprOpenedInventory.java)
  - [ExprPickupDelay.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ExprPickupDelay.java)
  - [ReflectiveHandleAccess.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/main/java/ch/njol/skript/expressions/ReflectiveHandleAccess.java)
  - [ExpressionInventoryContainerCompatibilityTest.java](/private/tmp/skript-mixed-runtime-20260311/m2/src/test/java/ch/njol/skript/expressions/ExpressionInventoryContainerCompatibilityTest.java)
  - [M2_STATUS.md](/private/tmp/skript-mixed-runtime-20260311/m2/docs/porting/parallel/batch-mixed-runtime-20260311/M2_STATUS.md)
