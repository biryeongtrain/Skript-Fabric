package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class TriggerItemCompatibilityTest {

    @Test
    void walkReturnsFalseWhenRunThrowsException() {
        assertFalse(TriggerItem.walk(new ThrowingTriggerItem(), SkriptEvent.EMPTY));
    }

    @Test
    void walkReturnsFalseWhenRunThrowsStackOverflowError() {
        assertFalse(TriggerItem.walk(new StackOverflowTriggerItem(), SkriptEvent.EMPTY));
    }

    @Test
    void walkRethrowsNonExceptionThrowables() {
        AssertionError error = assertThrows(
                AssertionError.class,
                () -> TriggerItem.walk(new ErrorTriggerItem(), SkriptEvent.EMPTY)
        );
        org.junit.jupiter.api.Assertions.assertEquals("boom", error.getMessage());
    }

    private static final class ThrowingTriggerItem extends TriggerItem {

        @Override
        protected boolean run(SkriptEvent event) {
            throw new IllegalStateException("boom");
        }

        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "throwing trigger item";
        }
    }

    private static final class StackOverflowTriggerItem extends TriggerItem {

        @Override
        protected boolean run(SkriptEvent event) {
            throw new StackOverflowError("boom");
        }

        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "stack overflow trigger item";
        }
    }

    private static final class ErrorTriggerItem extends TriggerItem {

        @Override
        protected boolean run(SkriptEvent event) {
            throw new AssertionError("boom");
        }

        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "error trigger item";
        }
    }
}
