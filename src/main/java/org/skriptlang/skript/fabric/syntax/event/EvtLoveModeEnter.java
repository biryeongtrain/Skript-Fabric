package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricLoveModeEnterEventHandle;

public final class EvtLoveModeEnter extends SimpleEvent {

    private static final String[] PATTERNS = {
            "on love mode enter",
            "on [entity] enter[s] love mode",
            "on [entity] love mode [enter]"
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
        return event.handle() instanceof FabricLoveModeEnterEventHandle;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricLoveModeEnterEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "on love mode enter";
    }
}
