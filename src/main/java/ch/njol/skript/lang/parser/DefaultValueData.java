package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.DefaultExpression;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

/**
 * Parser data storing explicit default expressions by type.
 */
public class DefaultValueData extends ParserInstance.Data {

    private final Map<Class<?>, Deque<DefaultExpression<?>>> defaults = new LinkedHashMap<>();

    public DefaultValueData(ParserInstance parserInstance) {
        super(parserInstance);
    }

    public <T> void addDefaultValue(Class<T> type, DefaultExpression<T> value) {
        defaults.computeIfAbsent(type, ignored -> new ArrayDeque<>()).push(value);
    }

    public <T> @Nullable DefaultExpression<T> getDefaultValue(Class<T> type) {
        Deque<DefaultExpression<?>> stack = defaults.get(type);
        if (stack == null || stack.isEmpty()) {
            Class<?> bestMatch = null;
            for (Map.Entry<Class<?>, Deque<DefaultExpression<?>>> entry : defaults.entrySet()) {
                Deque<DefaultExpression<?>> candidateStack = entry.getValue();
                if (candidateStack.isEmpty()) {
                    continue;
                }
                Class<?> candidateType = entry.getKey();
                if (!candidateType.isAssignableFrom(type)) {
                    continue;
                }
                if (bestMatch == null || bestMatch.isAssignableFrom(candidateType)) {
                    bestMatch = candidateType;
                    stack = candidateStack;
                }
            }
            if (stack == null || stack.isEmpty()) {
                return null;
            }
        }
        @SuppressWarnings("unchecked")
        DefaultExpression<T> value = (DefaultExpression<T>) stack.peek();
        return value;
    }

    public void removeDefaultValue(Class<?> type) {
        Deque<DefaultExpression<?>> stack = defaults.get(type);
        if (stack == null || stack.isEmpty()) {
            throw new IllegalStateException("No default value for " + type.getName() + " to remove. Imbalanced add/remove?");
        }
        stack.pop();
    }
}
