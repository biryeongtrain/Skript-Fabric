package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Math2;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorderWarningTime extends SimplePropertyExpression<WorldBorder, Timespan> {

    private static final int DEFAULT_WARNING_TIME = 15;

    static {
        registerDefault(ExprWorldBorderWarningTime.class, Timespan.class, "world[ ]border warning time", "worldborders");
    }

    @Override
    public @Nullable Timespan convert(WorldBorder worldBorder) {
        return new Timespan(TimePeriod.SECOND, worldBorder.getWarningTime());
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Timespan.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        long input = mode == ChangeMode.RESET ? DEFAULT_WARNING_TIME : ((Timespan) delta[0]).getAs(TimePeriod.SECOND);
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            long warningTime = switch (mode) {
                case SET, RESET -> input;
                case ADD -> addClamped(worldBorder.getWarningTime(), input);
                case REMOVE -> addClamped(worldBorder.getWarningTime(), -input);
                default -> throw new IllegalStateException();
            };
            setWarningTime(worldBorder, warningTime);
        }
    }

    private static void setWarningTime(WorldBorder worldBorder, long inputTime) {
        long time = multiplyClamped(inputTime, 20L);
        int warningTime = ((int) Math2.fit(0L, time, Integer.MAX_VALUE)) / 20;
        worldBorder.setWarningTime(warningTime);
    }

    private static long addClamped(long left, long right) {
        try {
            return Math.addExact(left, right);
        } catch (ArithmeticException ignored) {
            return right >= 0L ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    private static long multiplyClamped(long left, long right) {
        try {
            return Math.multiplyExact(left, right);
        } catch (ArithmeticException ignored) {
            return (left < 0L) == (right < 0L) ? Long.MAX_VALUE : Long.MIN_VALUE;
        }
    }

    @Override
    public Class<? extends Timespan> getReturnType() {
        return Timespan.class;
    }

    @Override
    protected String getPropertyName() {
        return "world border warning time";
    }
}
