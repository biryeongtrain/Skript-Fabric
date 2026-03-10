package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Incendiary")
@Description("Checks if an entity will create fire when it explodes. This condition is also usable in an explosion prime event.")
@Example("""
    on explosion prime:
        if the explosion is fiery:
            broadcast "A fiery explosive has been ignited!"
    """)
@Since("2.5")
public class CondIncendiary extends Condition {

    private static final @Nullable Class<?> EXPLOSION_PRIME_EVENT = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$ExplosionPrime");

    static {
        Skript.registerCondition(CondIncendiary.class,
                "%entities% ((is|are) incendiary|cause[s] a[n] (incendiary|fiery) explosion)",
                "%entities% ((is not|are not|isn't|aren't) incendiary|(does not|do not|doesn't|don't) cause[s] a[n] (incendiary|fiery) explosion)",
                "the [event(-| )]explosion (is|1¦(is not|isn't)) (incendiary|fiery)"
        );
    }

    private Expression<Entity> entities;
    private boolean eventVariant;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        eventVariant = matchedPattern == 2;
        if (eventVariant && (EXPLOSION_PRIME_EVENT == null || !getParser().isCurrentEvent(EXPLOSION_PRIME_EVENT))) {
            Skript.error("Checking if 'the explosion' is fiery is only possible in an explosion prime event");
            return false;
        }
        if (!eventVariant) {
            entities = (Expression<Entity>) exprs[0];
        }
        setNegated(matchedPattern == 1 || parseResult.mark == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (eventVariant) {
            return ConditionRuntimeSupport.booleanMethod(event.handle(), false, "causesFire", "getFire", "isIncendiary") ^ isNegated();
        }
        return entities.check(event,
                entity -> ConditionRuntimeSupport.booleanMethod(entity, false, "isIncendiary", "causesFire"),
                isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (eventVariant) {
            return "the event-explosion " + (isNegated() ? "is not" : "is") + " incendiary";
        }
        if (entities.isSingle()) {
            return entities.toString(event, debug) + (isNegated() ? " is not" : " is") + " incendiary";
        }
        return entities.toString(event, debug) + (isNegated() ? " are not" : " are") + " incendiary";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
