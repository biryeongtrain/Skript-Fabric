package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;

public final class CondChatColors extends PropertyCondition<ServerPlayer> {

    static {
        register(CondChatColors.class, PropertyType.CAN, "see chat colo[u]r[s|ing]", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return ConditionRuntimeSupport.booleanMethod(
                player,
                false,
                "canChatInColor",
                "hasChatColorsEnabled",
                "isChatColorsEnabled"
        ) || ConditionRuntimeSupport.booleanField(
                player,
                false,
                "canChatColor",
                "chatColorsEnabled"
        );
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.CAN;
    }

    @Override
    protected String getPropertyName() {
        return "see chat colors";
    }
}
