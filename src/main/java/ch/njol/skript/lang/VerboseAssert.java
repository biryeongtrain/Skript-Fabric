package ch.njol.skript.lang;

import ch.njol.skript.registrations.Classes;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Optional assertion metadata for conditions used in debug/testing output.
 */
public interface VerboseAssert {

    String getExpectedMessage(SkriptEvent event);

    String getReceivedMessage(SkriptEvent event);

    static String getExpressionValue(Expression<?> expression, SkriptEvent event) {
        return Classes.toString(expression.getAll(event), expression.getAnd());
    }
}
