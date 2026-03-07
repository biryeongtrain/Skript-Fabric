package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffBrewingConsume extends Effect {

    private boolean consume;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricBrewingFuelEventHandle.class)) {
            Skript.error("The 'brewing consume fuel' effect can only be used in a brewing fuel event.");
            return false;
        }
        consume = matchedPattern == 0;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (event.handle() instanceof FabricBrewingFuelEventHandle handle) {
            handle.setWillConsume(consume);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return consume ? "make the brewing stand consume the fuel" : "prevent the brewing stand from consuming the fuel";
    }
}
