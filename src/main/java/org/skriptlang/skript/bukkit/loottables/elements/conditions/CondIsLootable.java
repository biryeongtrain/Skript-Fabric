package org.skriptlang.skript.bukkit.loottables.elements.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondIsLootable extends Condition {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        if (!values.canReturn(FabricBlock.class) && !values.canReturn(Object.class)) {
            return false;
        }
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return values.check(event, LootTableUtils::isLootable, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        if (isNegated()) {
            return values.toString(event, debug) + " is not lootable";
        }
        return values.toString(event, debug) + " is lootable";
    }
}
