package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtBeaconToggle extends SkriptEvent {

    private boolean activate;
    private boolean toggle;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtBeaconToggle.class)) {
            return;
        }
        Skript.registerEvent(
                EvtBeaconToggle.class,
                "beacon toggle",
                "beacon activat(e|ion)",
                "beacon deactivat(e|ion)"
        );
    }

    @Override
    public boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
        toggle = matchedPattern == 0;
        activate = matchedPattern == 1;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BeaconToggle handle)) {
            return false;
        }
        return toggle || handle.activated() == activate;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.BeaconToggle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "beacon " + (toggle ? "toggle" : activate ? "activate" : "deactivate");
    }
}
