package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.world.item.ItemStack;
import org.skriptlang.skript.fabric.compat.FabricItemType;

public final class CondIsEnchanted extends PropertyCondition<FabricItemType> {

    static {
        Skript.registerCondition(
                CondIsEnchanted.class,
                "%itemtypes% (is|are) enchanted",
                "%itemtypes% (isn't|is not|aren't|are not) enchanted",
                "%itemtypes% (is|are) [an] enchanted item[s]",
                "%itemtypes% (isn't|is not|aren't|are not) [an] enchanted item[s]",
                "%itemtypes% (has|have) enchantments",
                "%itemtypes% (doesn't|does not|do not|don't) have enchantments"
        );
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
