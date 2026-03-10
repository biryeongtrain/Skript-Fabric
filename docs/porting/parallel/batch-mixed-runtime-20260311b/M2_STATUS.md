# M2 Status

## Scope

- `expressions/**`
- minimal helper glue inside the same scope
- narrow tests for the assigned expression bundle

## Assigned Targets

- primary inventory/item/enchantment bundle `12`:
  - `ExprBannerItem`
  - `ExprBannerPatterns`
  - `ExprItemFlags`
  - `ExprLore`
  - `ExprNewBannerPattern`
  - `ExprAppliedEnchantments`
  - `ExprEnchantItem`
  - `ExprEnchantingExpCost`
  - `ExprEnchantmentBonus`
  - `ExprEnchantmentLevel`
  - `ExprEnchantmentOffer`
  - `ExprEnchantments`
- fallback item/projectile bundle `8`:
  - `ExprReadiedArrow`
  - `ExprProjectileCriticalState`
  - `ExprSpawnEggEntity`
  - `ExprSkull`
  - `ExprSkullOwner`
  - `ExprSlotIndex`
  - `ExprAppliedEffect`
  - `ExprItemFlags` follow-up runtime/value support if still needed

## Landed Classes

- `ExprInventoryAction`
- `ExprInventoryCloseReason`
- `ExprCursorSlot`
- `ExprArmorSlot`
- `ExprHotbarButton`
- `ExprHotbarSlot`
- `ExprArmorChangeItem`
- `ExprItemCooldown`
- `ExprAnvilRepairCost`
- `ExprAnvilText`
- `ExprCommandBlockCommand`
- `ExprItemFlags`

## Runtime-Eligible Classes

- `ExprInventoryAction`
- `ExprInventoryCloseReason`
- `ExprCursorSlot`
- `ExprArmorSlot`
- `ExprHotbarButton`
- `ExprHotbarSlot`
- `ExprArmorChangeItem`
- `ExprItemCooldown`
- `ExprAnvilRepairCost`
- `ExprAnvilText`
- `ExprCommandBlockCommand`
- `ExprItemFlags`

## Bootstrap Registrations Needed

- coordinator to decide after merge

## Targeted Tests

- `./gradlew test --tests 'ch.njol.skript.expressions.ExpressionItemCompatibilityTest' --tests 'ch.njol.skript.expressions.ExpressionSyntaxS2CompatibilityTest'`
- `ExpressionItemCompatibilityTest#itemFlagsReadAndMutateTooltipHiddenComponents`
- `ExpressionSyntaxS2CompatibilityTest#importedPropertyExpressionsParseAndBindAsChangeTargets`

## Blockers

- `ExprBannerItem` and `ExprBannerPatterns` remain open; no local upstream implementation was present in the sibling snapshots used for this shard.
- `ExprItemFlags` is implemented with Fabric tooltip-component mappings and string compatibility flags rather than a dedicated item-flag classinfo.

## Merge Note

- Landed 12 inventory/item expressions in this shard without touching canonical docs or `SkriptFabricBootstrap.java`.
