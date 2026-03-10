package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public final class EvtWorld extends SkriptEvent {

    private @Nullable Literal<ServerLevel> worlds;
    private FabricEventCompatHandles.WorldAction action;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtWorld.class)) {
            return;
        }
        Skript.registerEvent(
                EvtWorld.class,
                "world sav(e|ing) [of %-worlds%]",
                "world init[ialization] [of %-worlds%]",
                "world unload[ing] [of %-worlds%]",
                "world load[ing] [of %-worlds%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        worlds = args.length > 0 ? (Literal<ServerLevel>) args[0] : null;
        if (worlds instanceof LiteralList<?> list && list.getAnd()) {
            ((LiteralList<ServerLevel>) worlds).invertAnd();
        }
        action = switch (matchedPattern) {
            case 0 -> FabricEventCompatHandles.WorldAction.SAVE;
            case 1 -> FabricEventCompatHandles.WorldAction.INIT;
            case 2 -> FabricEventCompatHandles.WorldAction.UNLOAD;
            default -> FabricEventCompatHandles.WorldAction.LOAD;
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.World handle) || handle.action() != action) {
            return false;
        }
        return worlds == null || worlds.check(event, world -> world == handle.world());
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.World.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "world save/init/unload/load" + (worlds == null ? "" : " of " + worlds.toString(event, debug));
    }
}
