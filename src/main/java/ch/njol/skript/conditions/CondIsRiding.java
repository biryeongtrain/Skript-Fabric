package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Riding")
@Description("Tests whether an entity is riding any entity, a specific entity type, or a specific entity.")
@Example("if player is riding:")
@Example("if player is riding an entity:")
@Example("if player is riding a saddled pig:")
@Example("if player is riding last spawned horse:")
@Since("2.0, 2.11 (entities)")
public class CondIsRiding extends Condition {

    static {
        PropertyCondition.register(CondIsRiding.class, "riding [%-entitydatas/entities%]", "entities");
    }

    private Expression<Entity> riders;
    private @Nullable Expression<?> riding;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        riders = (Expression<Entity>) exprs[0];
        riding = exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (riding == null) {
            return riders.check(event, rider -> rider.getVehicle() != null, isNegated());
        }
        Object[] targets = riding.getArray(event);
        return riders.check(event, rider -> {
            Entity vehicle = rider.getVehicle();
            if (vehicle == null) {
                return false;
            }
            for (Object object : targets) {
                if (object instanceof EntityData<?> entityData) {
                    if (entityData.isInstance(vehicle)) {
                        return true;
                    }
                } else if (object instanceof Entity entity && vehicle == entity) {
                    return true;
                }
            }
            return false;
        }, isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String property = "riding";
        if (riding != null) {
            property += " " + riding.toString(event, debug);
        }
        return PropertyCondition.toString(this, PropertyType.BE, event, debug, riders, property);
    }
}
