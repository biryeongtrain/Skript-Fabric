package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;

public final class CondHasClientWeather extends PropertyCondition<ServerPlayer> {

    static {
        Skript.registerCondition(
                CondHasClientWeather.class,
                "%players% (is|are) using [a] (client|custom) weather",
                "%players% (isn't|is not|aren't|are not) using [a] (client|custom) weather",
                "%players% (has|have) [a] (client|custom) weather [set]",
                "%players% (doesn't|does not|do not|don't) have [a] (client|custom) weather [set]"
        );
    }

    @Override
    public boolean check(ServerPlayer player) {
        Object value = ConditionRuntimeSupport.invokeCompatible(player, "getPlayerWeather", "getClientWeather");
        return value != null || ConditionRuntimeSupport.booleanMethod(player, false, "hasClientWeather", "hasCustomWeather");
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.BE;
    }

    @Override
    protected String getPropertyName() {
        return "using custom weather";
    }
}
