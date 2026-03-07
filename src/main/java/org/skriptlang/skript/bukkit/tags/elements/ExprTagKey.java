package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.tags.MinecraftTag;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTagKey extends SimpleExpression<String> {

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
    protected String @Nullable [] get(SkriptEvent event) {
        List<String> values = new ArrayList<>();
        for (Object value : tags.getAll(event)) {
            if (value instanceof MinecraftTag tag) {
                values.add(tag.toString());
            }
        }
        return values.toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return tags.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "key of " + tags.toString(event, debug);
    }
}
