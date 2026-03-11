package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprWeather extends PropertyExpression<ServerLevel, ExprWeather.WeatherKind> {

    private static final int WEATHER_DURATION = 6000;

    static {
        ensureWeatherClassInfo();
        Skript.registerExpression(
                ExprWeather.class,
                WeatherKind.class,
                "[the] weather [(in|of) %worlds%]",
                "%worlds%'[s] weather"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        setExpr((Expression<ServerLevel>) expressions[0]);
        return true;
    }

    @Override
    protected WeatherKind[] get(SkriptEvent event, ServerLevel[] source) {
        if (getTime() >= 0
                && event.level() != null
                && event.handle() instanceof FabricEventCompatHandles.WeatherChange handle) {
            return get(source, world -> world == event.level()
                    ? WeatherKind.fromState(handle.rain(), handle.thunder())
                    : WeatherKind.fromWorld(world));
        }
        return get(source, WeatherKind::fromWorld);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case DELETE, RESET, SET -> new Class[]{WeatherKind.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        WeatherKind weather;
        if (mode == ChangeMode.SET) {
            weather = delta != null && delta.length > 0 && delta[0] instanceof WeatherKind value ? value : null;
        } else {
            weather = WeatherKind.CLEAR;
        }
        if (weather == null) {
            return;
        }
        for (ServerLevel world : getExpr().getArray(event)) {
            weather.apply(world);
        }
    }

    @Override
    public Class<WeatherKind> getReturnType() {
        return WeatherKind.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "weather of " + getExpr().toString(event, debug);
    }

    private static void ensureWeatherClassInfo() {
        if (Classes.getExactClassInfo(WeatherKind.class) != null || Classes.getClassInfoNoError("weather") != null) {
            return;
        }
        ClassInfo<WeatherKind> info = new ClassInfo<>(WeatherKind.class, "weather");
        info.user("weathers?");
        info.supplier(WeatherKind.values());
        info.parser(new WeatherParser());
        Classes.registerClassInfo(info);
    }

    private static String normalize(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int index = 0; index < input.length(); index++) {
            char character = Character.toLowerCase(input.charAt(index));
            if ((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9')) {
                builder.append(character);
            }
        }
        return builder.toString();
    }

    public enum WeatherKind {
        CLEAR("clear", "sunny"),
        RAIN("rain", "rainy"),
        THUNDER("thunder", "storm", "thunderstorm");

        private final String displayName;
        private final String[] aliases;

        WeatherKind(String displayName, String... aliases) {
            this.displayName = displayName;
            this.aliases = aliases;
        }

        static WeatherKind fromWorld(ServerLevel world) {
            return fromState(world.isRaining(), world.isThundering());
        }

        static WeatherKind fromState(boolean rain, boolean thunder) {
            if (thunder) {
                return THUNDER;
            }
            if (rain) {
                return RAIN;
            }
            return CLEAR;
        }

        void apply(ServerLevel world) {
            switch (this) {
                case CLEAR -> {
                    world.setWeatherParameters(WEATHER_DURATION, 0, false, false);
                    world.setRainLevel(0.0F);
                    world.setThunderLevel(0.0F);
                }
                case RAIN -> {
                    world.setWeatherParameters(0, WEATHER_DURATION, true, false);
                    world.setRainLevel(1.0F);
                    world.setThunderLevel(0.0F);
                }
                case THUNDER -> {
                    world.setWeatherParameters(0, WEATHER_DURATION, true, true);
                    world.setRainLevel(1.0F);
                    world.setThunderLevel(1.0F);
                }
            }
        }

        static @Nullable WeatherKind parse(String input) {
            String normalized = normalize(input);
            for (WeatherKind value : values()) {
                if (normalize(value.displayName).equals(normalized)) {
                    return value;
                }
                for (String alias : value.aliases) {
                    if (normalize(alias).equals(normalized)) {
                        return value;
                    }
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final class WeatherParser implements ClassInfo.Parser<WeatherKind> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable WeatherKind parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            return WeatherKind.parse(input);
        }
    }
}
