package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtLevel extends SkriptEvent {

    private Kleenean leveling = Kleenean.UNKNOWN;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(EvtLevel.class, "[player] level (change|1¦up|-1¦down)");
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtLevel.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        leveling = parseResult.mark > 0 ? Kleenean.TRUE : parseResult.mark < 0 ? Kleenean.FALSE : Kleenean.UNKNOWN;
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.Level handle)) {
            return false;
        }
        if (leveling.isTrue()) {
            return handle.newLevel() > handle.oldLevel();
        }
        if (leveling.isFalse()) {
            return handle.newLevel() < handle.oldLevel();
        }
        return true;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.Level.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "level " + (leveling.isTrue() ? "up" : leveling.isFalse() ? "down" : "change");
    }
}
