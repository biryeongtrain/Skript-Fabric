package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtPressurePlate extends SkriptEvent {

    private boolean tripwire;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPressurePlate.class)) {
            return;
        }
        Skript.registerEvent(
                EvtPressurePlate.class,
                "[step[ping] on] [a] [pressure] plate",
                "(trip|[step[ping] on] [a] tripwire)"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        tripwire = matchedPattern == 1;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.PressurePlate handle
                && handle.tripwire() == tripwire;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.PressurePlate.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return tripwire ? "trip" : "stepping on a pressure plate";
    }
}
