package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class CondIsEnchanted extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsEnchanted.class, "enchanted", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        ItemStack stack = itemType.toStack();
        return stack.isEnchanted() || stack.isEnchantable();
    }

    @Override
    protected String getPropertyName() {
        return "enchanted";
    }
}
