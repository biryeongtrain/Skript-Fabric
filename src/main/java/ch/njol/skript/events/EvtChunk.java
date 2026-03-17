package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtChunk extends SkriptEvent {

    private boolean load;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtChunk.class)) {
            return;
        }
        Skript.registerEvent(EvtChunk.class,
                "chunk load[ing]",
                "chunk generat(e|ing)",
                "chunk unload[ing]"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        load = matchedPattern <= 1; // 0 = load, 1 = generate (both are load events), 2 = unload
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (load) {
            return event.handle() instanceof FabricEventCompatHandles.ChunkLoad;
        }
        return event.handle() instanceof FabricEventCompatHandles.ChunkUnload;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return load
                ? new Class<?>[]{FabricEventCompatHandles.ChunkLoad.class}
                : new Class<?>[]{FabricEventCompatHandles.ChunkUnload.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return load ? "chunk load" : "chunk unload";
    }
}
