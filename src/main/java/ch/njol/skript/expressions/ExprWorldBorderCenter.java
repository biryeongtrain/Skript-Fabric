package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWorldBorderCenter extends SimplePropertyExpression<WorldBorder, FabricLocation> {

    private static final double MAX_CENTER_COORDINATE = 29_999_984D;

    static {
        registerDefault(ExprWorldBorderCenter.class, FabricLocation.class, "world[ ]border (center|middle)", "worldborders");
    }

    @Override
    public @Nullable FabricLocation convert(WorldBorder worldBorder) {
        return new FabricLocation(null, new Vec3(worldBorder.getCenterX(), 0.0D, worldBorder.getCenterZ()));
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{FabricLocation.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (WorldBorder worldBorder : getExpr().getArray(event)) {
            if (mode == ChangeMode.RESET) {
                worldBorder.setCenter(0.0D, 0.0D);
                continue;
            }
            if (delta == null || delta.length == 0 || !(delta[0] instanceof FabricLocation location)) {
                continue;
            }
            double x = location.position().x;
            double z = location.position().z;
            if (Double.isNaN(x) || Double.isNaN(z)) {
                Skript.error("Your location can't have a NaN value as one of its components");
                return;
            }
            x = Math.max(-MAX_CENTER_COORDINATE, Math.min(MAX_CENTER_COORDINATE, x));
            z = Math.max(-MAX_CENTER_COORDINATE, Math.min(MAX_CENTER_COORDINATE, z));
            worldBorder.setCenter(x, z);
        }
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "world border center";
    }
}
