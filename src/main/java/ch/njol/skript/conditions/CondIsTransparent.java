package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import net.minecraft.world.item.BlockItem;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Is Transparent")
@Description(
        "Checks whether an item is transparent. Note that this condition may not work for all blocks, "
                + "due to the transparency list used by Spigot not being completely accurate."
)
@Example("player's tool is transparent.")
@Since("2.2-dev36")
public class CondIsTransparent extends PropertyCondition<FabricItemType> {

    static {
        register(CondIsTransparent.class, "transparent", "itemtypes");
    }

    @Override
    public boolean check(FabricItemType itemType) {
        return itemType.item() instanceof BlockItem blockItem
                && blockItem.getBlock().defaultBlockState().propagatesSkylightDown();
    }

    @Override
    protected String getPropertyName() {
        return "transparent";
    }
}
