package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Provided for convenience: one can write 'event-world' instead of only 'world' to distinguish between the
 * event-world and the loop-world.
 */
@NoDoc
public class ExprEventExpression extends WrapperExpression<Object> {

    static {
        Skript.registerExpression(ExprEventExpression.class, Object.class, "[the] event-%*classinfo%");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        ClassInfo<?> classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle(null);
        if (classInfo == null) {
            return false;
        }
        Class<?> type = classInfo.getC();
        boolean plural = isPluralReference(parser.expr, classInfo);
        EventValueExpression<?> eventValue = new EventValueExpression<>(plural ? arrayType(type) : type);
        setExpr(eventValue);
        return eventValue.init();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug);
    }

    private static boolean isPluralReference(String input, ClassInfo<?> classInfo) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ENGLISH);
        String plural = classInfo.getName().getPlural().toLowerCase(Locale.ENGLISH);
        String singular = classInfo.getName().getSingular().toLowerCase(Locale.ENGLISH);
        return normalized.contains(plural) && !normalized.equals("event-" + singular);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> arrayType(Class<?> type) {
        return (Class<T>) Array.newInstance(type, 0).getClass();
    }
}
