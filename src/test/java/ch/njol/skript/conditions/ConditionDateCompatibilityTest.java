package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

class ConditionDateCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void dateConditionSupportsMoreAndLessThanAgo() {
        CondDate moreThan = new CondDate();
        moreThan.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() - 5_000L), false),
                new SimpleLiteral<>(new Timespan(Timespan.TimePeriod.SECOND, 1), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(moreThan.check(SkriptEvent.EMPTY));

        CondDate lessThan = new CondDate();
        lessThan.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() - 500L), false),
                new SimpleLiteral<>(new Timespan(Timespan.TimePeriod.SECOND, 1), false)
        }, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(lessThan.check(SkriptEvent.EMPTY));
    }

    @Test
    void pastFutureConditionHandlesPastFutureAndPassedSyntax() {
        CondPastFuture past = new CondPastFuture();
        past.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() - 1_000L), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(past.check(SkriptEvent.EMPTY));

        CondPastFuture future = new CondPastFuture();
        SkriptParser.ParseResult futureParse = parseResult("");
        futureParse.tags.add("future");
        future.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() + 1_000L), false)
        }, 0, Kleenean.FALSE, futureParse);
        assertTrue(future.check(SkriptEvent.EMPTY));

        CondPastFuture passedNegated = new CondPastFuture();
        SkriptParser.ParseResult negatedParse = parseResult("");
        negatedParse.tags.add("negated");
        passedNegated.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() + 1_000L), false)
        }, 1, Kleenean.FALSE, negatedParse);
        assertTrue(passedNegated.check(SkriptEvent.EMPTY));

        CondPastFuture wrongDirection = new CondPastFuture();
        wrongDirection.init(new Expression[]{
                new SimpleLiteral<>(new Date(System.currentTimeMillis() + 1_000L), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertFalse(wrongDirection.check(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
