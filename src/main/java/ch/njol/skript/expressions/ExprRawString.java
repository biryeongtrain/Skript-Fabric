package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRawString extends SimpleExpression<String> {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x((?:&\\p{XDigit}){6})");

    static {
        Skript.registerExpression(ExprRawString.class, String.class, "raw %strings%");
    }

    private Expression<String> expr;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        expr = (Expression<String>) exprs[0];
        if (expr instanceof ExprColoured) {
            Skript.error("The 'colored' expression may not be used in a 'raw string' expression");
            return false;
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        List<String> strings = new ArrayList<>();
        for (String value : expr.getArray(event)) {
            String raw = value.replace('\u00A7', '&');
            if (raw.toLowerCase().contains("&x")) {
                Matcher matcher = HEX_PATTERN.matcher(raw);
                raw = matcher.replaceAll(matchResult ->
                        "<#" + matchResult.group(1).replace("&", "") + '>');
            }
            strings.add(raw);
        }
        return strings.toArray(new String[0]);
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
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "raw " + expr.toString(event, debug);
    }
}
