package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.events.bukkit.ScriptEvent;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtScript extends SkriptEvent {

    private static final String[] PATTERNS = {
            "on [:async] [script] (load|init|enable)",
            "on [:async] [script] (unload|stop|disable)"
    };

    private boolean async;
    private boolean load;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtScript.class, PATTERNS);
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtScript.class) {
                return true;
            }
        }
        return false;
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
            trigger.execute(new org.skriptlang.skript.lang.event.SkriptEvent(new ScriptEvent(), null, null, null));
        }
        return true;
    }

    @Override
    public void unload() {
        if (!load) {
            trigger.execute(new org.skriptlang.skript.lang.event.SkriptEvent(new ScriptEvent(), null, null, null));
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
