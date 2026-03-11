package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorBetweenLocations extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorBetweenLocations.class,
                Vec3.class,
                "[the] vector (from|between) %location% (to|and) %location%"
        );
    }

    private Expression<FabricLocation> from;
    private Expression<FabricLocation> to;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        from = (Expression<FabricLocation>) exprs[0];
        to = (Expression<FabricLocation>) exprs[1];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        FabricLocation fromValue = from.getSingle(event);
        FabricLocation toValue = to.getSingle(event);
        if (fromValue == null || toValue == null) {
            return new Vec3[0];
        }
        return new Vec3[]{Vec3ExpressionSupport.subtract(toValue.position(), fromValue.position())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }
}
