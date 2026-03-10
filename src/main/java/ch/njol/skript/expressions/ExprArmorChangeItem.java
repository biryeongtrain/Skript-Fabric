package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Armor Change Item")
@Description("Gets the old or new armor item from an armor-change style event handle when that handle exposes it.")
@Example("broadcast the old armor item")
@Events("Armor Change")
@Since("2.11")
public final class ExprArmorChangeItem extends SimpleExpression<ItemStack> {

    static {
        ch.njol.skript.Skript.registerExpression(
                ExprArmorChangeItem.class,
                ItemStack.class,
                "(old|unequipped) armo[u]r item",
                "(new|equipped) armo[u]r item"
        );
    }

    private boolean oldArmor;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        oldArmor = matchedPattern == 0;
        return true;
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), oldArmor ? "oldItem" : "newItem");
        if (!(value instanceof ItemStack stack) || stack.isEmpty()) {
            return null;
        }
        return new ItemStack[]{stack};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return oldArmor ? "old armor item" : "new armor item";
    }
}
