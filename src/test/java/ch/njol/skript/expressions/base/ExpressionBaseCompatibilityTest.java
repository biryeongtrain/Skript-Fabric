package ch.njol.skript.expressions.base;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.simplification.SimplifiedLiteral;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

class ExpressionBaseCompatibilityTest {

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void propertyExpressionExposesLegacyPropertyPatternsAndPriority() {
        assertArrayEquals(
                new String[]{"[the] length of %strings%", "%strings%'[s] length"},
                PropertyExpression.getPatterns("length", "strings")
        );
        assertArrayEquals(
                new String[]{"[the] length [of %strings%]", "%strings%'[s] length"},
                PropertyExpression.getDefaultPatterns("length", "strings")
        );

        SyntaxInfo.Expression<DummySimplePropertyExpression, String> info =
                PropertyExpression.infoBuilder(
                        DummySimplePropertyExpression.class,
                        String.class,
                        "length",
                        "strings",
                        true
                ).build();
        assertArrayEquals(
                new String[]{"[the] length [of %strings%]", "%strings%'[s] length"},
                info.patterns()
        );
        assertEquals(PropertyExpression.DEFAULT_PRIORITY, info.priority());
    }

    @Test
    void propertyExpressionRegistrationUsesLegacyPatterns() {
        PropertyExpression.register(DummySimplePropertyExpression.class, String.class, "length", "strings");

        List<SyntaxInfo.Expression<?, ?>> syntaxes = collectExpressionSyntaxes();
        assertEquals(1, syntaxes.size());
        assertArrayEquals(
                new String[]{"[the] length of %strings%", "%strings%'[s] length"},
                syntaxes.get(0).patterns()
        );
        assertEquals(DummySimplePropertyExpression.class, syntaxes.get(0).type());
        assertEquals(String.class, syntaxes.get(0).returnType());
    }

    @Test
    void simplePropertyExpressionCapturesRawExprAndConvertsSourceValues() {
        DummySimplePropertyExpression expression = new DummySimplePropertyExpression();
        TestStringExpression source = new TestStringExpression("items", false, "alpha", "beta");

        boolean initialized = expression.init(
                new Expression[]{source},
                0,
                Kleenean.FALSE,
                parseResult("uppercase of {_values::*}")
        );

        assertEquals(true, initialized);
        assertSame(source, expression.getExpr());
        assertEquals("uppercase of {_values::*}", expression.getRawExpr());
        assertArrayEquals(new String[]{"ALPHA", "BETA"}, expression.getArray(SkriptEvent.EMPTY));
        assertEquals("uppercase of items", expression.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void wrapperExpressionSimplifyReturnsSimplifiedLiteralAndDelegatesChanges() {
        TrackingExpression source = new TrackingExpression("wrapped", "value");
        DummyWrapperExpression wrapper = new DummyWrapperExpression(source);

        Object[] delta = new Object[]{"next"};
        assertSame(delta, wrapper.beforeChange(wrapper, delta));
        wrapper.change(SkriptEvent.EMPTY, delta, ChangeMode.SET);
        assertSame(delta, source.lastDelta);
        assertEquals(ChangeMode.SET, source.lastMode);

        Expression<? extends String> simplified = wrapper.simplify();
        SimplifiedLiteral<?> literal = assertInstanceOf(SimplifiedLiteral.class, simplified);
        assertEquals("value", literal.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static List<SyntaxInfo.Expression<?, ?>> collectExpressionSyntaxes() {
        List<SyntaxInfo.Expression<?, ?>> syntaxes = new ArrayList<>();
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            syntaxes.add((SyntaxInfo.Expression<?, ?>) syntax);
        }
        return syntaxes;
    }

    private static final class DummySimplePropertyExpression extends SimplePropertyExpression<String, String> {

        @Override
        public @Nullable String convert(String from) {
            return from.toUpperCase();
        }

        @Override
        protected String getPropertyName() {
            return "uppercase";
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        String getRawExpr() {
            return rawExpr;
        }
    }

    private static final class DummyWrapperExpression extends WrapperExpression<String> {

        private DummyWrapperExpression(Expression<? extends String> expr) {
            setExpr(expr);
        }
    }

    private static class TestStringExpression extends SimpleExpression<String> {

        private final String name;
        private final boolean single;
        private final String[] values;

        private TestStringExpression(String name, boolean single, String... values) {
            this.name = name;
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
            return name;
        }
    }

    private static final class TrackingExpression extends TestStringExpression {

        private Object[] lastDelta;
        private ChangeMode lastMode;

        private TrackingExpression(String name, String value) {
            super(name, true, value);
        }

        @Override
        public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
            return new Class[]{String.class};
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            lastDelta = delta;
            lastMode = mode;
        }

        @Override
        public Expression<? extends String> simplify() {
            return new SimpleLiteral<>(new String[]{"value"}, String.class, true, this);
        }

        @Override
        public Object[] beforeChange(Expression<?> changed, Object[] delta) {
            return delta;
        }
    }
}
