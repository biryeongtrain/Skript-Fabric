package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondResourcePack extends Condition {

    static {
        Skript.registerCondition(
                CondResourcePack.class,
                "[the] resource pack (was|is|has) [been] %strings%",
                "[the] resource pack (was|is|has)(n't| not) [been] %strings%"
        );
    }

    private Expression<String> states;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricEventCompatHandles.ResourcePackResponse.class)) {
            Skript.error("The resource pack condition can't be used outside of a resource pack response event");
            return false;
        }
        states = (Expression<String>) exprs[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.ResourcePackResponse response)) {
            return isNegated();
        }
        String state = String.valueOf(response.status());
        return states.check(event, candidate -> ConditionRuntimeSupport.normalizeToken(candidate)
                .equals(ConditionRuntimeSupport.normalizeToken(state)), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "resource pack was " + (isNegated() ? "not " : "") + states.toString(event, debug);
    }
}
