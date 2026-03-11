package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprStringCase extends SimpleExpression<String> {

    static {
        Skript.registerExpression(
                ExprStringCase.class,
                String.class,
                "%strings% in (0¦upper|1¦lower)[ ]case",
                "(0¦upper|1¦lower)[ ]case %strings%",
                "capitali(s|z)ed %strings%",
                "%strings% in [(0¦lenient|1¦strict) ](proper|title)[ ]case",
                "[(0¦lenient|1¦strict) ](proper|title)[ ]case %strings%",
                "%strings% in [(0¦lenient|1¦strict) ]camel[ ]case",
                "[(0¦lenient|1¦strict) ]camel[ ]case %strings%",
                "%strings% in [(0¦lenient|1¦strict) ]pascal[ ]case",
                "[(0¦lenient|1¦strict) ]pascal[ ]case %strings%",
                "%strings% in [(1¦lower|2¦upper|3¦capital|4¦screaming)[ ]]snake[ ]case",
                "[(1¦lower|2¦upper|3¦capital|4¦screaming)[ ]]snake[ ]case %strings%",
                "%strings% in [(1¦lower|2¦upper|3¦capital)[ ]]kebab[ ]case",
                "[(1¦lower|2¦upper|3¦capital)[ ]]kebab[ ]case %strings%"
        );
    }

    private Expression<String> expr;
    private int casemode;
    private int type;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expr = (Expression<String>) exprs[0];
        if (matchedPattern <= 1) {
            casemode = parseResult.mark == 0 ? 1 : 2;
        } else if (matchedPattern == 2) {
            casemode = 1;
        } else if (matchedPattern <= 4) {
            type = 1;
            if (parseResult.mark != 0) {
                casemode = 3;
            }
        } else if (matchedPattern <= 6) {
            type = 2;
            if (parseResult.mark != 0) {
                casemode = 3;
            }
        } else if (matchedPattern <= 8) {
            type = 3;
            if (parseResult.mark != 0) {
                casemode = 3;
            }
        } else if (matchedPattern <= 10) {
            type = 4;
            if (parseResult.mark != 0) {
                casemode = parseResult.mark == 1 ? 2 : 1;
            }
        } else if (matchedPattern <= 12) {
            type = 5;
            if (parseResult.mark != 0) {
                casemode = parseResult.mark == 1 ? 2 : 1;
            }
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        String[] values = expr.getArray(event);
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            if (value == null) {
                continue;
            }
            values[i] = switch (type) {
                case 0 -> casemode == 1 ? value.toUpperCase(Locale.ENGLISH) : value.toLowerCase(Locale.ENGLISH);
                case 1 -> casemode == 3 ? capitalizeFully(value) : capitalizeWords(value);
                case 2 -> toCamelCase(value, casemode == 3);
                case 3 -> toPascalCase(value, casemode == 3);
                case 4 -> toSnakeCase(value, casemode);
                case 5 -> toKebabCase(value, casemode);
                default -> value;
            };
        }
        return values;
    }

    @Override
    public boolean isSingle() {
        return expr.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Expression<? extends String> simplify() {
        if (expr instanceof Literal<?>) {
            return SimplifiedLiteral.fromExpression(this);
        }
        return this;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return expr.toString(event, debug);
    }

    private static String toCamelCase(String input, boolean strict) {
        String[] words = input.split(" ");
        if (words.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(strict ? words[0].toLowerCase(Locale.ENGLISH) : uncapitalize(words[0]));
        for (int i = 1; i < words.length; i++) {
            builder.append(strict ? capitalizeFully(words[i]) : capitalizeWords(words[i]));
        }
        return builder.toString();
    }

    private static String toPascalCase(String input, boolean strict) {
        String[] words = input.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            builder.append(strict ? capitalizeFully(word) : capitalizeWords(word));
        }
        return builder.toString();
    }

    private static String toSnakeCase(String input, int mode) {
        return joinBySeparator(input, '_', mode);
    }

    private static String toKebabCase(String input, int mode) {
        return joinBySeparator(input, '-', mode);
    }

    private static String joinBySeparator(String input, char separator, int mode) {
        if (mode == 0) {
            return input.replace(' ', separator);
        }
        StringBuilder builder = new StringBuilder();
        input.codePoints().forEach(codePoint -> {
            if (codePoint == ' ') {
                builder.append(separator);
            } else {
                builder.appendCodePoint(mode == 1
                        ? Character.toUpperCase(codePoint)
                        : Character.toLowerCase(codePoint));
            }
        });
        return builder.toString();
    }

    private static String capitalizeWords(String value) {
        String[] words = value.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(capitalize(words[i]));
        }
        return builder.toString();
    }

    private static String capitalizeFully(String value) {
        String[] words = value.toLowerCase(Locale.ENGLISH).split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                builder.append(' ');
            }
            builder.append(capitalize(words[i]));
        }
        return builder.toString();
    }

    private static String capitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private static String uncapitalize(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }
}
