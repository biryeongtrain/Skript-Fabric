package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;

public class ExprSeaLevel extends SimplePropertyExpression<ServerLevel, Long> {

    static {
        register(ExprSeaLevel.class, Long.class, "sea level", "worlds");
    }

    @Override
    public Long convert(ServerLevel world) {
        return (long) world.getSeaLevel();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return "sea level";
    }
}
