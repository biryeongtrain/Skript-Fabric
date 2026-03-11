package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorderWarningDistance extends SimplePropertyExpression<WorldBorder, Integer> {

    private static final int DEFAULT_WARNING_DISTANCE = 5;

    static {
        registerDefault(ExprWorldBorderWarningDistance.class, Integer.class, "world[ ]border warning distance", "worldborders");
    }

    @Override
    public @Nullable Integer convert(WorldBorder worldBorder) {
        return worldBorder.getWarningBlocks();
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
        int input = mode == ChangeMode.RESET ? DEFAULT_WARNING_DISTANCE : ((Number) delta[0]).intValue();
        if (mode != ChangeMode.RESET && Double.isNaN(((Number) delta[0]).doubleValue())) {
            Skript.error("NaN is not a valid world border warning distance");
            return;
        }
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            switch (mode) {
                case SET, RESET -> worldBorder.setWarningBlocks(Math.max(input, 0));
                case ADD -> {
                    long next = (long) worldBorder.getWarningBlocks() + input;
                    worldBorder.setWarningBlocks((int) Math.max(0L, Math.min(Integer.MAX_VALUE, next)));
                }
                case REMOVE -> {
                    long next = (long) worldBorder.getWarningBlocks() - input;
                    worldBorder.setWarningBlocks((int) Math.max(0L, Math.min(Integer.MAX_VALUE, next)));
                }
                default -> {
                }
            }
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "world border warning distance";
    }
}
