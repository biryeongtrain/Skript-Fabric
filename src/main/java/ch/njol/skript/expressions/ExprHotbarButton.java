package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Hotbar Button")
@Description("The hotbar button used by an inventory-click style event handle.")
@Example("send hotbar button")
@Since("2.5")
public final class ExprHotbarButton extends SimpleExpression<Long> {

    static {
        Skript.registerExpression(ExprHotbarButton.class, Long.class, "[the] hotbar button");
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!ReflectiveHandleAccess.currentEventSupports("hotbarButton", "getHotbarButton", "button", "getButton")) {
            Skript.error("The 'hotbar button' expression may only be used in an inventory click event.");
            return false;
        }
        return true;
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event) {
        Object value = ReflectiveHandleAccess.invokeNoArg(event.handle(), "hotbarButton", "getHotbarButton", "button", "getButton");
        if (!(value instanceof Number number)) {
            return null;
        }
        return new Long[]{number.longValue()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the hotbar button";
    }
}
