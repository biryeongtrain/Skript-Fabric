package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("View Distance")
@Description("The server-side view distance of a player, or the shared server view distance exposed through worlds.")
@Example("send view distance of player")
@Since("2.4, Fabric")
public class ExprViewDistance extends SimplePropertyExpression<Object, Integer> {

    static {
        register(ExprViewDistance.class, Integer.class, "view distance[s]", "players/worlds");
    }

    @Override
    public @Nullable Integer convert(Object object) {
        if (object instanceof ServerPlayer player) {
            return ExpressionRuntimeSupport.playerViewDistance(player);
        }
        if (object instanceof ServerLevel world) {
            return ExpressionRuntimeSupport.viewDistance(world.getServer());
        }
        return null;
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
        for (Object source : getExpr().getArray(event)) {
            Integer current = convert(source);
            if (current == null) {
                continue;
            }
            int base = switch (mode) {
                case SET -> delta == null ? current : ((Number) delta[0]).intValue();
                case DELETE, RESET -> 10;
                case ADD -> current + ((Number) delta[0]).intValue();
                case REMOVE -> current - ((Number) delta[0]).intValue();
                default -> current;
            };
            int next = (int) Math2.fit(2, base, 32);
            if (source instanceof ServerPlayer player) {
                ExpressionRuntimeSupport.setPlayerViewDistance(player, next);
            } else if (source instanceof ServerLevel world) {
                ExpressionRuntimeSupport.setViewDistance(world.getServer(), next);
            }
        }
    }

    @Override
    public Class<Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "view distance";
    }
}
