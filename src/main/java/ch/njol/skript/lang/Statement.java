package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Statement extends TriggerItem implements SyntaxElement {

    public final @Nullable ExecutionIntent loaderExecutionIntent() {
        return executionIntent();
    }

    public static @Nullable Statement parse(String input, String defaultError) {
        return parse(input, defaultError, null, null);
    }

    public static @Nullable Statement parse(String input, @Nullable List<TriggerItem> items, String defaultError) {
        return parse(input, defaultError, null, items);
    }

    public static @Nullable Statement parse(
            String input,
            @Nullable String defaultError,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> items
    ) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
            String expression = input.trim();
            if (node == null) {
                return sectionContext.modify(null, null, () ->
                        parseInternal(expression, defaultError, null, null, sectionContext, log)
                );
            }
            return parseInternal(expression, defaultError, node, items, sectionContext, log);
        }
    }

    private static @Nullable Statement parseInternal(
            String expression,
            @Nullable String defaultError,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> items,
            Section.SectionContext sectionContext,
            ParseLogHandler log
    ) {
        EffFunctionCall functionCall = parseFunctionCall(expression, node, items, sectionContext);
        if (functionCall != null) {
            log.printLog();
            return functionCall;
        }
        if (log.hasError()) {
            log.printError();
            return null;
        }
        log.clear();

        ParseLogHandler effectFailure = null;
        Effect effect = Effect.parse(expression, null, node, items);
        if (effect != null) {
            log.printLog();
            return effect;
        }
        if (log.hasError()) {
            effectFailure = log.backup();
        }
        resetParseLog(log);

        ParseLogHandler conditionFailure = null;
        Condition condition = parseCondition(expression, node, items, sectionContext);
        if (condition != null) {
            log.printLog();
            return condition;
        }
        if (log.hasError()) {
            conditionFailure = log.backup();
        }
        resetParseLog(log);

        Statement statement = parseRegisteredStatement(expression, defaultError, node, items, sectionContext);
        if (statement != null) {
            log.printLog();
            return statement;
        }
        ParseLogHandler statementFailure = log.backup();
        ParseLogHandler retainedFailure = selectRetainedFailure(
                defaultError,
                statementFailure,
                conditionFailure,
                effectFailure
        );
        if (retainedFailure != null) {
            log.restore(retainedFailure);
        }
        log.printError(defaultError);
        return null;
    }

    private static @Nullable EffFunctionCall parseFunctionCall(
            String expression,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> items,
            Section.SectionContext sectionContext
    ) {
        if (node == null) {
            return EffFunctionCall.parse(expression);
        }
        return sectionContext.modify(node, items, () -> {
            EffFunctionCall parsed = EffFunctionCall.parse(expression);
            if (parsed != null && !sectionContext.claimed()) {
                Skript.error("The line '" + expression
                        + "' is a valid function call but cannot function as a section (:) because there is no parameter to manage it.");
                return null;
            }
            return parsed;
        });
    }

    private static @Nullable Condition parseCondition(
            String expression,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> items,
            Section.SectionContext sectionContext
    ) {
        if (node == null) {
            return Condition.parse(expression, null);
        }
        return sectionContext.modify(node, items, () -> {
            Condition parsed = Condition.parse(expression, null);
            if (parsed != null && !sectionContext.claimed()) {
                Skript.error("The line '" + expression
                        + "' is a valid condition but cannot function as a section (:) because there is no syntax in the line to manage it.");
                return null;
            }
            return parsed;
        });
    }

    private static @Nullable Statement parseRegisteredStatement(
            String expression,
            @Nullable String defaultError,
            @Nullable SectionNode node,
            @Nullable List<TriggerItem> items,
            Section.SectionContext sectionContext
    ) {
        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.STATEMENT).iterator();
        if (node == null) {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Statement statement = (Statement) SkriptParser.parseModern(
                    expression,
                    (Iterator) iterator,
                    ParseContext.DEFAULT,
                    defaultError
            );
            return statement;
        }

        Iterator<?> wrappedIterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Object next() {
                sectionContext.owner = null;
                return iterator.next();
            }
        };
        return sectionContext.modify(node, items, () -> {
            @SuppressWarnings({"rawtypes", "unchecked"})
            Statement parsed = (Statement) SkriptParser.parseModern(
                    expression,
                    (Iterator) wrappedIterator,
                    ParseContext.DEFAULT,
                    defaultError
            );
            if (parsed != null && !sectionContext.claimed()) {
                Skript.error("The line '" + expression
                        + "' is a valid statement but cannot function as a section (:) because there is no syntax in the line to manage it.");
                return null;
            }
            return parsed;
        });
    }

    private static void resetParseLog(ParseLogHandler log) {
        log.clear();
        log.clearError();
    }

    private static @Nullable ParseLogHandler selectRetainedFailure(
            @Nullable String defaultError,
            ParseLogHandler statementFailure,
            @Nullable ParseLogHandler conditionFailure,
            @Nullable ParseLogHandler effectFailure
    ) {
        if (hasSpecificError(statementFailure, defaultError)) {
            return statementFailure;
        }
        ParseLogHandler previousFailure = moreRelevantFailure(conditionFailure, effectFailure);
        if (previousFailure != null) {
            return previousFailure;
        }
        return statementFailure.hasError() ? statementFailure : null;
    }

    private static boolean hasSpecificError(ParseLogHandler log, @Nullable String defaultError) {
        if (!log.hasError()) {
            return false;
        }
        if (defaultError == null || defaultError.isBlank()) {
            return true;
        }
        for (var error : log.getErrors()) {
            if (!defaultError.equals(error.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable ParseLogHandler moreRelevantFailure(
            @Nullable ParseLogHandler primary,
            @Nullable ParseLogHandler fallback
    ) {
        if (primary == null || !primary.hasError()) {
            return fallback != null && fallback.hasError() ? fallback : null;
        }
        if (fallback == null || !fallback.hasError()) {
            return primary;
        }
        if (primary.getError() == null) {
            return fallback;
        }
        if (fallback.getError() == null) {
            return primary;
        }
        return primary.getError().getQuality().priority() > fallback.getError().getQuality().priority()
                ? primary
                : fallback;
    }
}
