package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public final class EvtFirework extends SkriptEvent {

    private @Nullable Literal<Color> colors;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtFirework.class)) {
            return;
        }
        Skript.registerEvent(EvtFirework.class, "[a] firework explo(d(e|ing)|sion) [colo[u]red %-colors%]");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        colors = args[0] == null ? null : (Literal<Color>) args[0];
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.Firework handle)) {
            return false;
        }
        if (colors == null) {
            return true;
        }
        if (handle.colors() == null) {
            return false;
        }
        Set<Integer> expected = colors.stream(event)
                .map(Color::rgb)
                .collect(Collectors.toSet());
        return handle.colors().containsAll(expected);
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.Firework.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("firework explode");
        if (colors != null) {
            builder.append("with colors").append(colors);
        }
        return builder.toString();
    }
}
