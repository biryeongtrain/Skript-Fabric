package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Compass Target")
@Description({
        "The location a player's compass is pointing at on the Fabric compatibility surface.",
        "Fabric does not expose a direct compass target API, so this expression stores the configured target and syncs it to the client."
})
@Example("""
    every 5 seconds:
        loop all players:
            set loop-player's compass target to spawn location of world of loop-player
    """)
@Since("2.0, 2.13 (Fabric)")
public class ExprCompassTarget extends SimplePropertyExpression<ServerPlayer, FabricLocation> {

    static {
        register(ExprCompassTarget.class, FabricLocation.class, "compass target", "players");
    }

    @Override
    public @Nullable FabricLocation convert(ServerPlayer player) {
        FabricLocation target = CompassTargetExpressionSupport.get(player);
        return target != null ? target : CompassTargetExpressionSupport.defaultTarget(player);
    }

    @Override
    public Class<FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "compass target";
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{FabricLocation.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) throws UnsupportedOperationException {
        for (ServerPlayer player : getExpr().getArray(event)) {
            FabricLocation target = mode == ChangeMode.RESET || delta == null
                    ? CompassTargetExpressionSupport.defaultTarget(player)
                    : (FabricLocation) delta[0];
            if (mode == ChangeMode.RESET || delta == null) {
                CompassTargetExpressionSupport.clear(player);
            } else {
                CompassTargetExpressionSupport.set(player, target);
            }
            CompassTargetExpressionSupport.sync(player, target);
        }
    }
}
