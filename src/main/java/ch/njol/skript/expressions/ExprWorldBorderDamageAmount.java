package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorderDamageAmount extends SimplePropertyExpression<WorldBorder, Double> {

    private static final double DEFAULT_DAMAGE_AMOUNT = 0.2D;

    static {
        registerDefault(ExprWorldBorderDamageAmount.class, Double.class, "world[ ]border damage amount", "worldborders");
    }

    @Override
    public @Nullable Double convert(WorldBorder worldBorder) {
        return worldBorder.getDamagePerBlock();
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
        double input = mode == ChangeMode.RESET ? DEFAULT_DAMAGE_AMOUNT : ((Number) delta[0]).doubleValue();
        if (Double.isNaN(input)) {
            Skript.error("NaN is not a valid world border damage amount");
            return;
        }
        if (Double.isInfinite(input)) {
            Skript.error("World border damage amount cannot be infinite");
            return;
        }
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            switch (mode) {
                case SET, RESET -> worldBorder.setDamagePerBlock(Math.max(input, 0.0D));
                case ADD -> worldBorder.setDamagePerBlock(Math.max(worldBorder.getDamagePerBlock() + input, 0.0D));
                case REMOVE -> worldBorder.setDamagePerBlock(Math.max(worldBorder.getDamagePerBlock() - input, 0.0D));
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
        return "world border damage amount";
    }
}
