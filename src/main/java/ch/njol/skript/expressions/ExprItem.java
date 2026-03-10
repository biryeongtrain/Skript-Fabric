package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import net.minecraft.world.item.ItemStack;

@Name("Item")
@Description("The item involved in an event, e.g. in a drop, dispense, pickup or craft event.")
@Example("""
    on dispense:
        item is a clock
        set the time to 6:00
    """)
@Since("unknown (before 2.1)")
public class ExprItem extends EventValueExpression<ItemStack> {

    static {
        register(ExprItem.class, ItemStack.class, "item");
    }

    public ExprItem() {
        super(ItemStack.class);
    }
}
