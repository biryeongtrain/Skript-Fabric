package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Utils;
import com.google.common.base.Preconditions;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public record ScriptParameter<T>(
        String name,
        Class<T> type,
        Set<Modifier> modifiers,
        @Nullable Expression<?> defaultValue
) implements Parameter<T> {

    public static Parameter<?> parse(@NotNull String name, @NotNull Class<?> type, @Nullable String def) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(type, "type cannot be null");

        if (!Variable.isValidVariableName(name, true, false)) {
            Skript.error("Invalid parameter name: " + name);
            return null;
        }

        Expression<?> parsedDefault = null;
        if (def != null) {
            Class<?> target = Utils.getComponentType(type);
            try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
                parsedDefault = new SkriptParser(def, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                        .parseExpression(new Class[]{target});
                if (parsedDefault == null || LiteralUtils.hasUnparsedLiteral(parsedDefault)) {
                    log.printErrors("Can't understand this expression: " + def);
                    log.stop();
                    return null;
                }
                log.printLog();
                log.stop();
            }
        }

        LinkedHashSet<Modifier> modifiers = new LinkedHashSet<>();
        if (parsedDefault != null) {
            modifiers.add(Modifier.OPTIONAL);
        }
        if (type.isArray()) {
            modifiers.add(Modifier.KEYED);
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        Parameter<?> parameter = new ScriptParameter(name, type, modifiers, parsedDefault);
        return parameter;
    }

    public ScriptParameter(String name, Class<T> type, Modifier... modifiers) {
        this(name, type, Set.of(modifiers), null);
    }

    public ScriptParameter(String name, Class<T> type, @Nullable Expression<?> defaultValue, Modifier... modifiers) {
        this(name, type, Set.of(modifiers), defaultValue);
    }

    @Override
    public Object[] evaluate(@Nullable Expression<? extends T> argument, SkriptEvent event) {
        if (argument == null) {
            if (!hasModifier(Modifier.OPTIONAL)) {
                throw new IllegalStateException("This parameter is required, but no argument was provided");
            }
            if (defaultValue == null) {
                throw new IllegalStateException("This parameter does not have a default value");
            }
            @SuppressWarnings("unchecked")
            Expression<? extends T> expression = (Expression<? extends T>) defaultValue;
            return Parameter.super.evaluate(expression, event);
        }
        return Parameter.super.evaluate(argument, event);
    }

    @Override
    public @NotNull String toString() {
        return toFormattedString();
    }
}
