package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public class EvtScript extends SkriptEvent {

    private static final String[] PATTERNS = {
            "on [:async] [script] (load|init|enable)",
            "on [:async] [script] (unload|stop|disable)"
    };

    private static boolean registered;

    private boolean async;
    private boolean load;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEvent(EvtScript.class, PATTERNS);
        registered = true;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        async = parseResult.hasTag("async");
        load = matchedPattern == 0;
        return args.length == 0;
    }

    @Override
    public boolean postLoad() {
        if (load) {
            trigger.execute(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY);
        }
        return true;
    }

    @Override
    public void unload() {
        if (!load) {
            trigger.execute(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY);
        }
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEventPrioritySupported() {
        return false;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return (async ? "async " : "") + "script " + (load ? "" : "un") + "load";
    }
}
