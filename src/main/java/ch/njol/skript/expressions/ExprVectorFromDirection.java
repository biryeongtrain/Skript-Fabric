package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorFromDirection extends SimpleExpression<Vec3> {

    static {
        Skript.registerExpression(
                ExprVectorFromDirection.class,
                Vec3.class,
                "vector[s] [from] %directions%",
                "%directions% vector[s]"
        );
    }

    private Expression<Direction> direction;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        direction = (Expression<Direction>) exprs[0];
        if (matchedPattern == 1 && !(direction instanceof ExprDirection)) {
            Skript.error("The direction in '%directions% vector[s]' can not be a variable. Use the direction expression instead.");
            return false;
        }
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        return direction.stream(event)
                .map(Direction::getDirection)
                .toArray(Vec3[]::new);
    }

    @Override
    public boolean isSingle() {
        return direction.isSingle();
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "vector " + direction.toString(event, debug);
    }
}
