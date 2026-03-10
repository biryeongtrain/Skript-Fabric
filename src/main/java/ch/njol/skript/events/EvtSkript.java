package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtSkript extends SkriptEvent {

    private static final String[] PATTERNS = {
            "on (:server|skript) (start|load|enable)",
            "on (:server|skript) (stop|unload|disable)"
    };

    private static final List<Trigger> START = Collections.synchronizedList(new ArrayList<>());
    private static final List<Trigger> STOP = Collections.synchronizedList(new ArrayList<>());

    private boolean start;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtSkript.class, PATTERNS);
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtSkript.class) {
                return true;
            }
        }
        return false;
    }

    public static void onSkriptStart() {
        fire(START);
    }

    public static void onSkriptStop() {
        fire(STOP);
    }

    private static void fire(List<Trigger> triggers) {
        synchronized (triggers) {
            for (Trigger trigger : triggers) {
                trigger.execute(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY);
            }
            triggers.clear();
        }
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        start = matchedPattern == 0;
        if (parseResult.hasTag("server")) {
            Skript.warning(
                    "Server start/stop events are actually called when Skript is started or stopped."
                            + " It is thus recommended to use 'on Skript start/stop' instead."
            );
        }
        return args.length == 0;
    }

    @Override
    public boolean postLoad() {
        (start ? START : STOP).add(trigger);
        return true;
    }

    @Override
    public void unload() {
        (start ? START : STOP).remove(trigger);
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
        return "on skript " + (start ? "start" : "stop");
    }
}
