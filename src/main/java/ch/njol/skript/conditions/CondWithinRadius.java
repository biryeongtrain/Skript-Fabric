package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Within Radius")
@Description("Checks whether a location is within a certain radius of another location.")
@Example("""
	on damage:
		if attacker's location is within 10 blocks around {_spawn}:
			cancel event
			send "You can't PVP in spawn."
	""")
@Since("2.7")
public class CondWithinRadius extends Condition {

    static {
        PropertyCondition.register(CondWithinRadius.class, "within %number% (block|metre|meter)[s] (around|of) %locations%", "locations");
    }

    private Expression<FabricLocation> locations;
    private Expression<Number> radius;
    private Expression<FabricLocation> points;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        locations = (Expression<FabricLocation>) exprs[0];
        radius = (Expression<Number>) exprs[1];
        points = (Expression<FabricLocation>) exprs[2];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        double resolvedRadius = radius.getOptionalSingle(event).orElse(0).doubleValue();
        double radiusSquared = resolvedRadius * resolvedRadius * Skript.EPSILON_MULT;
        return locations.check(event, location -> points.check(event, center ->
                location.level() == center.level() && location.position().distanceToSqr(center.position()) <= radiusSquared
        ), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return locations.toString(event, debug) + (locations.isSingle() ? " is " : " are ") + (isNegated() ? " not " : "")
                + "within " + radius.toString(event, debug) + " blocks around " + points.toString(event, debug);
    }
}
