package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricReadyArrowEventHandle;

public final class EvtReadyArrow extends SkriptEvent {

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtReadyArrow.class)) {
            return;
        }
        Skript.registerEvent(
                EvtReadyArrow.class,
                "[player] (ready|draw|select)[ing] [an] arrow"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricReadyArrowEventHandle;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricReadyArrowEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "ready arrow";
    }
}
