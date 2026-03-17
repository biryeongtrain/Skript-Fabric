package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtBed extends SkriptEvent {

    private boolean enter;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtBed.class)) {
            return;
        }
        Skript.registerEvent(
                EvtBed.class,
                "bed enter[ing]",
                "[player] enter[ing] [a] bed",
                "bed leav(e|ing)",
                "[player] leav(e|ing) [a] bed"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        enter = matchedPattern <= 1;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (enter) {
            return event.handle() instanceof FabricEventCompatHandles.BedEnter;
        } else {
            return event.handle() instanceof FabricEventCompatHandles.BedLeave;
        }
    }

    @Override
    public Class<?>[] getEventClasses() {
        if (enter) {
            return new Class<?>[]{FabricEventCompatHandles.BedEnter.class};
        }
        return new Class<?>[]{FabricEventCompatHandles.BedLeave.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return enter ? "bed enter" : "bed leave";
    }
}
