package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerChunkEnter extends SkriptEvent {

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtPlayerChunkEnter.class, "[player] (enter[s] [a] chunk|chunk enter[ing])");
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtPlayerChunkEnter.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Move handle)) {
            return false;
        }
        if (!(handle.entity() instanceof net.minecraft.server.level.ServerPlayer)) {
            return false;
        }
        return changedChunk(handle.from(), handle.to());
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Move.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "player enter chunk";
    }

    private static boolean changedChunk(@Nullable FabricLocation from, @Nullable FabricLocation to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.level() != to.level()) {
            return true;
        }
        int fromChunkX = ((int) Math.floor(from.position().x)) >> 4;
        int fromChunkZ = ((int) Math.floor(from.position().z)) >> 4;
        int toChunkX = ((int) Math.floor(to.position().x)) >> 4;
        int toChunkZ = ((int) Math.floor(to.position().z)) >> 4;
        return fromChunkX != toChunkX || fromChunkZ != toChunkZ;
    }
}
