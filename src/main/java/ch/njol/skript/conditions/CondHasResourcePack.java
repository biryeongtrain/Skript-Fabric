package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import net.minecraft.server.level.ServerPlayer;
import org.skriptlang.skript.fabric.runtime.FabricPlayerClientState;

public final class CondHasResourcePack extends PropertyCondition<ServerPlayer> {

    static {
        Skript.registerCondition(
                CondHasResourcePack.class,
                "%players% (is|are) using [a] resource pack",
                "%players% (isn't|is not|aren't|are not) using [a] resource pack",
                "%players% (has|have) [a] resource pack [(loaded|installed)]",
                "%players% (doesn't|does not|do not|don't) have [a] resource pack [(loaded|installed)]"
        );
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
        return PropertyType.BE;
    }

    @Override
    protected String getPropertyName() {
        return "using a resource pack";
    }
}
