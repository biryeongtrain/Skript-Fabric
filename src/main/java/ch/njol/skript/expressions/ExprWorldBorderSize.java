package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;

public class ExprWorldBorderSize extends SimplePropertyExpression<WorldBorder, Double> {

    private static final double MAX_SIZE = 59_999_968D;
    private boolean radius;

    static {
        registerDefault(ExprWorldBorderSize.class, Double.class, "world[ ]border (size|diameter|:radius)", "worldborders");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = parseResult.hasTag("radius");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Double convert(WorldBorder worldBorder) {
        return worldBorder.getSize() * (radius ? 0.5D : 1.0D);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        double input = mode == ChangeMode.RESET ? MAX_SIZE : ((Number) delta[0]).doubleValue() * (radius ? 2.0D : 1.0D);
        if (Double.isNaN(input)) {
            Skript.error("NaN is not a valid world border size");
            return;
        }
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            switch (mode) {
                case SET, RESET -> worldBorder.setSize(Math2.fit(1.0D, input, MAX_SIZE));
                case ADD -> worldBorder.setSize(Math2.fit(1.0D, worldBorder.getSize() + input, MAX_SIZE));
                case REMOVE -> worldBorder.setSize(Math2.fit(1.0D, worldBorder.getSize() - input, MAX_SIZE));
                default -> {
                }
            }
        }
    }

    @Override
    public Class<? extends Double> getReturnType() {
        return Double.class;
    }

    @Override
    protected String getPropertyName() {
        return "world border size";
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "world border " + (radius ? "radius" : "diameter") + " of " + getExpr().toString(event, debug);
    }
}
