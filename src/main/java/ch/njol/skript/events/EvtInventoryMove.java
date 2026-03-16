package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricInventoryMoveEventHandle;

public final class EvtInventoryMove extends SkriptEvent {

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtInventoryMove.class)) {
            return;
        }
        Skript.registerEvent(
                EvtInventoryMove.class,
                "inventory item (move|transport)",
                "inventory (mov(e|ing)|transport[ing]) [an] item"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricInventoryMoveEventHandle;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricInventoryMoveEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "inventory item move";
    }
}
