package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;

public final class CondHasClientWeather extends PropertyCondition<ServerPlayer> {

    static {
        register(CondHasClientWeather.class, PropertyType.HAVE, "[a] (client|custom) weather [set]", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        Object value = ConditionRuntimeSupport.invokeCompatible(player, "getPlayerWeather", "getClientWeather");
        return value != null || ConditionRuntimeSupport.booleanMethod(player, false, "hasClientWeather", "hasCustomWeather");
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "custom weather set";
    }
}
