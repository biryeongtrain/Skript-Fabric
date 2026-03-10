package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

@Name("Middle of Location")
@Description("Returns the center of a location.")
@Example("teleport player to the center of player's location")
@Since("2.6.1")
public class ExprMiddleOfLocation extends SimplePropertyExpression<FabricLocation, FabricLocation> {

    static {
        register(ExprMiddleOfLocation.class, FabricLocation.class, "(middle|center) [point]", "location");
    }

    @Override
    public @Nullable FabricLocation convert(FabricLocation location) {
        return FabricLocationExpressionSupport.centered(location);
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public Expression<? extends FabricLocation> simplify() {
        if (getExpr() instanceof Literal<? extends FabricLocation>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "middle point";
    }
}
