package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ExpressionDateCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void nowAgoLaterAndTimeSinceMatchDateHelpers() {
        ExprNow now = new ExprNow();
        now.init(new Expression[0], 0, Kleenean.FALSE, parseResult(""));
        Date nowValue = now.getSingle(SkriptEvent.EMPTY);
        assertNotNull(nowValue);
        assertTrue(Math.abs(nowValue.getTime() - System.currentTimeMillis()) < 5_000L);

        Date anchor = new Date(10_000L);
        Timespan twoSeconds = new Timespan(Timespan.TimePeriod.SECOND, 2);

        ExprDateAgoLater ago = new ExprDateAgoLater();
        ago.init(new Expression[]{
                new SimpleLiteral<>(twoSeconds, false),
                new SimpleLiteral<>(anchor, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(8_000L, ago.getSingle(SkriptEvent.EMPTY).getTime());

        ExprDateAgoLater later = new ExprDateAgoLater();
        later.init(new Expression[]{
                new SimpleLiteral<>(twoSeconds, false),
                new SimpleLiteral<>(anchor, false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertEquals(12_000L, later.getSingle(SkriptEvent.EMPTY).getTime());

        ExprTimeSince since = new ExprTimeSince();
        since.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() - 2_000L), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(since.getSingle(SkriptEvent.EMPTY).millis() >= 1_000L);

        ExprTimeSince until = new ExprTimeSince();
        until.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() + 2_000L), false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(until.getSingle(SkriptEvent.EMPTY).millis() >= 1_000L);

    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
