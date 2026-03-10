package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import org.skriptlang.skript.fabric.compat.FabricLocation;

@Name("Altitude")
@Description("Effectively an alias of the y-coordinate of a location.")
@Example("altitude of player's location")
@Since("1.4.3")
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

    @Override
    public Expression<? extends Number> simplify() {
        if (getExpr() instanceof Literal<? extends FabricLocation>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }
}
