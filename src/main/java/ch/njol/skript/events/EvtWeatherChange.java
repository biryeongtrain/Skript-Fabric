package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;

public final class EvtWeatherChange extends SkriptEvent {

    private @Nullable FabricEventCompatHandles.WeatherType target;

    public static synchronized void register() {
        EventClassInfoRegistrar.register();
        if (EventSyntaxRegistry.isRegistered(EvtWeatherChange.class)) {
            return;
        }
        Skript.registerEvent(
                EvtWeatherChange.class,
                "weather change [to %-weathertypes%]"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parser) {
        if (args.length > 0 && args[0] != null) {
            target = ((Literal<FabricEventCompatHandles.WeatherType>) args[0]).getSingle(null);
        }
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
