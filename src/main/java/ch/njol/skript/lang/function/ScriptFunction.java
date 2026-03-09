package ch.njol.skript.lang.function;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Script-backed function implementation.
 */
public class ScriptFunction<T> extends Function<T> implements ReturnHandler<T> {

    private final Trigger trigger;

    private final ThreadLocal<Boolean> returnValueSet = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<T @Nullable []> returnValues = new ThreadLocal<>();
    private final ThreadLocal<String @Nullable []> returnKeys = new ThreadLocal<>();

    public ScriptFunction(Signature<T> signature, SectionNode node) {
        super(signature);
        Functions.currentFunction = this;
        HintManager hintManager = ParserInstance.get().getHintManager();
        try {
            hintManager.enterScope(false);
            for (Parameter<?> parameter : signature.getParameters()) {
                String hintName = parameter.name();
                if (parameter.isSingle()) {
                    hintManager.set(hintName, parameter.type());
                    continue;
                }
                hintManager.set(hintName + ch.njol.skript.lang.Variable.SEPARATOR + "*", parameter.type().getComponentType());
            }
            trigger = loadReturnableTrigger(node, "function " + signature.getName(), new SimpleEvent());
        } finally {
            hintManager.exitScope();
            Functions.currentFunction = null;
        }
        trigger.setLineNumber(node.getLine());
    }

    @Override
    public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
        SkriptEvent callContext = new SkriptEvent(event, null, null, null);

        int index = 0;
        for (Parameter<?> parameter : getSignature().getParameters()) {
            Object[] values = index < params.length ? params[index] : null;
            if (values == null) {
                index++;
                continue;
            }
            if (parameter.isSingle()) {
                if (values.length > 0) {
                    Variables.setVariable(parameter.name(), values[0], callContext, true);
                }
                index++;
                continue;
            }

            boolean keyed = Arrays.stream(values).allMatch(it -> it instanceof KeyedValue<?>);
            if (keyed) {
                for (Object value : values) {
                    KeyedValue<?> keyedValue = (KeyedValue<?>) value;
                    Variables.setVariable(
                            parameter.name() + ch.njol.skript.lang.Variable.SEPARATOR + keyedValue.key(),
                            keyedValue.value(),
                            callContext,
                            true
                    );
                }
            } else {
                for (int i = 0; i < values.length; i++) {
                    Variables.setVariable(
                            parameter.name() + ch.njol.skript.lang.Variable.SEPARATOR + (i + 1),
                            values[i],
                            callContext,
                            true
                    );
                }
            }
            index++;
        }

        trigger.execute(callContext);
        return type() != null ? returnValues.get() : null;
    }

    @Override
    public @Nullable String[] returnedKeys() {
        return type() != null ? returnKeys.get() : null;
    }

    @Override
    public boolean resetReturnValue() {
        returnValueSet.remove();
        returnValues.remove();
        returnKeys.remove();
        return true;
    }

    @Override
    public final void returnValues(SkriptEvent event, Expression<? extends T> value) {
        if (returnValueSet.get()) {
            return;
        }
        returnValueSet.set(true);
        returnValues.set(value.getArray(event));
        if (KeyProviderExpression.canReturnKeys(value)) {
            returnKeys.set(((KeyProviderExpression<?>) value).getArrayKeys(event));
        }
    }

    @Override
    public final boolean isSingleReturnValue() {
        return isSingle();
    }

    @Override
    public final @Nullable Class<? extends T> returnValueType() {
        //noinspection unchecked
        return (Class<? extends T>) Utils.getComponentType(type());
    }
}
