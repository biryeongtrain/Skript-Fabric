package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.chat.ChatMessages;
import ch.njol.util.Kleenean;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprColoured extends PropertyExpression<String, String> {

    private static final Pattern HEX_TAG_PATTERN = Pattern.compile("(?i)<#([0-9a-f]{6})>");
    private static final Pattern NAMED_TAG_PATTERN = Pattern.compile("(?i)<([a-z_]+)>");
    private static final Pattern AMPERSAND_COLOR_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");
    private static final Map<String, Character> NAMED_COLORS = Map.ofEntries(
            Map.entry("black", '0'),
            Map.entry("dark_blue", '1'),
            Map.entry("dark_green", '2'),
            Map.entry("dark_aqua", '3'),
            Map.entry("dark_red", '4'),
            Map.entry("dark_purple", '5'),
            Map.entry("gold", '6'),
            Map.entry("gray", '7'),
            Map.entry("grey", '7'),
            Map.entry("dark_gray", '8'),
            Map.entry("dark_grey", '8'),
            Map.entry("blue", '9'),
            Map.entry("green", 'a'),
            Map.entry("aqua", 'b'),
            Map.entry("red", 'c'),
            Map.entry("light_purple", 'd'),
            Map.entry("yellow", 'e'),
            Map.entry("white", 'f'),
            Map.entry("reset", 'r'),
            Map.entry("bold", 'l'),
            Map.entry("italic", 'o'),
            Map.entry("underlined", 'n'),
            Map.entry("strikethrough", 'm'),
            Map.entry("obfuscated", 'k')
    );

    static {
        Skript.registerExpression(
                ExprColoured.class,
                String.class,
                "(colo[u]r-|colo[u]red )%strings%",
                "(format-|formatted )%strings%",
                "(un|non)[-](colo[u]r-|colo[u]red |format-|formatted )%strings%"
        );
    }

    boolean color;
    boolean format;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<? extends String>) exprs[0]);
        color = matchedPattern <= 1;
        format = matchedPattern == 1;
        return true;
    }

    @Override
    protected String[] get(SkriptEvent event, String[] source) {
        return get(source, value -> color ? replaceChatStyles(value) : ChatMessages.stripStyles(value));
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (color ? "" : "un") + "colored " + getExpr().toString(event, debug);
    }

    public boolean isUnsafeFormat() {
        return format;
    }

    static String replaceChatStyles(String value) {
        String withHex = HEX_TAG_PATTERN.matcher(value).replaceAll(matchResult -> toLegacyHex(matchResult.group(1)));
        Matcher named = NAMED_TAG_PATTERN.matcher(withHex);
        StringBuffer buffer = new StringBuffer();
        while (named.find()) {
            Character code = NAMED_COLORS.get(named.group(1).toLowerCase(Locale.ENGLISH));
            if (code == null) {
                named.appendReplacement(buffer, Matcher.quoteReplacement(named.group()));
                continue;
            }
            named.appendReplacement(buffer, Matcher.quoteReplacement("§" + code));
        }
        named.appendTail(buffer);
        return AMPERSAND_COLOR_PATTERN.matcher(buffer.toString()).replaceAll("§$1");
    }

    private static String toLegacyHex(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        for (int i = 0; i < hex.length(); i++) {
            builder.append('§').append(Character.toLowerCase(hex.charAt(i)));
        }
        return builder.toString();
    }
}
