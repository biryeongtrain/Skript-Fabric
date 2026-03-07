package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondBrewingConsume extends Condition {

    private boolean willConsume;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricBrewingFuelEventHandle.class)) {
            Skript.error("The 'brewing stand consume fuel' condition can only be used in a brewing fuel event.");
            return false;
        }
        willConsume = matchedPattern == 0;
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (!(event.handle() instanceof FabricBrewingFuelEventHandle handle)) {
            return false;
        }
        return handle.willConsume() == willConsume;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the brewing stand will" + (willConsume ? "" : " not") + " consume the fuel";
    }
}
