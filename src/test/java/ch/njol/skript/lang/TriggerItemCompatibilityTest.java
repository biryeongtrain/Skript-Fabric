package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class TriggerItemCompatibilityTest {

    @Test
    void walkReturnsFalseWhenRunThrowsException() {
        assertFalse(TriggerItem.walk(new ThrowingTriggerItem(), SkriptEvent.EMPTY));
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
}
