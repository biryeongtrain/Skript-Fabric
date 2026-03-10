package ch.njol.skript.expressions;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import org.jetbrains.annotations.Nullable;

public class ExprDebugInfo extends SimplePropertyExpression<Object, String> {

    static {
        register(ExprDebugInfo.class, String.class, "debug info[rmation]", "objects");
    }

    @Override
    public @Nullable String convert(Object from) {
        String rendered = Classes.toString(from, StringMode.MESSAGE);
        ClassInfo<?> classInfo = Classes.getSuperClassInfo(from.getClass());
        String typeName = classInfo == null ? from.getClass().getSimpleName().toLowerCase() : classInfo.getName().toString();
        if (from instanceof String) {
            rendered = "\"" + rendered + "\"";
        }
        return rendered + " (" + typeName + ")";
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "debug info";
    }
}
