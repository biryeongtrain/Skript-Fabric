package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorNormalize extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorNormalize.class,
                Vec3.class,
                "normalize[d] %vector%",
                "%vector% normalized"
        );
    }

    private Expression<Vec3> vector;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        vector = (Expression<Vec3>) exprs[0];
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        Vec3 value = vector.getSingle(event);
        if (value == null) {
            return new Vec3[0];
        }
        return new Vec3[]{Vec3ExpressionSupport.normalize(value)};
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
