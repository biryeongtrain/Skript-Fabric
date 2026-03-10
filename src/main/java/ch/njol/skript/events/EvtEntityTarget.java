package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtEntityTarget extends SkriptEvent {

    private boolean target;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtEntityTarget.class)) {
            return;
        }
        Skript.registerEvent(EvtEntityTarget.class, "[entity] target", "[entity] un[-]target");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        target = matchedPattern == 0;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.EntityTarget handle
                && ((handle.target() != null) == target);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.EntityTarget.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "entity " + (target ? "" : "un") + "target";
    }
}
