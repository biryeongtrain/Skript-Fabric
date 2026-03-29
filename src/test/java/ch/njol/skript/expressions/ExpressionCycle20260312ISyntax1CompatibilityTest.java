package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;

final class ExpressionCycle20260312ISyntax1CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void runtimeLoaderParsesExprNumbersSyntaxVariants() throws Exception {
        Path scriptPath = Files.createTempFile("expr-numbers-syntax", ".sk");
        Files.writeString(
                scriptPath,
                """
                on gametest:
                    loop numbers from 2.5 to 5.5:
                        set {_numbers::*} to loop-value
                    loop integers from 2.9 to 5.1:
                        set {_integers::*} to loop-value
                    loop decimals from 3.94 to 4:
                        set {_decimals::*} to loop-value
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
        assertEquals(1, script.getStructures().size());
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void numbersExpressionPreservesFractionalOffsetAndReverseOrder() {
        ExprNumbers forward = expression(0, 2.5, 5.5, "numbers from 2.5 to 5.5");
        assertArrayEquals(new Number[]{2.5D, 3.5D, 4.5D, 5.5D}, forward.getArray(SkriptEvent.EMPTY));
        assertEquals(Double.class, forward.getReturnType());

        ExprNumbers reverse = expression(0, 5.5, 2.5, "numbers from 5.5 to 2.5");
        assertArrayEquals(new Number[]{5.5D, 4.5D, 3.5D, 2.5D}, collect(reverse.iterator(SkriptEvent.EMPTY)).toArray(Number[]::new));
    }

    @Test
    void integersExpressionRoundsBoundsAndExposesIntegerLoopType() {
        ExprNumbers expression = expression(1, 2.9, 5.1, "integers from 2.9 to 5.1");
        assertArrayEquals(new Number[]{3L, 4L, 5L}, expression.getArray(SkriptEvent.EMPTY));
        assertEquals(Long.class, expression.getReturnType());
        assertTrue(expression.isLoopOf("integer"));
        assertTrue(expression.isLoopOf("int"));

        ExprNumbers reverse = expression(1, 5.1, 2.9, "integers from 5.1 to 2.9");
        assertArrayEquals(new Number[]{5L, 4L, 3L}, collect(reverse.iterator(SkriptEvent.EMPTY)).toArray(Number[]::new));
    }

    @Test
    void decimalsExpressionUsesStartPrecisionForArrayAndIterator() {
        ExprNumbers expression = expression(2, 3.94, 4.0, "decimals from 3.94 to 4");
        assertArrayEquals(
                new Number[]{3.94D, 3.95D, 3.96D, 3.97D, 3.98D, 3.99D, 4.0D},
                expression.getArray(SkriptEvent.EMPTY)
        );
        assertArrayEquals(
                new Number[]{3.94D, 3.95D, 3.96D, 3.97D, 3.98D, 3.99D, 4.0D},
                collect(expression.iterator(SkriptEvent.EMPTY)).toArray(Number[]::new)
        );
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        new ExprNumbers();
        syntaxRegistered = true;
    }

    private static ExprNumbers expression(int mode, Number start, Number end, String expr) {
        ExprNumbers expression = new ExprNumbers();
        SkriptParser.ParseResult parseResult = new SkriptParser.ParseResult();
        parseResult.expr = expr;
        parseResult.mark = mode;
        assertTrue(expression.init(
                new Expression[]{
                        new SimpleLiteral<>(start, false),
                        new SimpleLiteral<>(end, false)
                },
                0,
                Kleenean.FALSE,
                parseResult
        ));
        return expression;
    }

    private static List<Number> collect(Iterator<Number> iterator) {
        assertNotNull(iterator);
        List<Number> values = new ArrayList<>();
        while (iterator.hasNext()) {
            values.add(iterator.next());
        }
        return values;
    }
}
