package ch.njol.skript.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingEventHandle;

public final class EvtServerListPing extends SimpleEvent {

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricServerListPingEventHandle;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "on server list ping";
    }
}
