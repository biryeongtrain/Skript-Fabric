package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventPotionEffect extends SimpleExpression<SkriptPotionEffect> {

    private int time;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricPotionEffectEventHandle.class)) {
            Skript.error("The event-potion effect expression can only be used in an entity potion effect event.");
            return false;
        }
        return true;
    }

    @Override
    protected SkriptPotionEffect @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricPotionEffectEventHandle handle)) {
            return new SkriptPotionEffect[0];
        }
        SkriptPotionEffect effect = handle.effect(time);
        return effect == null ? new SkriptPotionEffect[0] : new SkriptPotionEffect[]{effect};
    }

    @Override
    public boolean setTime(int time) {
        if (time == 0) {
            this.time = 0;
            return true;
        }
        if (time == 1 && getParser().isCurrentEvent(FabricPotionEffectEventHandle.class)) {
            this.time = 1;
            return true;
        }
        return false;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends SkriptPotionEffect> getReturnType() {
        return SkriptPotionEffect.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return time == 1 ? "past event-potion effect" : "event-potion effect";
    }
}
