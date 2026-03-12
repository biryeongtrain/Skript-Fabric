package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprNamed extends SimpleExpression<FabricItemType> {

    static {
        Skript.registerExpression(ExprNamed.class, FabricItemType.class, "%itemtype% (named|with name[s]) %string%");
    }

    private Expression<FabricItemType> items;
    private Expression<String> names;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        items = (Expression<FabricItemType>) exprs[0];
        names = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        FabricItemType item = items.getSingle(event);
        String name = names.getSingle(event);
        if (item == null) {
            return new FabricItemType[0];
        }
        if (name == null) {
            return new FabricItemType[]{new FabricItemType(item.toStack())};
        }
        FabricItemType named = new FabricItemType(item.toStack());
        named.name(name);
        return new FabricItemType[]{named};
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
        return items.toString(event, debug) + " named " + names.toString(event, debug);
    }
}
