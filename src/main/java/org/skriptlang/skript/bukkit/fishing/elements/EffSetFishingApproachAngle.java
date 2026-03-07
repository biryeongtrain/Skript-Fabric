package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetFishingApproachAngle extends Effect {

    private Expression<?> value;
    private boolean minimum;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        value = expressions[0];
        minimum = matchedPattern < 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return;
        }
        float angle = resolveFloat(event);
        if (minimum) {
            FabricFishingState.minLureAngle(handle.hook(), angle);
        } else {
            FabricFishingState.maxLureAngle(handle.hook(), angle);
        }
    }

    private float resolveFloat(SkriptEvent event) {
        Object parsed = value.getSingle(event);
        if (parsed instanceof Number number) {
            return clamp(number.floatValue());
        }
        if (parsed instanceof String string) {
            try {
                return clamp(Float.parseFloat(string.trim()));
            } catch (NumberFormatException ignored) {
                return 0.0F;
            }
        }
        return 0.0F;
    }

    private float clamp(float angle) {
        return Math.max(0.0F, Math.min(360.0F, angle));
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "set " + (minimum ? "minimum" : "maximum") + " fishing approach angle to "
                + value.toString(event, debug);
    }
}
