package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.GameruleValue;
import ch.njol.util.Kleenean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprGameRule extends SimpleExpression<GameruleValue> {

    static {
        ensureGameRuleClassInfo();
        Skript.registerExpression(ExprGameRule.class, GameruleValue.class, "[the] gamerule %gamerule% of %worlds%");
    }

    private Expression<GameRules.Key<?>> gamerule;
    private Expression<ServerLevel> worlds;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        gamerule = (Expression<GameRules.Key<?>>) exprs[0];
        worlds = (Expression<ServerLevel>) exprs[1];
        return true;
    }

    @Override
    protected GameruleValue @Nullable [] get(SkriptEvent event) {
        GameRules.Key<?> key = gamerule.getSingle(event);
        if (key == null) {
            return null;
        }

        ServerLevel[] levels = worlds.getArray(event);
        GameruleValue<?>[] values = new GameruleValue<?>[levels.length];
        for (int index = 0; index < levels.length; index++) {
            Object value = readValue(levels[index].getGameRules().getRule(key));
            if (value == null) {
                return null;
            }
            values[index] = new GameruleValue<>(value);
        }
        return values;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Boolean.class, Integer.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0) {
            return;
        }

        GameRules.Key<?> key = gamerule.getSingle(event);
        if (key == null) {
            return;
        }

        Object value = delta[0];
        for (ServerLevel level : worlds.getArray(event)) {
            GameRules.Value<?> rule = level.getGameRules().getRule(key);
            if (rule instanceof GameRules.BooleanValue booleanValue) {
                if (!(value instanceof Boolean flag)) {
                    Skript.error("The " + key.getId() + " gamerule can only be set to a boolean value.");
                    return;
                }
                booleanValue.set(flag, level.getServer());
                continue;
            }
            if (rule instanceof GameRules.IntegerValue integerValue) {
                if (!(value instanceof Integer number)) {
                    Skript.error("The " + key.getId() + " gamerule can only be set to an integer value.");
                    return;
                }
                integerValue.set(number, level.getServer());
                continue;
            }
            Skript.error("The " + key.getId() + " gamerule is not supported on this compatibility surface.");
            return;
        }
    }

    @Override
    public boolean isSingle() {
        return worlds.isSingle();
    }

    @Override
    public Class<? extends GameruleValue> getReturnType() {
        return GameruleValue.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the gamerule " + gamerule.toString(event, debug) + " of " + worlds.toString(event, debug);
    }

    private static @Nullable Object readValue(GameRules.Value<?> value) {
        if (value instanceof GameRules.BooleanValue booleanValue) {
            return booleanValue.get();
        }
        if (value instanceof GameRules.IntegerValue integerValue) {
            return integerValue.get();
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void ensureGameRuleClassInfo() {
        if (Classes.getExactClassInfo((Class) GameRules.Key.class) != null || Classes.getClassInfoNoError("gamerule") != null) {
            return;
        }
        ClassInfo<GameRules.Key<?>> info = new ClassInfo<>((Class) GameRules.Key.class, "gamerule");
        info.user("game ?rules?");
        info.supplier(() -> allGameRules().iterator());
        info.parser(new GameRuleParser());
        Classes.registerClassInfo(info);
    }

    private static List<GameRules.Key<?>> allGameRules() {
        List<GameRules.Key<?>> keys = new ArrayList<>();
        for (Field field : GameRules.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !GameRules.Key.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof GameRules.Key<?> key) {
                    keys.add(key);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        keys.sort(Comparator.comparing(GameRules.Key::getId));
        return keys;
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

    private static final class GameRuleParser implements ClassInfo.Parser<GameRules.Key<?>> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable GameRules.Key<?> parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = normalize(input);
            for (GameRules.Key<?> key : allGameRules()) {
                if (normalize(key.getId()).equals(normalized)) {
                    return key;
                }
            }
            return null;
        }
    }
}
