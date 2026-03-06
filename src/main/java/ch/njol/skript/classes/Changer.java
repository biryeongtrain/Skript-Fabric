package ch.njol.skript.classes;

import ch.njol.skript.lang.Expression;

public interface Changer {

    enum ChangeMode {
        SET,
        ADD,
        REMOVE,
        RESET,
        DELETE
    }

    final class ChangerUtils {

        private ChangerUtils() {
        }

        public static boolean acceptsChange(Expression<?> expression, ChangeMode mode, Class<?> type) {
            Class<?>[] accepted = expression.acceptChange(mode);
            if (accepted == null) {
                return false;
            }
            if (accepted.length == 0) {
                return true;
            }
            for (Class<?> acceptedType : accepted) {
                if (acceptedType.isAssignableFrom(type) || type.isAssignableFrom(acceptedType)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean acceptsChangeTypes(Class<?>[] accepted, Class<?> type) {
            if (accepted == null) {
                return false;
            }
            if (accepted.length == 0) {
                return true;
            }
            for (Class<?> acceptedType : accepted) {
                if (acceptedType.isAssignableFrom(type) || type.isAssignableFrom(acceptedType)) {
                    return true;
                }
            }
            return false;
        }
    }
}
