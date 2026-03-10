package ch.njol.skript.expressions.base;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class EventValueExpression<T> extends SimpleExpression<T> implements DefaultExpression<T> {

    public static <T> void register(Class<? extends EventValueExpression<T>> expression, Class<T> type, String... patterns) {
        String[] normalized = new String[patterns.length];
        for (int i = 0; i < patterns.length; i++) {
            normalized[i] = patterns[i].startsWith("[the] ") ? patterns[i] : "[the] " + patterns[i];
        }
        Skript.registerExpression((Class) expression, type, normalized);
    }

    private final Class<? extends T> type;
    private final boolean single;
    private int time;

    public EventValueExpression(Class<? extends T> type) {
        this(type, !type.isArray());
    }

    public EventValueExpression(Class<? extends T> type, boolean single) {
        this.type = type;
        this.single = single;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        return expressions.length == 0 && init();
    }

    @Override
    public boolean init() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T[] get(SkriptEvent event) {
        Object handle = event.handle();
        if (handle == null || !type.isInstance(handle)) {
            return (T[]) Array.newInstance(type, 0);
        }
        T[] values = (T[]) Array.newInstance(type, 1);
        values[0] = type.cast(handle);
        return values;
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    @Override
    public Class<? extends T> getReturnType() {
        return type;
    }

    @Override
    public boolean setTime(int time) {
        this.time = time;
        return true;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return type.getSimpleName();
    }
}
