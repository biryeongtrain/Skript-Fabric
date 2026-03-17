package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtBucket extends SkriptEvent {

    private boolean fill;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtBucket.class)) {
            return;
        }
        Skript.registerEvent(
                EvtBucket.class,
                "bucket empty[ing]",
                "[player] empty[ing] [a] bucket",
                "bucket fill[ing]",
                "[player] fill[ing] [a] bucket"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        fill = matchedPattern >= 2;
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.BucketUse handle)) {
            return false;
        }
        return handle.fill() == fill;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.BucketUse.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return fill ? "bucket fill" : "bucket empty";
    }
}
