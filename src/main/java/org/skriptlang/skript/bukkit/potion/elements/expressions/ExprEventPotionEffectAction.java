package org.skriptlang.skript.bukkit.potion.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventPotionEffectAction extends SimpleExpression<String> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricPotionEffectEventHandle.class)) {
            Skript.error("The event-potion effect action expression can only be used in an entity potion effect event.");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricPotionEffectEventHandle handle)) {
            return new String[0];
        }
        return new String[]{handle.action().skriptName()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-potion effect action";
    }
}
