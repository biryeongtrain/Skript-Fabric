package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;

class LangCompatibilityScaffoldingTest {

    @Test
    void expressionInfoConvertsFromModernExpressionInfo() {
        SyntaxInfo.Expression<TestExpression, String> modern = SyntaxInfo.Expression
                .builder(TestExpression.class, String.class)
                .patterns("dummy")
                .originClassPath(TestExpression.class.getName())
                .priority(SyntaxInfo.SIMPLE)
                .build();

        ExpressionInfo<TestExpression, String> legacy = new ExpressionInfo<>(modern);
        assertEquals(String.class, legacy.getReturnType());
        assertEquals(ExpressionType.SIMPLE, legacy.getExpressionType());
    }

    @Test
    void expressionTypeFromModernMatchesKnownPriorities() {
        assertEquals(ExpressionType.SIMPLE, ExpressionType.fromModern(SyntaxInfo.SIMPLE));
        assertEquals(ExpressionType.COMBINED, ExpressionType.fromModern(SyntaxInfo.COMBINED));
        assertEquals(ExpressionType.PATTERN_MATCHES_EVERYTHING, ExpressionType.fromModern(SyntaxInfo.PATTERN_MATCHES_EVERYTHING));
        assertNull(ExpressionType.fromModern(org.skriptlang.skript.util.Priority.before(SyntaxInfo.SIMPLE)));
    }

    @Test
    void syntaxStringBuilderAppendsDebuggableValues() {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(SkriptEvent.EMPTY, false)
                .append("alpha")
                .append(new DummyDebuggable())
                .appendIf(false, "skip")
                .appendIf(true, "omega");

        assertEquals("alpha debuggable omega", builder.toString());
    }

    static class TestExpression implements Expression<String> {

        @Override
        public String[] getArray(SkriptEvent event) {
            return new String[]{"dummy"};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "dummy";
        }
    }

    static class DummyDebuggable implements Debuggable {

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "debuggable";
        }
    }
}
