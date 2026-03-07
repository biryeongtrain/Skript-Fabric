package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondTextDisplayHasDropShadow extends Condition {

    private Expression<?> displays;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        displays = expressions[0];
        setNegated(matchedPattern > 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return displays.check(event, value -> value instanceof Display.TextDisplay textDisplay
                && (PrivateEntityAccess.textDisplayFlags(textDisplay) & Display.TextDisplay.FLAG_SHADOW) != 0, isNegated());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        if (isNegated()) {
            return displays.toString(event, debug) + " does not have drop shadow";
        }
        return displays.toString(event, debug) + " has drop shadow";
    }
}
