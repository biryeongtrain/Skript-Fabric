package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Equip")
@Description({
        "Equips or unequips an entity with the given itemtypes (usually armor).",
        "This effect will replace any armor that the entity is already wearing."
})
@Example("equip player with diamond helmet")
@Example("equip player with diamond leggings, diamond chestplate, and diamond boots")
@Example("unequip diamond chestplate from player")
@Example("unequip player's armor")
@Since({
        "1.0, 2.7 (multiple entities, unequip), 2.10 (wolves)",
        "2.12.1 (happy ghasts)"
})
public class EffEquip extends Effect {

    private static boolean registered;

    private Expression<?> entities;
    private @Nullable Expression<FabricItemType> itemTypes;
    private boolean equip = true;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffEquip.class,
                "equip [%livingentities%] with %itemtypes%",
                "make %livingentities% wear %itemtypes%",
                "unequip %itemtypes% [from %livingentities%]",
                "unequip %livingentities%'[s] (armo[u]r|equipment)"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        if (matchedPattern == 0 || matchedPattern == 1) {
            entities = exprs[0];
            itemTypes = (Expression<FabricItemType>) exprs[1];
        } else if (matchedPattern == 2) {
            itemTypes = (Expression<FabricItemType>) exprs[0];
            entities = exprs[1];
            equip = false;
        } else {
            entities = exprs[0];
            itemTypes = null;
            equip = false;
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        Expression<?> rawEntities = entities;
        FabricItemType[] resolvedItemTypes = itemTypes == null ? new FabricItemType[0] : itemTypes.getArray(event);
        for (Object resolvedEntity : rawEntities.getArray(event)) {
            if (!(resolvedEntity instanceof LivingEntity entity)) {
                continue;
            }
            if (itemTypes == null) {
                clearEquipment(entity);
                continue;
            }
            for (FabricItemType itemType : resolvedItemTypes) {
                EquipmentSlot slot = resolveSlot(itemType);
                if (slot == null) {
                    continue;
                }
                entity.setItemSlot(slot, equip ? itemType.toStack() : ItemStack.EMPTY);
            }
        }
    }

    private void clearEquipment(LivingEntity entity) {
        entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
        entity.setItemSlot(EquipmentSlot.BODY, ItemStack.EMPTY);
    }

    private @Nullable EquipmentSlot resolveSlot(FabricItemType itemType) {
        ItemStack stack = itemType.toStack();
        var equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            return equippable.slot();
        }
        String itemId = itemType.itemId();
        if (itemId.endsWith("helmet") || itemId.endsWith("skull") || itemId.endsWith("head") || itemId.endsWith("pumpkin")) {
            return EquipmentSlot.HEAD;
        }
        if (itemId.endsWith("chestplate") || itemId.endsWith("elytra")) {
            return EquipmentSlot.CHEST;
        }
        if (itemId.endsWith("leggings")) {
            return EquipmentSlot.LEGS;
        }
        if (itemId.endsWith("boots")) {
            return EquipmentSlot.FEET;
        }
        return EquipmentSlot.HEAD;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (equip) {
            return "equip " + entities.toString(event, debug) + " with " + itemTypes.toString(event, debug);
        }
        if (itemTypes != null) {
            return "unequip " + itemTypes.toString(event, debug) + " from " + entities.toString(event, debug);
        }
        return "unequip " + entities.toString(event, debug) + "'s equipment";
    }
}
