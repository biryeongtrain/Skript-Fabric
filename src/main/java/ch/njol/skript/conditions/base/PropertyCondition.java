package ch.njol.skript.conditions.base;

import ch.njol.skript.lang.Expression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class PropertyCondition {

    public enum PropertyType {
        BE,
        HAVE,
        CAN
    }

    private PropertyCondition() {
    }

    public static String toString(
            Object ignored,
            PropertyType type,
            @Nullable SkriptEvent event,
            boolean debug,
            Expression<?> expression,
            String propertyName
    ) {
        return switch (type) {
            case BE -> expression.toString(event, debug) + " is " + propertyName;
            case HAVE -> expression.toString(event, debug) + " has " + propertyName;
            case CAN -> expression.toString(event, debug) + " can " + propertyName;
        };
    }
}
