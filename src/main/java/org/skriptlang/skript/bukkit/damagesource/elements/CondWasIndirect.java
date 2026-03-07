package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.damagesource.DamageSource;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondWasIndirect extends Condition {

    private Expression<?> damageSources;
    private boolean indirect;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(DamageSource.class)) {
            return false;
        }
        damageSources = expressions[0];
        indirect = matchedPattern < 2;
        setNegated((matchedPattern % 2) == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return damageSources.check(event, value -> {
            if (!(value instanceof DamageSource damageSource)) {
                return false;
            }
            return indirect ? !damageSource.isDirect() : damageSource.isDirect();
        }, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder(damageSources.toString(event, debug));
        builder.append(damageSources.isSingle() ? " was " : " were ");
        if (isNegated()) {
            builder.append("not ");
        }
        builder.append(indirect ? "indirectly" : "directly");
        builder.append(" caused");
        return builder.toString();
    }
}
