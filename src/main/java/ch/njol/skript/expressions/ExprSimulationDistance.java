package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.server.level.ServerLevel;

public class ExprSimulationDistance extends SimplePropertyExpression<ServerLevel, Integer> {

    static {
        register(ExprSimulationDistance.class, Integer.class, "simulation distance[s]", "worlds");
    }

    @Override
    public @Nullable Integer convert(ServerLevel world) {
        return world.getServer().getPlayerList().getSimulationDistance();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> new Class[]{Integer.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (ServerLevel world : getExpr().getArray(event)) {
            int current = world.getServer().getPlayerList().getSimulationDistance();
            int next = switch (mode) {
                case SET -> delta == null ? current : ((Number) delta[0]).intValue();
                case DELETE, RESET -> 10;
                case ADD -> current + ((Number) delta[0]).intValue();
                case REMOVE -> current - ((Number) delta[0]).intValue();
                default -> current;
            };
            world.getServer().getPlayerList().setSimulationDistance((int) Math2.fit(2, next, 32));
        }
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "simulation distance";
    }
}
