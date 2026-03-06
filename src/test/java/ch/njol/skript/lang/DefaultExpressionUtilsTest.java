package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class DefaultExpressionUtilsTest {

    @Test
    void nullDefaultExpressionReturnsNotFoundError() {
        SkriptParser.ExprInfo info = new SkriptParser.ExprInfo();
        DefaultExpressionUtils.DefaultExpressionError error = DefaultExpressionUtils.isValid(null, info, 0);
        assertEquals(DefaultExpressionUtils.DefaultExpressionError.NOT_FOUND, error);
    }

    @Test
    void literalBlockedByFlagsReturnsLiteralError() {
        SkriptParser.ExprInfo info = new SkriptParser.ExprInfo();
        info.flagMask = SkriptParser.PARSE_EXPRESSIONS;

        DefaultExpressionUtils.DefaultExpressionError error = DefaultExpressionUtils.isValid(
                new DummyDefaultExpression(true, true),
                info,
                0
        );

        assertEquals(DefaultExpressionUtils.DefaultExpressionError.LITERAL, error);
    }

    @Test
    void notSingleExpressionForSingleSlotReturnsNotSingleError() {
        SkriptParser.ExprInfo info = new SkriptParser.ExprInfo();
        info.isPlural = new boolean[]{false};

        DefaultExpressionUtils.DefaultExpressionError error = DefaultExpressionUtils.isValid(
                new DummyDefaultExpression(false, true),
                info,
                0
        );

        assertEquals(DefaultExpressionUtils.DefaultExpressionError.NOT_SINGLE, error);
    }

    @Test
    void errorMessageFormatterBuildsReadableMessage() {
        String message = DefaultExpressionUtils.DefaultExpressionError.NOT_FOUND
                .getError(List.of("item type", "entity"), "%item/entity%");

        assertNotNull(message);
        assertTrue(message.contains("item type and entity"));
    }

    private static class DummyDefaultExpression implements DefaultExpression<Object>, Literal<Object> {

        private final boolean single;
        private final boolean acceptsTime;

        private DummyDefaultExpression(boolean single, boolean acceptsTime) {
            this.single = single;
            this.acceptsTime = acceptsTime;
        }

        @Override
        public boolean init() {
            return true;
        }

        @Override
        public Object[] getArray(SkriptEvent event) {
            return new Object[]{1, 2};
        }

        @Override
        public boolean isSingle() {
            return single;
        }

        @Override
        public boolean setTime(int time) {
            return acceptsTime;
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "dummy";
        }
    }
}
