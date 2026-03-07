package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprSkriptPotionEffect extends SimpleExpression<SkriptPotionEffect> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(SkriptPotionEffect.class)) {
            Skript.error("The created potion effect expression can only be used in a potion effect section.");
            return false;
        }
        return true;
    }

    @Override
    protected SkriptPotionEffect @Nullable [] get(SkriptEvent event) {
        return event.handle() instanceof SkriptPotionEffect effect ? new SkriptPotionEffect[]{effect} : new SkriptPotionEffect[0];
    }

    @Override public boolean isSingle() { return true; }
    @Override public Class<? extends SkriptPotionEffect> getReturnType() { return SkriptPotionEffect.class; }
    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "created potion effect"; }
}
