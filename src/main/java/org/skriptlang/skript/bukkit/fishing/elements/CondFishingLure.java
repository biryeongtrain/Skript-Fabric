package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondFishingLure extends Condition {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'lure enchantment' condition can only be used in a fishing event.");
            return false;
        }
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle fishingEvent)) {
            return false;
        }
        return fishingEvent.lureApplied() ^ isNegated();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "lure enchantment bonus " + (isNegated() ? "isn't" : "is") + " applied";
    }
}
