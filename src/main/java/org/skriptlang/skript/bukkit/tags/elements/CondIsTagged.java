package org.skriptlang.skript.bukkit.tags.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.tags.TagSupport;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsTagged extends Condition {

    private Expression<?> elements;
    private Expression<?> tags;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        elements = expressions[0];
        tags = expressions[1];
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        Object[] rawTags = tags.getAll(event);
        boolean and = tags.getAnd();
        return elements.check(event, element -> {
            if (rawTags.length == 0) {
                return false;
            }
            for (Object rawTag : rawTags) {
                boolean tagged = TagSupport.isTagged(element, rawTag);
                if (and && !tagged) {
                    return false;
                }
                if (!and && tagged) {
                    return true;
                }
            }
            return and;
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(this, PropertyType.BE, event, debug, elements,
                "tagged with " + tags.toString(event, debug));
    }
}
