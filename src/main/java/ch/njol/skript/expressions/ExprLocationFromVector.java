package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLocationFromVector extends SimpleExpression<FabricLocation> {

    static {
        Skript.registerExpression(
                ExprLocationFromVector.class,
                FabricLocation.class,
                "%vector% to location in %world%",
                "location (from|of) %vector% in %world%"
        );
    }

    private Expression<Vec3> vector;
    private Expression<ServerLevel> world;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        vector = (Expression<Vec3>) exprs[0];
        world = (Expression<ServerLevel>) exprs[1];
        return true;
    }

    @Override
    protected FabricLocation @Nullable [] get(SkriptEvent event) {
        Vec3 vectorValue = vector.getSingle(event);
        ServerLevel level = world.getSingle(event);
        if (vectorValue == null || level == null) {
            return new FabricLocation[0];
        }
        return new FabricLocation[]{new FabricLocation(level, vectorValue)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }
}
