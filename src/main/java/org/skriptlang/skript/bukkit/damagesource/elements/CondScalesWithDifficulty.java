package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.damagesource.DamageSource;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondScalesWithDifficulty extends Condition {

    private Expression<?> damageSources;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(DamageSource.class)) {
            return false;
        }
        damageSources = expressions[0];
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return damageSources.check(event, value -> value instanceof DamageSource damageSource
                && damageSource.scalesWithDifficulty(), isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        if (isNegated()) {
            return damageSources.toString(event, debug) + " does not scale damage with difficulty";
        }
        return damageSources.toString(event, debug) + " scales damage with difficulty";
    }
}
