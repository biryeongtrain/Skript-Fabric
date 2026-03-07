package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffTextDisplayDropShadow extends Effect {

    private Expression<Entity> displays;
    private boolean addShadow;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        displays = (Expression<Entity>) expressions[0];
        addShadow = matchedPattern <= 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : displays.getAll(event)) {
            if (!(entity instanceof Display.TextDisplay textDisplay)) {
                continue;
            }
            byte flags = PrivateEntityAccess.textDisplayFlags(textDisplay);
            if (addShadow) {
                flags |= Display.TextDisplay.FLAG_SHADOW;
            } else {
                flags &= ~Display.TextDisplay.FLAG_SHADOW;
            }
            PrivateEntityAccess.setTextDisplayFlags(textDisplay, flags);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (addShadow ? "add" : "remove") + " drop shadow " + displays.toString(event, debug);
    }
}
