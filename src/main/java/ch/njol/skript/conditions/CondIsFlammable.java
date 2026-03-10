package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.item.BlockItem;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Flammable")
@Description("Checks whether an item is flammable.")
@Example("send whether the tag contents of minecraft tag \"planks\" are flammable")
@Example("player's tool is flammable")
@Since("2.2-dev36")
public class CondIsFlammable extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsFlammable.class, "flammable", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        return itemType.item() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().ignitedByLava();
    }

    @Override
    protected String getPropertyName() {
        return "flammable";
    }
}
