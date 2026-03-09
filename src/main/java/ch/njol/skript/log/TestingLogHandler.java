package ch.njol.skript.log;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.structure.Structure;

public class TestingLogHandler extends LogHandler {

    private static final String EVT_TEST_CASE_CLASS = "ch.njol.skript.test.runner.EvtTestCase";
    private static final String TEST_TRACKER_CLASS = "ch.njol.skript.test.runner.TestTracker";

    private final int minimum;
    private int count;
    private final ParserInstance parser;

    public TestingLogHandler(Level minimum) {
        this.minimum = minimum.intValue();
        this.parser = ParserInstance.get();
    }

    @Override
    public LogResult log(LogEntry entry) {
        if (entry.getLevel().intValue() >= minimum) {
            count++;
            reportFailure(entry.getMessage(), parser.getCurrentStructure(), parser.getNode());
        }
        return LogResult.LOG;
    }

    @Override
    public TestingLogHandler start() {
        SkriptLogger.startLogHandler(this);
        return this;
    }

    public int getCount() {
        return count;
    }

    private void reportFailure(String message, @Nullable Structure structure, @Nullable Node node) {
        Class<?> trackerClass = loadClass(TEST_TRACKER_CLASS);
        if (trackerClass == null) {
            return;
        }
        invoke(trackerClass, "parsingStarted", new Class<?>[]{String.class}, resolveStructureName(structure));
        if (node != null) {
            Object currentScript = parser.getCurrentScript();
            invoke(
                    trackerClass,
                    "testFailed",
                    new Class<?>[]{String.class, currentScript == null ? Object.class : currentScript.getClass(), int.class},
                    message,
                    currentScript,
                    node.getLine()
            );
            return;
        }
        invoke(trackerClass, "testFailed", new Class<?>[]{String.class}, message);
    }

    private @Nullable String resolveStructureName(@Nullable Structure structure) {
        if (structure == null) {
            return null;
        }
        Class<?> evtTestCaseClass = loadClass(EVT_TEST_CASE_CLASS);
        if (evtTestCaseClass != null && evtTestCaseClass.isInstance(structure)) {
            try {
                Method method = evtTestCaseClass.getMethod("getTestName");
                Object value = method.invoke(structure);
                return value instanceof String string ? string : null;
            } catch (ReflectiveOperationException ignored) {
                // Fall through to the generic structure name.
            }
        }
        return structure.getSyntaxTypeName();
    }

    private @Nullable Class<?> loadClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private void invoke(Class<?> owner, String name, Class<?>[] parameterTypes, Object... arguments) {
        try {
            Method method = owner.getMethod(name, parameterTypes);
            method.invoke(null, arguments);
            return;
        } catch (ReflectiveOperationException ignored) {
            // Fall back to compatible overload lookup.
        }
        for (Method method : owner.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers()) || !method.getName().equals(name)) {
                continue;
            }
            Class<?>[] candidateTypes = method.getParameterTypes();
            if (candidateTypes.length != arguments.length) {
                continue;
            }
            if (!isCompatible(candidateTypes, arguments)) {
                continue;
            }
            try {
                method.invoke(null, arguments);
                return;
            } catch (ReflectiveOperationException ignored) {
                return;
            }
        }
    }

    private boolean isCompatible(Class<?>[] parameterTypes, Object[] arguments) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments[i];
            if (argument == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
                continue;
            }
            if (!wrap(parameterTypes[i]).isInstance(argument)) {
                return false;
            }
        }
        return true;
    }

    private Class<?> wrap(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == int.class) {
            return Integer.class;
        }
        if (type == long.class) {
            return Long.class;
        }
        if (type == boolean.class) {
            return Boolean.class;
        }
        if (type == double.class) {
            return Double.class;
        }
        if (type == float.class) {
            return Float.class;
        }
        if (type == byte.class) {
            return Byte.class;
        }
        if (type == short.class) {
            return Short.class;
        }
        if (type == char.class) {
            return Character.class;
        }
        return type;
    }
}
