package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class CondHasMetadata extends Condition {

    static {
        Skript.registerCondition(
                CondHasMetadata.class,
                "%objects% (has|have) metadata [(value|tag)[s]] %strings%",
                "%objects% (doesn't|does not|do not|don't) have metadata [(value|tag)[s]] %strings%"
        );
    }

    private Expression<Object> holders;
    private Expression<String> values;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        holders = (Expression<Object>) exprs[0];
        values = (Expression<String>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return holders.check(event, holder -> values.check(event, value -> hasMetadata(holder, value)), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return PropertyCondition.toString(
                this,
                PropertyType.HAVE,
                event,
                debug,
                holders,
                "metadata " + (values.isSingle() ? "value " : "values ") + values.toString(event, debug)
        );
    }

    private static boolean hasMetadata(@Nullable Object holder, @Nullable String value) {
        if (holder == null || value == null) {
            return false;
        }
        if (holder instanceof Map<?, ?> map) {
            return map.containsKey(value);
        }
        if (ConditionRuntimeSupport.booleanMethod(holder, new Object[]{value}, false, "hasMetadata", "hasTag", "contains")) {
            return true;
        }
        Object result = ConditionRuntimeSupport.invokeCompatible(holder, "metadata", "getMetadata");
        if (result instanceof Map<?, ?> map) {
            return map.containsKey(value);
        }
        return false;
    }
}
