package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Utils;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Compatibility implementation for input-source expressions used by filtering and mapping paths.
 * This keeps the upstream "input" and "input index" behavior available even before the rest of the
 * upstream input-source syntax surface is imported.
 */
public class ExprInput<T> extends SimpleExpression<T> {

    private final @Nullable ExprInput<?> source;
    private final @Nullable InputSource inputSource;
    private final boolean index;
    private final @Nullable Class<?> specifiedType;
    private final @Nullable String specifiedTypeName;
    private final Class<? extends T>[] types;
    private final Class<T> superType;

    @SuppressWarnings("unchecked")
    public ExprInput() {
        this(null, false, null, null, (Class<? extends T>) Object.class);
    }

    public static ExprInput<String> inputIndex() {
        return new ExprInput<>(null, true, null, null, String.class);
    }

    public static <T> ExprInput<T> typed(String typeName, Class<? extends T> type) {
        return new ExprInput<>(null, false, type, typeName, type);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public ExprInput(@Nullable ExprInput<?> source, Class<? extends T>... types) {
        this(
                source,
                source != null && source.index,
                null,
                null,
                types.length == 0 ? new Class[]{(Class<? extends T>) Object.class} : types
        );
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private ExprInput(
            @Nullable ExprInput<?> source,
            boolean index,
            @Nullable Class<?> specifiedType,
            @Nullable String specifiedTypeName,
            Class<? extends T>... types
    ) {
        this.source = source;
        this.inputSource = source != null ? source.inputSource : currentInputSource();
        this.index = source != null ? source.index : index;
        this.specifiedType = source != null ? source.specifiedType : specifiedType;
        this.specifiedTypeName = source != null ? source.specifiedTypeName : specifiedTypeName;
        this.types = types.length == 0 ? new Class[]{(Class<? extends T>) Object.class} : Arrays.copyOf(types, types.length);
        this.superType = (Class<T>) Utils.getSuperType(this.types);

        if (source != null && inputSource != null) {
            Set<ExprInput<?>> dependentInputs = inputSource.getDependentInputs();
            dependentInputs.remove(source);
            dependentInputs.add(this);
        }
    }

    private static @Nullable InputSource currentInputSource() {
        return ch.njol.skript.lang.parser.ParserInstance.get()
                .getData(InputSource.InputData.class)
                .getSource();
    }

    protected final @Nullable InputSource getInputSource() {
        return inputSource;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T[] get(SkriptEvent event) {
        Object currentValue;
        if (inputSource == null) {
            return (T[]) Array.newInstance(superType, 0);
        }

        if (index) {
            if (!inputSource.hasIndices()) {
                return (T[]) Array.newInstance(superType, 0);
            }
            currentValue = inputSource.getCurrentIndex();
        } else {
            currentValue = inputSource.getCurrentValue();
        }

        if (currentValue == null) {
            return (T[]) Array.newInstance(superType, 0);
        }
        if (specifiedType != null && !specifiedType.isInstance(currentValue)) {
            return (T[]) Array.newInstance(superType, 0);
        }

        try {
            return Converters.convert(new Object[]{currentValue}, types, superType);
        } catch (ClassCastException ignored) {
            return (T[]) Array.newInstance(superType, 0);
        }
    }

    @Override
    public <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
        return new ExprInput<>(this, to);
    }

    @Override
    public Expression<?> getSource() {
        return source == null ? this : source;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends T> getReturnType() {
        return superType;
    }

    @Override
    public Class<? extends T>[] possibleReturnTypes() {
        return Arrays.copyOf(types, types.length);
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (index) {
            return "input index";
        }
        return specifiedTypeName == null ? "input" : specifiedTypeName + " input";
    }
}
