package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ExprWorldEnvironment extends SimplePropertyExpression<ServerLevel, String> {

    static {
        register(ExprWorldEnvironment.class, String.class, "[world] environment", "worlds");
    }

    @Override
    public @Nullable String convert(ServerLevel world) {
        return environmentOf(world.dimension());
    }

    static String environmentOf(ResourceKey<Level> dimension) {
        if (Level.NETHER.equals(dimension)) {
            return "nether";
        }
        if (Level.END.equals(dimension)) {
            return "the_end";
        }
        if (Level.OVERWORLD.equals(dimension)) {
            return "normal";
        }
        return "custom";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "environment";
    }
}
