package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import net.minecraft.world.phys.Vec3;

@Name("Is Normalized")
@Description("Checks whether a vector is normalized i.e. length of 1")
@Example("vector of player's location is normalized")
@Since("2.5.1")
public class CondIsVectorNormalized extends PropertyCondition<Vec3> {

    static {
        register(CondIsVectorNormalized.class, "normalized", "vectors");
    }

    @Override
    public boolean check(Vec3 vector) {
        return Math.abs(vector.lengthSqr() - 1.0D) <= 1.0E-10D;
    }

    public Condition simplify() {
        if (getExpr() instanceof Literal<? extends Vec3>) {
            return SimplifiedCondition.fromCondition(this);
        }
        return this;
    }

    @Override
    protected String getPropertyName() {
        return "normalized";
    }
}
