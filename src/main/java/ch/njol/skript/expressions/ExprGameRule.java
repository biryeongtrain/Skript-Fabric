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
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprGameRule extends SimpleExpression<GameruleValue> {

    static {
        ensureGameRuleClassInfo();
        Skript.registerExpression(ExprGameRule.class, GameruleValue.class, "[the] gamerule %gamerule% of %worlds%");
    }

    private Expression<GameRule<Object>> gamerule;
    private Expression<ServerLevel> worlds;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        gamerule = (Expression<GameRule<Object>>) exprs[0];
        worlds = (Expression<ServerLevel>) exprs[1];
        return true;
    }

    @Override
    protected GameruleValue @Nullable [] get(SkriptEvent event) {
        GameRule<?> key = gamerule.getSingle(event);
        if (key == null) {
            return null;
        }

        ServerLevel[] levels = worlds.getArray(event);
        GameruleValue<?>[] values = new GameruleValue<?>[levels.length];
        for (int index = 0; index < levels.length; index++) {
            Object value = readValue(levels[index].getGameRules().get(key));
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

        GameRule<Object> key = gamerule.getSingle(event);
        if (key == null) {
            return;
        }

        Object value = delta[0];
        for (ServerLevel level : worlds.getArray(event)) {
            if (value instanceof Boolean booleanValue) {
                level.getGameRules().set(key, booleanValue, level.getServer());
                continue;
            }
            if (value instanceof Integer integerValue) {
                level.getGameRules().set(key, integerValue, level.getServer());
                continue;
            }
            Skript.error("The " + key.id() + " gamerule is not supported on this compatibility surface.");
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

    private static @Nullable Object readValue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void ensureGameRuleClassInfo() {
        if (Classes.getExactClassInfo((Class) GameRule.class) != null || Classes.getClassInfoNoError("gamerule") != null) {
            return;
        }
        ClassInfo<GameRule<?>> info = new ClassInfo<>((Class) GameRule.class, "gamerule");
        info.user("game ?rules?");
        info.supplier(() -> allGameRules().iterator());
        info.parser(new GameRuleParser());
        Classes.registerClassInfo(info);
    }

    private static List<GameRule<?>> allGameRules() {
        List<GameRule<?>> keys = new ArrayList<>();
        for (Field field : GameRules.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !GameRule.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof GameRule<?> key) {
                    keys.add(key);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        keys.sort(Comparator.comparing(GameRule::id));
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

    private static final class GameRuleParser implements ClassInfo.Parser<GameRule<?>> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable GameRule<?> parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String normalized = normalize(input);
            for (GameRule<?> key : allGameRules()) {
                if (normalize(key.id()).equals(normalized)) {
                    return key;
                }
            }
            return null;
        }
    }
}
