package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprRandomCharacter extends SimpleExpression<String> {

    private @Nullable Expression<Number> amount;
    private Expression<String> from;
    private Expression<String> to;
    private boolean alphanumeric;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        amount = (Expression<Number>) exprs[0];
        from = (Expression<String>) exprs[1];
        to = (Expression<String>) exprs[2];
        alphanumeric = parseResult.hasTag("alphanumeric");
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        Number raw = amount == null ? 1 : amount.getSingle(event);
        if (raw == null) {
            return new String[0];
        }
        int requested = raw.intValue();
        if (requested <= 0) {
            return new String[0];
        }

        String fromValue = from.getSingle(event);
        String toValue = to.getSingle(event);
        if (fromValue == null || toValue == null || fromValue.isEmpty() || toValue.isEmpty()) {
            return new String[0];
        }

        Random random = ThreadLocalRandom.current();
        int min = Math.min(fromValue.charAt(0), toValue.charAt(0));
        int max = Math.max(fromValue.charAt(0), toValue.charAt(0));
        String[] values = new String[requested];

        if (alphanumeric) {
            StringBuilder valid = new StringBuilder();
            for (int current = min; current <= max; current++) {
                if (Character.isLetterOrDigit(current)) {
                    valid.append((char) current);
                }
            }
            if (valid.isEmpty()) {
                return new String[0];
            }
            for (int i = 0; i < requested; i++) {
                values[i] = String.valueOf(valid.charAt(random.nextInt(valid.length())));
            }
            return values;
        }

        for (int i = 0; i < requested; i++) {
            values[i] = String.valueOf((char) (random.nextInt(max - min + 1) + min));
        }
        return values;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isSingle() {
        if (amount instanceof Literal<?> literal) {
            Number literalAmount = ((Literal<Number>) literal).getSingle(SkriptEvent.EMPTY);
            return literalAmount != null && literalAmount.intValue() == 1;
        }
        return amount == null;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (amount != null ? amount.toString(event, debug) : "a")
                + " random "
                + (alphanumeric ? "alphanumeric " : "")
                + "character between "
                + from.toString(event, debug)
                + " and "
                + to.toString(event, debug);
    }
}
