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

public class ExprLocationAt extends SimpleExpression<FabricLocation> {

    static {
        Skript.registerExpression(
                ExprLocationAt.class,
                FabricLocation.class,
                "[the] (location|position) [at] [\\(][x[ ][=[ ]]]%number%, [y[ ][=[ ]]]%number%, [and] [z[ ][=[ ]]]%number%[\\)] [[(in|of) [[the] world]] %world%]"
        );
    }

    private Expression<Number> x;
    private Expression<Number> y;
    private Expression<Number> z;
    private @Nullable Expression<ServerLevel> world;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        x = (Expression<Number>) exprs[0];
        y = (Expression<Number>) exprs[1];
        z = (Expression<Number>) exprs[2];
        world = (Expression<ServerLevel>) exprs[3];
        return true;
    }

    @Override
    protected FabricLocation @Nullable [] get(SkriptEvent event) {
        Number xValue = x.getSingle(event);
        Number yValue = y.getSingle(event);
        Number zValue = z.getSingle(event);
        if (xValue == null || yValue == null || zValue == null) {
            return new FabricLocation[0];
        }
        ServerLevel level = world == null ? null : world.getSingle(event);
        return new FabricLocation[]{
                new FabricLocation(level, new Vec3(xValue.doubleValue(), yValue.doubleValue(), zValue.doubleValue()))
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String worldText = world == null ? "" : " in " + world.toString(event, debug);
        return "the location at (" + x.toString(event, debug) + ", " + y.toString(event, debug) + ", " + z.toString(event, debug) + ")" + worldText;
    }
}
