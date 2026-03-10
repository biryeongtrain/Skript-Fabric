package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.PropertyExpression;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLastDeathLocation extends PropertyExpression<GameProfile, FabricLocation> {

    static {
        register(ExprLastDeathLocation.class, FabricLocation.class, "[last] death location[s]", "offlineplayers");
    }

    @Override
    protected FabricLocation[] get(SkriptEvent event, GameProfile[] source) {
        if (event.server() == null) {
            return new FabricLocation[0];
        }
        List<FabricLocation> values = new ArrayList<>();
        for (GameProfile profile : source) {
            ServerPlayer player = event.server().getPlayerList().getPlayer(profile.getId());
            if (player == null) {
                continue;
            }
            FabricLocation location = lastDeathLocation(player);
            if (location != null) {
                values.add(location);
            }
        }
        return values.toArray(FabricLocation[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{FabricLocation.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (event.server() == null) {
            return;
        }
        FabricLocation location = delta == null ? null : (FabricLocation) delta[0];
        for (GameProfile profile : getExpr().getArray(event)) {
            ServerPlayer player = event.server().getPlayerList().getPlayer(profile.getId());
            if (player == null) {
                continue;
            }
            setLastDeathLocation(player, location);
        }
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "last death location of " + getExpr().toString(event, debug);
    }

    private @Nullable FabricLocation lastDeathLocation(ServerPlayer player) {
        try {
            Method getter = ServerPlayer.class.getMethod("getLastDeathLocation");
            Object value = getter.invoke(player);
            if (!(value instanceof Optional<?> optional) || optional.isEmpty() || !(optional.get() instanceof GlobalPos pos)) {
                return null;
            }
            net.minecraft.server.level.ServerLevel currentLevel = (net.minecraft.server.level.ServerLevel) player.level();
            return new FabricLocation(currentLevel.getServer().getLevel(pos.dimension()), net.minecraft.world.phys.Vec3.atCenterOf(pos.pos()));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private void setLastDeathLocation(ServerPlayer player, @Nullable FabricLocation location) {
        try {
            Method setter = ServerPlayer.class.getMethod("setLastDeathLocation", Optional.class);
            Optional<GlobalPos> value = location == null
                    ? Optional.empty()
                    : Optional.of(GlobalPos.of(location.level().dimension(), net.minecraft.core.BlockPos.containing(location.position())));
            setter.invoke(player, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
