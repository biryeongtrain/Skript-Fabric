package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Wearing")
@Description("Checks whether an entity is wearing some items.")
@Example("player is wearing an iron chestplate and iron leggings")
@Since("1.0")
public final class CondIsWearing extends Condition {

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.BODY
    };

    private Expression<LivingEntity> entities;
    private Expression<FabricItemType> types;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        entities = (Expression<LivingEntity>) exprs[0];
        types = (Expression<FabricItemType>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return entities.check(event, entity -> types.check(event, type -> matches(entity, type)), isNegated());
    }

    private boolean matches(LivingEntity entity, FabricItemType type) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (type.isOfType(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyType.BE, event, debug, entities,
                "wearing " + types.toString(event, debug));
    }
}
