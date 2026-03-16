package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.util.Locale;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

public final class EvtGameMode extends SkriptEvent {

    private @Nullable GameType mode;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtGameMode.class)) {
            return;
        }
        Skript.registerEvent(
                EvtGameMode.class,
                "game[ ]mode change [to %gamemode%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        if (args.length > 0 && args[0] != null) {
            mode = ((Literal<GameType>) args[0]).getSingle(null);
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        return event.handle() instanceof FabricEventCompatHandles.GameMode handle
                && (mode == null || mode == handle.mode());
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.GameMode.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "gamemode change" + (mode != null ? " to " + mode.name().toLowerCase(Locale.ENGLISH) : "");
    }
}
