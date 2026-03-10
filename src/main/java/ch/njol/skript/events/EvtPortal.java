package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtPortal extends SkriptEvent {

    private boolean player;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPortal.class)) {
            return;
        }
        Skript.registerEvent(EvtPortal.class, "[player] portal", "entity portal");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        player = matchedPattern == 0;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.Portal handle && handle.player() == player;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Portal.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return (player ? "player" : "entity") + " portal";
    }
}
