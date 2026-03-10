package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

public final class CondIsSlimeChunk extends PropertyCondition<Object> {

    static {
        register(CondIsSlimeChunk.class, "([a] slime chunk|slime chunks|slimey)", "chunk");
    }

    @Override
    public boolean check(Object chunk) {
        return invokeBoolean(chunk, "isSlimeChunk", "slimeChunk");
    }

    @Override
    protected String getPropertyName() {
        return "slime chunk";
    }

    private static boolean invokeBoolean(@Nullable Object target, String... methodNames) {
        if (target == null) {
            return false;
        }
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                Object value = method.invoke(target);
                if (value instanceof Boolean bool) {
                    return bool;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }
}
