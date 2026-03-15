package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.events.FabricEventCompatHandles;

public final class EvtInventoryMove extends SimpleEvent {

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.Item item
                && item.action() == FabricEventCompatHandles.ItemAction.INVENTORY_MOVE;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "on inventory item move";
    }
}
