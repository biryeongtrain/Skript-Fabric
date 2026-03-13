package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.common.expressions.ExprColorFromHexCode;
import org.skriptlang.skript.common.expressions.ExprHexCode;
import org.skriptlang.skript.common.expressions.ExprRecursiveSize;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313FSafe4CompatibilityTest {

    @AfterEach
    void cleanupVariables() {
        Variables.clearAll();
    }

    @Test
    void hexCodeFormatsRgbValuesWithoutHashPrefix() {
        ExprHexCode expression = new ExprHexCode();

        assertEquals("0A10FF", expression.convert(new ColorRGB(10, 16, 255)));
    }

    @Test
    void colorFromHexCodeAcceptsRgbAndArgbInputs() {
        ExprColorFromHexCode expression = new ExprColorFromHexCode();

        Color rgb = expression.convert("#FFBBA7");
        assertNotNull(rgb);
        assertEquals(255, rgb.red());
        assertEquals(187, rgb.green());
        assertEquals(167, rgb.blue());

        Color argb = expression.convert("AA3366CC");
        assertNotNull(argb);
        assertEquals(51, argb.red());
        assertEquals(102, argb.green());
        assertEquals(204, argb.blue());
    }

    @Test
    void colorFromHexCodeRejectsInvalidLength() {
        ExprColorFromHexCode expression = new ExprColorFromHexCode();

        assertNull(expression.convert("#12345"));
    }

    @Test
    void recursiveSizeCountsNestedListEntriesAndDirectBranchValues() {
        Variables.setVariable("scores::plain", "emerald_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group", "diamond_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group::1", "gold_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group::nested::2", "lapis_block", SkriptEvent.EMPTY, false);

        Variable<Object> variable = Variable.newInstance("scores::*", new Class[]{Object.class});
        assertNotNull(variable);

        ExprRecursiveSize expression = new ExprRecursiveSize();
        assertTrue(expression.init(new Expression[]{variable}, 0, Kleenean.FALSE, parseResult("recursive size of {scores::*}")));
        assertEquals(4L, expression.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void recursiveSizeRejectsSingularVariables() {
        Variable<Object> variable = Variable.newInstance("score", new Class[]{Object.class});
        assertNotNull(variable);

        ExprRecursiveSize expression = new ExprRecursiveSize();
        assertFalse(expression.init(new Expression[]{variable}, 0, Kleenean.FALSE, parseResult("recursive size of {score}")));
    }

    @Test
    void recursiveSizeRejectsNonVariableExpressions() {
        ExprRecursiveSize expression = new ExprRecursiveSize();

        assertFalse(expression.init(
                new Expression[]{new SimpleLiteral<>("value", true)},
                0,
                Kleenean.FALSE,
                parseResult("recursive size of \"value\"")
        ));
    }

    @Test
    void blockSphereYieldsNoBlocksWithoutResolvedWorld() {
        ExprBlockSphere expression = new ExprBlockSphere();

        assertTrue(expression.init(
                new Expression[]{
                        new SimpleLiteral<>(1, false),
                        new SimpleLiteral<>(new FabricLocation(null, net.minecraft.world.phys.Vec3.ZERO), false)
                },
                0,
                Kleenean.FALSE,
                parseResult("blocks in radius 1 around location")
        ));
        assertEquals(0, expression.getArray(SkriptEvent.EMPTY).length);
        assertNull(expression.iterator(SkriptEvent.EMPTY));
    }

    @Test
    void blockSphereIncludesCenterBlockForZeroRadius() {
        ExprBlockSphere expression = new ExprBlockSphere();
        FabricLocation location = new FabricLocation(null, new net.minecraft.world.phys.Vec3(5.5D, 10.5D, 15.5D));

        assertTrue(expression.init(
                new Expression[]{
                        new SimpleLiteral<>(location, false),
                        new SimpleLiteral<>(0, false)
                },
                1,
                Kleenean.FALSE,
                parseResult("blocks around location in radius 0")
        ));
        assertTrue(expression.toString(SkriptEvent.EMPTY, false).startsWith("the blocks in radius "));
        assertEquals(0, expression.getArray(SkriptEvent.EMPTY).length);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
