package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtExperienceChange extends SkriptEvent {

    private static final int ANY = 0;
    private static final int UP = 1;
    private static final int DOWN = 2;

    private int mode = ANY;

    public static synchronized void register() {
        if (isRegistered()) {
            return;
        }
        Skript.registerEvent(
                EvtExperienceChange.class,
                "[player] (level progress|[e]xp|experience) (change|update|:increase|:decrease)"
        );
    }

    private static boolean isRegistered() {
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EVENT)) {
            if (info.type() == EvtExperienceChange.class) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        if (parseResult.hasTag("increase")) {
            mode = UP;
        } else if (parseResult.hasTag("decrease")) {
            mode = DOWN;
        }
        return args.length == 0;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricPlayerEventHandles.ExperienceChange handle)) {
            return false;
        }
        if (mode == ANY) {
            return true;
        }
        return mode == UP ? handle.amount() > 0 : handle.amount() < 0;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricPlayerEventHandles.ExperienceChange.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "player level progress " + (mode == ANY ? "change" : mode == UP ? "increase" : "decrease");
    }
}
