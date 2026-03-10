package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Sets")
@Description("Returns a list of all the values of a type. Useful for looping.")
@Example("""
	loop all attribute types:
		set loop-value attribute of player to 10
		message "Set attribute %loop-value% to 10!"
	""")
@Since("1.0 pre-5, 2.7 (classinfo)")
public class ExprSets extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprSets.class, Object.class, "[all [[of] the]|the|every] %*classinfo%");
    }

    private @Nullable Supplier<? extends Iterator<?>> supplier;
    private ClassInfo<?> classInfo;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
        classInfo = ((Literal<ClassInfo<?>>) exprs[0]).getSingle(null);
        if (classInfo == null) {
            return false;
        }

        boolean plural = isPluralRequest(parser.expr, classInfo);
        if (!plural) {
            return false;
        }

        supplier = classInfo.getSupplier();
        if (supplier == null) {
            Skript.error("You cannot get all values of type '" + classInfo.getName().getSingular() + "'");
            return false;
        }
        return true;
    }

    @Override
    protected Object[] get(SkriptEvent event) {
        List<?> objects = java.util.stream.StreamSupport.stream(
                        java.util.Spliterators.spliteratorUnknownSize(supplier.get(), 0),
                        false
                )
                .toList();
        return objects.toArray((Object[]) Array.newInstance(classInfo.getC(), objects.size()));
    }

    @Override
    public @Nullable Iterator<?> iterator(SkriptEvent event) {
        return supplier.get();
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<?> getReturnType() {
        return classInfo.getC();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "all of the " + classInfo.getName().getPlural();
    }

    private static boolean isPluralRequest(String input, ClassInfo<?> classInfo) {
        String normalized = input == null ? "" : input.toLowerCase(Locale.ENGLISH);
        if (normalized.startsWith("every")) {
            return true;
        }
        String plural = classInfo.getName().getPlural().toLowerCase(Locale.ENGLISH);
        return normalized.contains(plural);
    }
}
