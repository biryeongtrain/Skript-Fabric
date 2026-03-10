package ch.njol.skript.expressions;

import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

final class ReflectiveHandleAccess {

    private ReflectiveHandleAccess() {
    }

    static boolean currentEventSupports(String... methodNames) {
        Class<?>[] currentEvents = ParserInstance.get().getCurrentEventClasses();
        if (currentEvents == null || currentEvents.length == 0) {
            return false;
        }
        for (Class<?> eventClass : currentEvents) {
            if (supports(eventClass, methodNames)) {
                return true;
            }
        }
        return false;
    }

    static @Nullable Object invokeNoArg(@Nullable Object target, String... methodNames) {
        if (target == null) {
            return null;
        }
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
            try {
                Field field = target.getClass().getDeclaredField(methodName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    static void invokeSingleArg(@Nullable Object target, String methodName, Object value) {
        if (target == null) {
            return;
        }
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> parameterType = method.getParameterTypes()[0];
            if (value == null || parameterType.isInstance(value) || isPrimitiveMatch(parameterType, value.getClass())) {
                try {
                    method.invoke(target, value);
                    return;
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
        try {
            Field field = target.getClass().getDeclaredField(methodName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean supports(Class<?> eventClass, String... methodNames) {
        for (String methodName : methodNames) {
            for (Method method : eventClass.getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPrimitiveMatch(Class<?> parameterType, Class<?> valueType) {
        return (parameterType == Integer.TYPE && Integer.class.isAssignableFrom(valueType))
                || (parameterType == Long.TYPE && Long.class.isAssignableFrom(valueType))
                || (parameterType == Boolean.TYPE && Boolean.class.isAssignableFrom(valueType))
                || (parameterType == Float.TYPE && Float.class.isAssignableFrom(valueType))
                || (parameterType == Double.TYPE && Double.class.isAssignableFrom(valueType));
    }
}
