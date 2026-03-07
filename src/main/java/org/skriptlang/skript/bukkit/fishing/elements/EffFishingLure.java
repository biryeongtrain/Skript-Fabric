package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffFishingLure extends Effect {

    private boolean remove;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'fishing lure' effect can only be used in a fishing event.");
            return false;
        }
        remove = matchedPattern == 1;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.handle() instanceof FabricFishingEventHandle handle) {
            handle.setLureApplied(!remove);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (remove ? "remove" : "apply") + " the lure enchantment bonus";
    }
}
