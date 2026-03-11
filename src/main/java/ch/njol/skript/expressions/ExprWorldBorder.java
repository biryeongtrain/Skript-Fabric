package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorder extends SimplePropertyExpression<Object, WorldBorder> {

    private static final double DEFAULT_SIZE = 59_999_968D;
    private static final double DEFAULT_DAMAGE_AMOUNT = 0.2D;
    private static final double DEFAULT_DAMAGE_BUFFER = 5.0D;
    private static final int DEFAULT_WARNING_DISTANCE = 5;
    private static final int DEFAULT_WARNING_TIME = 15;

    static {
        registerDefault(ExprWorldBorder.class, WorldBorder.class, "world[ ]border", "worlds/players");
    }

    @Override
    public @Nullable WorldBorder convert(Object object) {
        if (object instanceof WorldBorder worldBorder) {
            return worldBorder;
        }
        if (object instanceof ServerLevel world) {
            return world.getWorldBorder();
        }
        if (object instanceof ServerPlayer player && player.level() instanceof ServerLevel level) {
            return level.getWorldBorder();
        }
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET || mode == ChangeMode.RESET ? new Class[]{WorldBorder.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (Object source : getExpr().getArray(event)) {
            WorldBorder border = convert(source);
            if (border == null) {
                continue;
            }
            if (mode == ChangeMode.RESET) {
                reset(border);
                continue;
            }
            if (delta != null && delta.length > 0 && delta[0] instanceof WorldBorder other) {
                copy(other, border);
            }
        }
    }

    private static void copy(WorldBorder from, WorldBorder to) {
        to.setCenter(from.getCenterX(), from.getCenterZ());
        to.setSize(from.getSize());
        to.setDamagePerBlock(from.getDamagePerBlock());
        to.setDamageSafeZone(from.getDamageSafeZone());
        to.setWarningBlocks(from.getWarningBlocks());
        to.setWarningTime(from.getWarningTime());
    }

    private static void reset(WorldBorder border) {
        border.setCenter(0.0D, 0.0D);
        border.setSize(DEFAULT_SIZE);
        border.setDamagePerBlock(DEFAULT_DAMAGE_AMOUNT);
        border.setDamageSafeZone(DEFAULT_DAMAGE_BUFFER);
        border.setWarningBlocks(DEFAULT_WARNING_DISTANCE);
        border.setWarningTime(DEFAULT_WARNING_TIME);
    }

    @Override
    public Class<? extends WorldBorder> getReturnType() {
        return WorldBorder.class;
    }

    @Override
    protected String getPropertyName() {
        return "world border";
    }
}
