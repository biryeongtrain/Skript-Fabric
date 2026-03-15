package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.events.FabricEventCompatHandles;

public final class EvtMending extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on [item] mend[ing]"
    };

    public static String[] patterns() {
        return PATTERNS.clone();
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.Mending;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "on mending";
    }
}
