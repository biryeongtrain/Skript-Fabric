package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorderDamageBuffer extends SimplePropertyExpression<WorldBorder, Double> {

    private static final double DEFAULT_DAMAGE_BUFFER = 5.0D;

    static {
        registerDefault(ExprWorldBorderDamageBuffer.class, Double.class, "world[ ]border damage buffer", "worldborders");
    }

    @Override
    public @Nullable Double convert(WorldBorder worldBorder) {
        return worldBorder.getSafeZone();
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
        double input = mode == ChangeMode.RESET ? DEFAULT_DAMAGE_BUFFER : ((Number) delta[0]).doubleValue();
        if (Double.isNaN(input)) {
            Skript.error("NaN is not a valid world border damage buffer");
            return;
        }
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            switch (mode) {
                case SET, RESET -> worldBorder.setSafeZone(Math.max(input, 0.0D));
                case ADD -> worldBorder.setSafeZone(Math.max(worldBorder.getSafeZone() + input, 0.0D));
                case REMOVE -> worldBorder.setSafeZone(Math.max(worldBorder.getSafeZone() - input, 0.0D));
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
        return "world border damage buffer";
    }
}
