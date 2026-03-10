package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Coordinate")
@Description("Represents a given coordinate of a location.")
@Example("player's y-coordinate is smaller than 40")
@Since("1.4.3")
public class ExprCoordinate extends SimplePropertyExpression<FabricLocation, Number> {

    private static final char[] AXES = {'x', 'y', 'z'};

    static {
        register(ExprCoordinate.class, Number.class, "(0¦x|1¦y|2¦z)(-| )(coord[inate]|pos[ition]|loc[ation])[s]", "locations");
    }

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
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        if ((mode == ChangeMode.SET || mode == ChangeMode.ADD || mode == ChangeMode.REMOVE)
                && getExpr().isSingle()
                && Changer.ChangerUtils.acceptsChange(getExpr(), ChangeMode.SET, FabricLocation.class)) {
            return new Class[]{Number.class};
        }
        return null;
    }

    @Override
    public void change(SkriptEvent event, @Nullable Object[] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0) {
            return;
        }
        FabricLocation location = getExpr().getSingle(event);
        if (location == null) {
            return;
        }
        double changed = ((Number) delta[0]).doubleValue();
        if (mode == ChangeMode.REMOVE) {
            changed = -changed;
            mode = ChangeMode.ADD;
        }
        FabricLocation updated = switch (mode) {
            case ADD -> FabricLocationExpressionSupport.withAxis(location, axis, convert(location).doubleValue() + changed);
            case SET -> FabricLocationExpressionSupport.withAxis(location, axis, changed);
            default -> null;
        };
        if (updated != null) {
            getExpr().change(event, new FabricLocation[]{updated}, ChangeMode.SET);
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public Expression<? extends Number> simplify() {
        if (getExpr() instanceof Literal<? extends FabricLocation>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "the " + AXES[axis] + "-coordinate";
    }
}
