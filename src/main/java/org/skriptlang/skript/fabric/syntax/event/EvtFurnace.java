package org.skriptlang.skript.fabric.syntax.event;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceEventHandle;

public final class EvtFurnace extends SimpleEvent {

    private FabricFurnaceEventHandle.Kind kind;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        kind = switch (matchedPattern) {
            case 0 -> FabricFurnaceEventHandle.Kind.SMELT;
            case 1 -> FabricFurnaceEventHandle.Kind.BURN;
            case 2 -> FabricFurnaceEventHandle.Kind.START_SMELT;
            case 3 -> FabricFurnaceEventHandle.Kind.EXTRACT;
            default -> FabricFurnaceEventHandle.Kind.SMELT;
        };
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricFurnaceEventHandle handle && handle.kind() == kind;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricFurnaceEventHandle.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return switch (kind) {
            case SMELT -> "on furnace smelt";
            case BURN -> "on fuel burn";
            case START_SMELT -> "on smelting start";
            case EXTRACT -> "on furnace extract";
        };
    }
}
