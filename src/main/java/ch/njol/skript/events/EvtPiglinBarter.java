package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtPiglinBarter extends SkriptEvent {

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPiglinBarter.class)) {
            return;
        }
        Skript.registerEvent(EvtPiglinBarter.class, "piglin (barter[ing]|trad(e|ing))");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.PiglinBarter;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.PiglinBarter.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "piglin barter";
    }
}
