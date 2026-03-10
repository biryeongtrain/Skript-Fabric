package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public class ExprAltitude extends SimplePropertyExpression<FabricLocation, Number> {

    static {
        register(ExprAltitude.class, Number.class, "altitude[s]", "locations");
    }

    @Override
    public Number convert(FabricLocation location) {
        return location.position().y;
    }

    @Override
    protected String getPropertyName() {
        return "altitude";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
