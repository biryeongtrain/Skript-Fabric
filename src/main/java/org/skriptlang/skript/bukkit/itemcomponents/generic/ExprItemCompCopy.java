package org.skriptlang.skript.bukkit.itemcomponents.generic;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.ComponentWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprItemCompCopy extends SimpleExpression<ComponentWrapper<?>> {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return true;
    }

    @Override
    protected ComponentWrapper<?> @Nullable [] get(SkriptEvent event) {
        return values.stream(event)
                .filter(ComponentWrapper.class::isInstance)
                .map(ComponentWrapper.class::cast)
                .map(ComponentWrapper::copy)
                .toArray(ComponentWrapper[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends ComponentWrapper<?>> getReturnType() {
        return (Class<? extends ComponentWrapper<?>>) (Class<?>) ComponentWrapper.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "item component copy of " + values.toString(event, debug);
    }
}
