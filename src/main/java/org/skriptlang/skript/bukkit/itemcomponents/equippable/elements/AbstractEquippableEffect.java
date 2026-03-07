package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

abstract class AbstractEquippableEffect extends Effect {

    protected Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return initPattern(matchedPattern);
    }

    protected abstract boolean initPattern(int matchedPattern);

    @Override
    protected void execute(SkriptEvent event) {
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper != null) {
                apply(wrapper);
            }
        }
    }

    protected abstract void apply(EquippableWrapper wrapper);
}
