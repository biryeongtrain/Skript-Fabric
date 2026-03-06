package ch.njol.skript.lang;

import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.util.StringUtils;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for {@link DefaultExpression}.
 */
final class DefaultExpressionUtils {

    private DefaultExpressionUtils() {
    }

    static @Nullable DefaultExpressionError isValid(DefaultExpression<?> expr, ExprInfo exprInfo, int index) {
        if (expr == null) {
            return DefaultExpressionError.NOT_FOUND;
        } else if (!(expr instanceof Literal<?>) && (exprInfo.flagMask & SkriptParser.PARSE_EXPRESSIONS) == 0) {
            return DefaultExpressionError.NOT_LITERAL;
        } else if (expr instanceof Literal<?> && (exprInfo.flagMask & SkriptParser.PARSE_LITERALS) == 0) {
            return DefaultExpressionError.LITERAL;
        } else if (exprInfo.isPlural != null && index < exprInfo.isPlural.length && !exprInfo.isPlural[index] && !expr.isSingle()) {
            return DefaultExpressionError.NOT_SINGLE;
        } else if (exprInfo.time != 0 && !expr.setTime(exprInfo.time)) {
            return DefaultExpressionError.TIME_STATE;
        }
        return null;
    }

    enum DefaultExpressionError {
        NOT_FOUND {
            @Override
            public String getError(List<String> codeNames, String pattern) {
                String combinedComma = getCombinedComma(codeNames);
                String combinedSlash = StringUtils.join(codeNames, "/");
                return plurality(codeNames, "The class '", "The classes '")
                        + combinedComma + "'"
                        + plurality(codeNames, " does ", " do ")
                        + "not provide a default expression. Either allow null (with %-"
                        + combinedSlash + "%) or make it mandatory [pattern: " + pattern + "]";
            }
        },
        NOT_LITERAL {
            @Override
            public String getError(List<String> codeNames, String pattern) {
                return defaultExpression(codeNames, " is not a literal. ", " are not literals. ")
                        + "Either allow null (with %-*" + StringUtils.join(codeNames, "/")
                        + "%) or make it mandatory [pattern: " + pattern + "]";
            }
        },
        LITERAL {
            @Override
            public String getError(List<String> codeNames, String pattern) {
                return defaultExpression(codeNames, " is a literal. ", " are literals. ")
                        + "Either allow null (with %-~" + StringUtils.join(codeNames, "/")
                        + "%) or make it mandatory [pattern: " + pattern + "]";
            }
        },
        NOT_SINGLE {
            @Override
            public String getError(List<String> codeNames, String pattern) {
                return defaultExpression(codeNames, " is not a single-element expression. ", " are not single-element expressions. ")
                        + "Change your pattern to allow multiple elements or make the expression mandatory [pattern: " + pattern + "]";
            }
        },
        TIME_STATE {
            @Override
            public String getError(List<String> codeNames, String pattern) {
                return defaultExpression(codeNames, " does ", " do ")
                        + "not have distinct time states. [pattern: " + pattern + "]";
            }
        };

        public abstract String getError(List<String> codeNames, String pattern);

        private static String defaultExpression(List<String> codeNames, String single, String plural) {
            String combinedComma = getCombinedComma(codeNames);
            return "The default "
                    + plurality(codeNames, "expression ", "expressions ")
                    + "of '" + combinedComma + "'"
                    + plurality(codeNames, single, plural);
        }

        private static String plurality(List<String> codeNames, String single, String plural) {
            return codeNames.size() > 1 ? plural : single;
        }

        private static String getCombinedComma(List<String> codeNames) {
            if (codeNames.isEmpty()) {
                return "";
            }
            if (codeNames.size() == 1) {
                return codeNames.getFirst();
            }
            if (codeNames.size() == 2) {
                return StringUtils.join(codeNames, " and ");
            }
            return StringUtils.join(codeNames, ", ", ", and ");
        }
    }
}
