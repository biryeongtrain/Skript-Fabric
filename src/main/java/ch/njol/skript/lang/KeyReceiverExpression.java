package ch.njol.skript.lang;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface KeyReceiverExpression<T> extends Expression<T> {

    default boolean acceptsNestedStructures() {
        return false;
    }

    void change(SkriptEvent event, Object @NotNull [] delta, ChangeMode mode, @NotNull String @NotNull [] keys);
}
