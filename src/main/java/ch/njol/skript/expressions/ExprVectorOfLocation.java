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

public class ExprVectorOfLocation extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorOfLocation.class,
                Vec3.class,
                "[the] vector (of|from|to) %location%",
                "%location%'s vector"
        );
    }

    private Expression<FabricLocation> location;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        location = (Expression<FabricLocation>) exprs[0];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        FabricLocation value = location.getSingle(event);
        if (value == null) {
            return new Vec3[0];
        }
        return new Vec3[]{value.position()};
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
