package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.world.phys.Vec3;

public class ExprCoordinate extends SimplePropertyExpression<FabricLocation, Number> {

    static {
        register(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations");
    }

    private static final char[] AXES = {'x', 'y', 'z'};
    private int axis;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        super.init(exprs, matchedPattern, isDelayed, parseResult);
        axis = parseResult.mark;
        return true;
    }

    @Override
    public Number convert(FabricLocation location) {
        return switch (axis) {
            case 0 -> location.position().x;
            case 1 -> location.position().y;
            default -> location.position().z;
        };
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
                && getExpr().isSingle()
                && Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, FabricLocation.class)) {
            return new Class[]{Number.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
        if (delta == null || delta.length == 0) {
            return;
        }
        FabricLocation location = getExpr().getSingle(event);
        if (location == null) {
            return;
        }
        double value = ((Number) delta[0]).doubleValue();
        if (mode == ChangeMode.REMOVE) {
            value = -value;
        }
        Vec3 position = location.position();
        Vec3 updated = switch (mode) {
            case ADD, REMOVE -> switch (axis) {
                case 0 -> new Vec3(position.x + value, position.y, position.z);
                case 1 -> new Vec3(position.x, position.y + value, position.z);
                default -> new Vec3(position.x, position.y, position.z + value);
            };
            case SET -> switch (axis) {
                case 0 -> new Vec3(value, position.y, position.z);
                case 1 -> new Vec3(position.x, value, position.z);
                default -> new Vec3(position.x, position.y, value);
            };
            default -> position;
        };
        getExpr().change(event, new Object[]{new FabricLocation(location.level(), updated)}, ChangeMode.SET);
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "the " + AXES[axis] + "-coordinate";
    }
}
