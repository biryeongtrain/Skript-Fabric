package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprPlain extends SimpleExpression<FabricItemType> {

    static {
        Skript.registerExpression(ExprPlain.class, FabricItemType.class, "[a[n]] (plain|unmodified) %itemtype%");
    }

    private Expression<FabricItemType> item;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        item = (Expression<FabricItemType>) exprs[0];
        return true;
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        FabricItemType itemType = item.getSingle(event);
        if (itemType == null) {
            return new FabricItemType[0];
        }
        return new FabricItemType[]{new FabricItemType(itemType.item(), itemType.amount(), null)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "plain " + item.toString(event, debug);
    }
}
