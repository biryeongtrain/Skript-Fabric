package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.phys.Vec3;

public class ExprVectorSquaredLength extends SimplePropertyExpression<Vec3, Number> {

    static {
        register(ExprVectorSquaredLength.class, Number.class, "squared length[s]", "vectors");
    }

    @Override
    public Number convert(Vec3 vector) {
        return vector.lengthSqr();
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "squared length of vector";
    }
}
