package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;

public final class CondChatFiltering extends PropertyCondition<ServerPlayer> {

    static {
        Skript.registerCondition(
                CondChatFiltering.class,
                "%players% (is|are) using (chat|text) filtering",
                "%players% (isn't|is not|aren't|are not) using (chat|text) filtering",
                "%players% (has|have) (chat|text) filtering (on|enabled)",
                "%players% (doesn't|does not|do not|don't) have (chat|text) filtering (on|enabled)"
        );
    }

    @Override
    public boolean check(ServerPlayer player) {
        return ConditionRuntimeSupport.booleanMethod(player, false, "isTextFilteringEnabled")
                || ConditionRuntimeSupport.booleanField(player, false, "textFilteringEnabled");
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.BE;
    }

    @Override
    protected String getPropertyName() {
        return "using text filtering";
    }
}
