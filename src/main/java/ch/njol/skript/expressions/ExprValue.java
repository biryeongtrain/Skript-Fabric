package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Value")
@Description({
        "Returns the value of something that has a value, for example a config node.",
        "The value is converted to the requested type where possible."
})
@Example("broadcast the text value of {_node}")
@Since("2.10, Fabric")
@Deprecated(since = "2.13", forRemoval = true)
public class ExprValue extends SimplePropertyExpression<Object, Object> {

    static {
        Skript.registerExpression(ExprValue.class, Object.class,
                "[the] %*classinfo% value of %valued%",
                "[the] %*classinfo% values of %valueds%",
                "%valued%'s %*classinfo% value",
                "%valueds%'[s] %*classinfo% values"
        );
    }

    private boolean single;
    private ClassInfo<?> classInfo;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
        Literal<ClassInfoReference> format;
        switch (pattern) {
            case 0 -> {
                single = true;
                format = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) expressions[0]);
                setExpr(expressions[1]);
            }
            case 1 -> {
                format = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) expressions[0]);
                setExpr(expressions[1]);
            }
            case 2 -> {
                single = true;
                format = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) expressions[1]);
                setExpr(expressions[0]);
            }
            default -> {
                format = (Literal<ClassInfoReference>) ClassInfoReference.wrap((Expression<ClassInfo<?>>) expressions[1]);
                setExpr(expressions[0]);
            }
        }
        ClassInfoReference reference = format.getSingle(null);
        classInfo = reference == null ? null : reference.getClassInfo();
        return classInfo != null;
    }

    @Override
    public @Nullable Object convert(@Nullable Object object) {
        if (!(object instanceof AnyValued<?> valued)) {
            return null;
        }
        return valued.convertedValue(classInfo);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class<?>[]{Object.class};
            case RESET, DELETE -> new Class<?>[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Object newValue = delta != null && delta.length > 0 ? delta[0] : null;
        for (Object object : getExpr().getArray(event)) {
            if (!(object instanceof AnyValued<?> valued) || !valued.supportsValueChange()) {
                continue;
            }
            if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
                valued.resetValue();
            } else {
                valued.changeValueSafely(newValue);
            }
        }
    }

    @Override
    public Class<?> getReturnType() {
        return classInfo.getC();
    }

    @Override
    public boolean isSingle() {
        return single;
    }

    @Override
    protected String getPropertyName() {
        return classInfo.getCodeName() + " value" + (single ? "" : "s");
    }
}
