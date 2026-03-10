package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public class ExprMiddleOfLocation extends SimplePropertyExpression<FabricLocation, FabricLocation> {

    static {
        register(ExprMiddleOfLocation.class, FabricLocation.class, "(middle|center) [point]", "location");
    }

    @Override
    public @Nullable FabricLocation convert(FabricLocation location) {
        return new FabricLocation(
                location.level(),
                new Vec3(
                        Math.floor(location.position().x) + 0.5,
                        Math.floor(location.position().y),
                        Math.floor(location.position().z) + 0.5
                )
        );
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    protected String getPropertyName() {
        return "middle point";
    }
}
