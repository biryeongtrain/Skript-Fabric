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

            EffFunctionCall functionCall = parseFunctionCall(expression, node, items, sectionContext);
            if (functionCall != null) {
                log.printLog();
                return functionCall;
            }
            log.clear();

            EffectSection section = EffectSection.parse(expression, null, node, false, items);
            if (section != null) {
                log.printLog();
                return new EffectSectionEffect(section);
            }
            log.clear();

            Effect effect = parsePlainEffect(expression);
            if (effect != null) {
                log.printLog();
                return effect;
            }
            log.clear();

            Condition condition = Condition.parse(expression, null);
            if (condition != null) {
                log.printLog();
                return condition;
            }
            log.clear();

            Statement statement = parseRegisteredStatement(expression, defaultError, node, items, sectionContext);
            if (statement != null) {
                log.printLog();
                return statement;
            }

            if (defaultError != null && !defaultError.isBlank()) {
                Skript.error(defaultError);
            }
            log.printError();
            return null;
        }
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

    private static @Nullable Effect parsePlainEffect(String expression) {
        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EFFECT).iterator();
        @SuppressWarnings({"rawtypes", "unchecked"})
        Effect effect = (Effect) SkriptParser.parseModern(
                expression,
                (Iterator) iterator,
                ParseContext.DEFAULT,
                null
        );
        return effect;
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
}
