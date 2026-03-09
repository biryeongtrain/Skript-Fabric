package ch.njol.skript.conditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ConditionBundleCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void startsEndsWithSupportsNegationAndLiteralSimplification() {
        CondStartsEndsWith starts = new CondStartsEndsWith();
        SkriptParser.ParseResult startsParse = parseResult("");
        starts.init(new Expression[]{
                new SimpleLiteral<>("alphabet", false),
                new SimpleLiteral<>(new String[]{"alp", "alpha"}, String.class, false)
        }, 0, Kleenean.FALSE, startsParse);
        assertTrue(starts.check(SkriptEvent.EMPTY));
        assertEquals("[alphabet] starts with [alp, alpha]", starts.toString(SkriptEvent.EMPTY, false));

        CondStartsEndsWith endsNegated = new CondStartsEndsWith();
        SkriptParser.ParseResult endsParse = parseResult("");
        endsParse.mark = 1;
        endsNegated.init(new Expression[]{
                new SimpleLiteral<>("alphabet", false),
                new SimpleLiteral<>("bet", false)
        }, 1, Kleenean.FALSE, endsParse);
        assertFalse(endsNegated.check(SkriptEvent.EMPTY));
    }

    @Test
    void matchesSupportsWholeAndPartialRegexChecks() {
        CondMatches full = new CondMatches();
        SkriptParser.ParseResult fullParse = parseResult("");
        fullParse.mark = 1;
        full.init(new Expression[]{
                new SimpleLiteral<>("abc123", false),
                new SimpleLiteral<>("[a-z]+\\d+", false)
        }, 0, Kleenean.FALSE, fullParse);
        assertTrue(full.check(SkriptEvent.EMPTY));

        CondMatches partialNegated = new CondMatches();
        SkriptParser.ParseResult partialParse = parseResult("");
        partialParse.mark = 2;
        partialNegated.init(new Expression[]{
                new SimpleLiteral<>("abc123", false),
                new SimpleLiteral<>("\\d+", false)
        }, 1, Kleenean.FALSE, partialParse);
        assertFalse(partialNegated.check(SkriptEvent.EMPTY));
    }

    @Test
    void isSetHandlesPlainExpressionsExpressionListsAndVerboseMessages() {
        CondIsSet set = new CondIsSet();
        set.init(new Expression[]{new SimpleLiteral<>("value", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(set.check(SkriptEvent.EMPTY));
        assertEquals("a value", set.getExpectedMessage(SkriptEvent.EMPTY));

        ExpressionList<Object> list = new ExpressionList<>(
                new Expression[]{
                        new SimpleLiteral<>("value", false),
                        new SimpleLiteral<>(new Object[0], Object.class, true)
                },
                Object.class,
                true
        );
        CondIsSet missing = new CondIsSet();
        missing.init(new Expression[]{list}, 0, Kleenean.FALSE, parseResult(""));
        assertFalse(missing.check(SkriptEvent.EMPTY));

        CondIsSet negated = new CondIsSet();
        negated.init(new Expression[]{new SimpleLiteral<>(new Object[0], Object.class, true)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(negated.check(SkriptEvent.EMPTY));
        assertEquals("none", negated.getExpectedMessage(SkriptEvent.EMPTY));
    }
    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
