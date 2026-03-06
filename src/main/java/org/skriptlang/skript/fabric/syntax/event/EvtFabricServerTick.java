package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricRuntimeHandle;

public final class EvtFabricServerTick extends SimpleEvent {

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() == FabricRuntimeHandle.SERVER_TICK;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "on server tick";
    }
}
