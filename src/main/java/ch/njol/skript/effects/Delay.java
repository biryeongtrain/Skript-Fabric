package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.GameTestRuntimeContext;
import org.skriptlang.skript.fabric.runtime.SkriptFabricTaskScheduler;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Delay")
@Description("Delays the script's execution by a given timespan.")
@Example("wait 2 minutes")
@Example("halt for 5 minecraft hours")
@Example("wait a tick")
@Since("1.4")
public class Delay extends Effect {

    private static boolean registered;
    private static final Set<SkriptEvent> DELAYED =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    protected Expression<Timespan> duration;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(Delay.class, "(wait|halt) [for] %timespan%");
        registered = true;
    }

    @Override
    @SuppressWarnings({"unchecked", "null"})
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        getParser().setHasDelayBefore(Kleenean.TRUE);
        duration = (Expression<Timespan>) exprs[0];
        if (duration instanceof Literal<Timespan> literal) {
            Timespan timespan = literal.getSingle(null);
            if (timespan != null) {
                if (timespan.isInfinite()) {
                    Skript.error("Delaying for an eternity is not allowed. Use the 'stop' effect instead.");
                    return false;
                }
                long millis = timespan.getAs(Timespan.TimePeriod.MILLISECOND);
                if (millis < 50L) {
                    Skript.warning("Delays less than one tick are not possible, defaulting to one tick.");
                }
            }
        }
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(SkriptEvent event) {
        debug(event, true);
        TriggerItem next = getNext();
        if (next == null || event.server() == null) {
            return null;
        }

        Timespan resolvedDuration = duration.getSingle(event);
        if (resolvedDuration == null) {
            return null;
        }

        Object localVariables = Variables.removeLocals(event);
        var helper = GameTestRuntimeContext.resolve(event);
        SkriptFabricTaskScheduler.schedule(event.server(), Math.max(resolvedDuration.getAs(Timespan.TimePeriod.TICK), 1L), () ->
                GameTestRuntimeContext.withHelper(helper, () -> {
                    addDelayedEvent(event);
                    if (localVariables != null) {
                        Variables.setLocalVariables(event, localVariables);
                    }
                    TriggerItem.walk(next, event);
                    Variables.removeLocals(event);
                }));
        return null;
    }

    @Override
    protected void execute(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "wait for " + duration.toString(event, debug) + (event == null ? "" : "...");
    }

    public static boolean isDelayed(SkriptEvent event) {
        return DELAYED.contains(event);
    }

    public static void addDelayedEvent(SkriptEvent event) {
        DELAYED.add(event);
    }
}
