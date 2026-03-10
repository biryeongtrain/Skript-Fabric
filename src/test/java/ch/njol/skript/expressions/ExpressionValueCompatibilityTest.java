package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.data.JavaClasses;
import ch.njol.skript.classes.data.SkriptClasses;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ExpressionValueCompatibilityTest {

    @BeforeAll
    static void registerClassInfos() {
        JavaClasses.register();
        SkriptClasses.register();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void angleSupportsDegreesRadiansAndSimplification() {
        ExprAngle degrees = new ExprAngle();
        degrees.init(new Expression[]{new SimpleLiteral<>(90, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(90, degrees.getSingle(SkriptEvent.EMPTY));

        ExprAngle radians = new ExprAngle();
        radians.init(new Expression[]{new SimpleLiteral<>(Math.PI, false)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals(180D, radians.getSingle(SkriptEvent.EMPTY).doubleValue(), 0.0000001D);
        assertTrue(radians.simplify() instanceof SimplifiedLiteral);
    }

    @Test
    void debugInfoUsesRegisteredClassInfoNames() {
        ExprDebugInfo debugInfo = new ExprDebugInfo();
        debugInfo.init(new Expression[]{new SimpleLiteral<>("hello", false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("\"hello\" (types.string)", debugInfo.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void hashAndTimespanDetailsMatchLocalHelpers() {
        ExprHash hash = new ExprHash();
        SkriptParser.ParseResult hashParse = parseResult("");
        hashParse.tags.add("SHA-256");
        hash.init(new Expression[]{new SimpleLiteral<>("hello", false)}, 0, Kleenean.FALSE, hashParse);
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                hash.getSingle(SkriptEvent.EMPTY));
        assertTrue(hash.simplify() instanceof SimplifiedLiteral);

        ExprTimespanDetails details = new ExprTimespanDetails();
        SkriptParser.ParseResult detailsParse = parseResult("");
        detailsParse.tags.add("minute");
        details.init(new Expression[]{new SimpleLiteral<>(new Timespan(Timespan.TimePeriod.MINUTE, 3), false)},
                0, Kleenean.FALSE, detailsParse);
        assertEquals(3L, details.getSingle(SkriptEvent.EMPTY));
        assertTrue(details.simplify() instanceof SimplifiedLiteral);
    }

    @Test
    void argbReturnsRgbComponentsAndOpaqueAlpha() {
        ExprARGB alpha = new ExprARGB();
        SkriptParser.ParseResult alphaParse = parseResult("");
        alphaParse.tags.add("alpha");
        alpha.init(new Expression[]{new SimpleLiteral<>(new ColorRGB(12, 34, 56), false)}, 0, Kleenean.FALSE, alphaParse);
        assertEquals(255, alpha.getSingle(SkriptEvent.EMPTY));

        ExprARGB red = new ExprARGB();
        SkriptParser.ParseResult redParse = parseResult("");
        redParse.tags.add("red");
        red.init(new Expression[]{new SimpleLiteral<>(new ColorRGB(12, 34, 56), false)}, 0, Kleenean.FALSE, redParse);
        assertEquals(12, red.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
