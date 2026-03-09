package ch.njol.skript.conditions.base;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

class PropertyConditionCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void propertyConditionPatternsCoverAllLegacyPropertyTypes() {
        assertArrayEquals(
                new String[]{"%players% (is|are) invisible", "%players% (isn't|is not|aren't|are not) invisible"},
                PropertyCondition.getPatterns(PropertyCondition.PropertyType.BE, "invisible", "players")
        );
        assertArrayEquals(
                new String[]{"%players% can fly", "%players% (can't|cannot|can not) fly"},
                PropertyCondition.getPatterns(PropertyCondition.PropertyType.CAN, "fly", "players")
        );
        assertArrayEquals(
                new String[]{"%players% (has|have) metadata", "%players% (doesn't|does not|do not|don't) have metadata"},
                PropertyCondition.getPatterns(PropertyCondition.PropertyType.HAVE, "metadata", "players")
        );
        assertArrayEquals(
                new String[]{"%players% will burn", "%players% (will (not|neither)|won't) burn"},
                PropertyCondition.getPatterns(PropertyCondition.PropertyType.WILL, "burn", "players")
        );
    }

    @Test
    void propertyConditionRegistrationUsesGeneratedPatterns() {
        PropertyCondition.register(DummyPropertyCondition.class, PropertyCondition.PropertyType.CAN, "fly", "players");

        List<SyntaxInfo<?>> syntaxes = collectConditionSyntaxes();
        assertEquals(1, syntaxes.size());
        assertArrayEquals(
                new String[]{"%players% can fly", "%players% (can't|cannot|can not) fly"},
                syntaxes.get(0).patterns()
        );
        assertEquals(DummyPropertyCondition.class, syntaxes.get(0).type());
    }

    @Test
    void propertyConditionInitSetsExpressionAndNegation() {
        DummyPropertyCondition condition = new DummyPropertyCondition(PropertyCondition.PropertyType.BE, "allowed");
        TestStringExpression source = new TestStringExpression("player", true, "allowed");

        boolean initialized = condition.init(
                new Expression[]{source},
                1,
                Kleenean.FALSE,
                parseResult("player is not allowed")
        );

        assertTrue(initialized);
        assertSame(source, condition.getExpr());
        assertTrue(condition.isNegated());
        assertFalse(condition.check(SkriptEvent.EMPTY));
    }

    @Test
    void propertyConditionToStringTracksSingularPluralAndNegation() {
        DummyPropertyCondition beCondition = new DummyPropertyCondition(PropertyCondition.PropertyType.BE, "visible");
        beCondition.init(new Expression[]{new TestStringExpression("player", true, "visible")}, 0, Kleenean.FALSE, parseResult("player is visible"));
        assertEquals("player is visible", beCondition.toString(SkriptEvent.EMPTY, false));

        DummyPropertyCondition haveCondition = new DummyPropertyCondition(PropertyCondition.PropertyType.HAVE, "metadata");
        haveCondition.init(new Expression[]{new TestStringExpression("players", false, "allowed")}, 1, Kleenean.FALSE, parseResult("players don't have metadata"));
        assertEquals("players don't have metadata", haveCondition.toString(SkriptEvent.EMPTY, false));

        DummyPropertyCondition canCondition = new DummyPropertyCondition(PropertyCondition.PropertyType.CAN, "fly");
        canCondition.init(new Expression[]{new TestStringExpression("players", false, "allowed")}, 0, Kleenean.FALSE, parseResult("players can fly"));
        assertEquals("players can fly", canCondition.toString(SkriptEvent.EMPTY, false));

        DummyPropertyCondition willCondition = new DummyPropertyCondition(PropertyCondition.PropertyType.WILL, "burning");
        willCondition.init(new Expression[]{new TestStringExpression("player", true, "allowed")}, 1, Kleenean.FALSE, parseResult("player won't be burning"));
        assertEquals("player won't be burning", willCondition.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void objectBasedToStringFallbackStaysAvailable() {
        String text = PropertyCondition.toString(
                new Object(),
                PropertyCondition.PropertyType.HAVE,
                SkriptEvent.EMPTY,
                false,
                new TestStringExpression("player", true, "allowed"),
                "metadata"
        );

        assertEquals("player has metadata", text);
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static List<SyntaxInfo<?>> collectConditionSyntaxes() {
        List<SyntaxInfo<?>> syntaxes = new ArrayList<>();
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.CONDITION)) {
            syntaxes.add(syntax);
        }
        return syntaxes;
    }

    private static final class DummyPropertyCondition extends PropertyCondition<String> {

        private final PropertyType propertyType;
        private final String propertyName;

        private DummyPropertyCondition(PropertyType propertyType, String propertyName) {
            this.propertyType = propertyType;
            this.propertyName = propertyName;
        }

        @Override
        public boolean check(String value) {
            return "allowed".equals(value) || "visible".equals(value);
        }

        @Override
        protected String getPropertyName() {
            return propertyName;
        }

        @Override
        protected PropertyType getPropertyType() {
            return propertyType;
        }
    }

    private static final class TestStringExpression extends SimpleExpression<String> {

        private final String text;
        private final boolean single;
        private final String[] values;

        private TestStringExpression(String text, boolean single, String... values) {
            this.text = text;
            this.single = single;
            this.values = values;
        }

        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return values.clone();
        }

        @Override
        public boolean isSingle() {
            return single;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return text;
        }
    }
}
