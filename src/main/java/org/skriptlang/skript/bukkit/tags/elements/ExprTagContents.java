package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTagContents extends SimpleExpression<Object> {

    private Expression<?> tags;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        tags = expressions[0];
        return true;
    }

    @Override
    protected Object @Nullable [] get(SkriptEvent event) {
        List<Object> values = new ArrayList<>();
        for (Object value : tags.getAll(event)) {
            if (value instanceof MinecraftTag tag) {
                values.addAll(TagSupport.contents(tag));
            }
        }
        return values.toArray(Object[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public Class<?>[] possibleReturnTypes() {
        return new Class[]{Object.class};
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "tag contents of " + tags.toString(event, debug);
    }
}
