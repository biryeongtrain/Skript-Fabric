package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Max Item Use Time")
@Description({
        "Returns the max duration an item can be used for before the action completes. " +
                "E.g. it takes 1.6 seconds to drink a potion, or 1.4 seconds to load an unenchanted crossbow.",
        "Some items, like bows and shields, do not have a limit to their use. They will return 1 hour."
})
@Example("""
	on right click:
		broadcast max usage duration of player's tool
	""")
@Since("2.8.0")
public class ExprMaxItemUseTime extends SimplePropertyExpression<Object, Timespan> {

    static {
        register(ExprMaxItemUseTime.class, Timespan.class, "max[imum] [item] us(e|age) (time|duration)", "itemtypes/itemstacks");
    }

    @Override
    public @Nullable Timespan convert(Object source) {
        ItemStack item = asItemStack(source);
        if (item == null) {
            return null;
        }
        return new Timespan(Timespan.TimePeriod.TICK, item.getUseDuration(null));
    }

    private @Nullable ItemStack asItemStack(Object source) {
        if (source instanceof ItemStack stack) {
            return stack;
        }
        if (source instanceof FabricItemType itemType) {
            return itemType.toStack();
        }
        return null;
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "maximum usage time";
    }
}
