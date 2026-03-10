package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtWeatherChange extends SkriptEvent {

    private enum Weather {
        CLEAR,
        RAIN,
        THUNDER
    }

    private @Nullable Weather target;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtWeatherChange.class)) {
            return;
        }
        Skript.registerEvent(
                EvtWeatherChange.class,
                "weather change",
                "weather change to clear",
                "weather change to rain",
                "weather change to thunder"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        target = switch (matchedPattern) {
            case 1 -> Weather.CLEAR;
            case 2 -> Weather.RAIN;
            case 3 -> Weather.THUNDER;
            default -> null;
        };
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricEventCompatHandles.WeatherChange handle)) {
            return false;
        }
        if (target == null) {
            return true;
        }
        return switch (target) {
            case CLEAR -> !handle.rain() && !handle.thunder();
            case RAIN -> handle.rain() && !handle.thunder();
            case THUNDER -> handle.rain() && handle.thunder();
        };
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricEventCompatHandles.WeatherChange.class};
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "weather change" + (target == null ? "" : " to " + target.name().toLowerCase());
    }
}
