package ch.njol.skript.expressions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

final class ExpressionHandleSupport {

    private ExpressionHandleSupport() {
    }

    static @Nullable Class<?> resolveClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    static @Nullable Object invoke(@Nullable Object target, String methodName, Object... args) {
        if (target == null) {
            return null;
        }
        Class<?> type = target instanceof Class<?> clazz ? clazz : target.getClass();
        Method method = findMethod(type, methodName, args.length);
        if (method == null) {
            return null;
        }
        try {
            method.setAccessible(true);
            return method.invoke(target instanceof Class<?> ? null : target, args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    static boolean set(@Nullable Object target, String methodName, Object value) {
        if (target == null) {
            return false;
        }
        Method method = findMethod(target.getClass(), methodName, 1);
        if (method == null) {
            return false;
        }
        try {
            method.setAccessible(true);
            method.invoke(target, value);
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    static @Nullable Object field(@Nullable Object target, String fieldName) {
        if (target == null) {
            return null;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    static boolean setField(@Nullable Object target, String fieldName, Object value) {
        if (target == null) {
            return false;
        }
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return false;
    }

    static @Nullable Object staticField(String className, String fieldName) {
        Class<?> type = resolveClass(className);
        if (type == null) {
            return null;
        }
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static @Nullable Method findMethod(Class<?> type, String methodName, int argCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
                return method;
            }
        }
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == argCount) {
                    return method;
                }
            }
        }
        return null;
    }
}
