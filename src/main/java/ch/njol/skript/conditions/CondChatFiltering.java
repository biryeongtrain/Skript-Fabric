package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;

public final class CondChatFiltering extends PropertyCondition<ServerPlayer> {

    static {
        register(CondChatFiltering.class, PropertyType.HAVE, "(chat|text) filtering (on|enabled)", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return ConditionRuntimeSupport.booleanMethod(player, false, "isTextFilteringEnabled");
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "chat filtering enabled";
    }
}
