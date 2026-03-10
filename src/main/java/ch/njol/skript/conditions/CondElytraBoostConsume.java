package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondElytraBoostConsume extends Condition {

    private static final @Nullable Class<?> ELYTRA_BOOST_EVENT = resolveEventClass(
            "ch.njol.skript.effects.FabricEffectEventHandles$PlayerElytraBoost"
    );

    static {
        Skript.registerCondition(
                CondElytraBoostConsume.class,
                "[the] (boosting|used) firework will be consumed",
                "[the] (boosting|used) firework (will not|won't) be consumed"
        );
    }

    private boolean checkConsume;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (ELYTRA_BOOST_EVENT == null || !getParser().isCurrentEvent(ELYTRA_BOOST_EVENT)) {
            Skript.error("This condition can only be used in an 'elytra boost' event.");
            return false;
        }
        checkConsume = matchedPattern == 0;
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return ConditionRuntimeSupport.booleanMethod(event.handle(), false, "shouldConsume", "consume", "isConsume")
                == checkConsume;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the boosting firework will " + (checkConsume ? "" : "not ") + "be consumed";
    }

    private static @Nullable Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
