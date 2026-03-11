package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprUnbreakable extends SimpleExpression<FabricItemType> {

    static {
        Skript.registerExpression(ExprUnbreakable.class, FabricItemType.class, "[a[n]] [:un]breakable %itemtypes%");
    }

    private Expression<FabricItemType> items;
    private boolean unbreakable;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        unbreakable = parseResult.hasTag("un");
        return true;
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        FabricItemType[] source = items.getArray(event);
        FabricItemType[] result = new FabricItemType[source.length];
        for (int i = 0; i < source.length; i++) {
            ItemStack stack = source[i].toStack();
            if (unbreakable) {
                stack.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
            } else {
                stack.remove(DataComponents.UNBREAKABLE);
            }
            result[i] = new FabricItemType(stack);
        }
        return result;
    }

    @Override
    public boolean isSingle() {
        return items.isSingle();
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (unbreakable ? "unbreakable " : "breakable ") + items.toString(event, debug);
    }
}
