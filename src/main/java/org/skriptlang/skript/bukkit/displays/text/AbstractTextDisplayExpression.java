package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

abstract class AbstractTextDisplayExpression<T> extends SimpleExpression<T> {

    protected Expression<Entity> displays;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        displays = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    protected T @Nullable [] get(SkriptEvent event) {
        List<T> values = new ArrayList<>();
        for (Entity entity : displays.getAll(event)) {
            if (!(entity instanceof Display.TextDisplay textDisplay)) {
                continue;
            }
            T converted = convert(textDisplay);
            if (converted != null) {
                values.add(converted);
            }
        }
        return values.toArray(createArray(values.size()));
    }

    @Override
    public boolean isSingle() {
        return displays.isSingle();
    }

    protected abstract @Nullable T convert(Display.TextDisplay textDisplay);

    protected abstract T[] createArray(int length);

    protected abstract String propertyName();

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return propertyName() + " of " + displays.toString(event, debug);
    }
}
