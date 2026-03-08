package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

public abstract class Condition extends Statement {

    private boolean negated;

    public abstract boolean check(SkriptEvent event);

    @Override
    protected final boolean run(SkriptEvent event) {
        return check(event);
    }

    public boolean isNegated() {
        return negated;
    }

    protected final void setNegated(boolean negated) {
        this.negated = negated;
    }

    public static @Nullable Condition parse(String input, @Nullable String defaultError) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String expression = unwrapGroupedCondition(input.trim());
        var iterator = Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.CONDITION).iterator();
        Iterator<?> parseIterator = iterator;
        Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
        if (sectionContext.sectionNode != null) {
            Debuggable baselineOwner = sectionContext.owner;
            String baselineOwnerErrorRepresentation = sectionContext.ownerErrorRepresentation;
            parseIterator = new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Object next() {
                    sectionContext.owner = baselineOwner;
                    sectionContext.ownerErrorRepresentation = baselineOwnerErrorRepresentation;
                    return iterator.next();
                }
            };
        }
        @SuppressWarnings({"rawtypes", "unchecked"})
        Condition condition = (Condition) SkriptParser.parseModern(
                expression,
                (Iterator) parseIterator,
                ParseContext.DEFAULT,
                defaultError
        );
        return condition;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getClass().getSimpleName();
    }

    private static String unwrapGroupedCondition(String expression) {
        String unwrapped = expression;
        while (hasWrappingParentheses(unwrapped)) {
            unwrapped = unwrapped.substring(1, unwrapped.length() - 1).trim();
        }
        return unwrapped;
    }

    private static boolean hasWrappingParentheses(String expression) {
        if (expression.length() < 2 || expression.charAt(0) != '(' || expression.charAt(expression.length() - 1) != ')') {
            return false;
        }

        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < expression.length(); i++) {
            char character = expression.charAt(i);
            if (character == '\'' && !inDoubleQuote && !isEscaped(expression, i)) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (character == '"' && !inSingleQuote && !isEscaped(expression, i)) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
                if (depth == 0 && i < expression.length() - 1) {
                    return false;
                }
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0 && !inSingleQuote && !inDoubleQuote;
    }

    private static boolean isEscaped(String expression, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && expression.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return (backslashes & 1) == 1;
    }
}
