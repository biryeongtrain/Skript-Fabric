package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;
import org.skriptlang.skript.fabric.runtime.FabricPlayerClientState;

public final class CondHasResourcePack extends PropertyCondition<ServerPlayer> {

    static {
        register(CondHasResourcePack.class, PropertyType.HAVE, "[a] resource pack [(loaded|installed)]", "players");
    }

    @Override
    public boolean check(ServerPlayer player) {
        return FabricPlayerClientState.hasLoadedResourcePack(player)
                || ConditionRuntimeSupport.booleanMethod(
                        player,
                        false,
                        "hasResourcePack",
                        "hasLoadedResourcePack",
                        "isResourcePackLoaded"
                );
    }

    @Override
    protected PropertyType getPropertyType() {
        return PropertyType.HAVE;
    }

    @Override
    protected String getPropertyName() {
        return "resource pack loaded";
    }
}
