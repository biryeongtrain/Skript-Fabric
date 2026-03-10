package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ExpressionUnixDateCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void unixDateAndTimestampRoundTripSeconds() {
        ExprUnixDate unixDate = new ExprUnixDate();
        unixDate.init(new Expression[]{new SimpleLiteral<>(946684800, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(946_684_800_000L, unixDate.getSingle(SkriptEvent.EMPTY).getTime());

        ExprUnixTicks unixTicks = new ExprUnixTicks();
        unixTicks.init(new Expression[]{new SimpleLiteral<>(new Date(946_684_800_000L), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(946684800.0, unixTicks.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
