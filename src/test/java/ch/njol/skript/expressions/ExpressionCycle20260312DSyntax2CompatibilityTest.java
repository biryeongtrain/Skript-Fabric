package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.EventValues;
import ch.njol.util.Kleenean;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260312DSyntax2CompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void timeStateSelectsPastAndFutureValues() {
        TimeAwareExpression pastFuture = new TimeAwareExpression("weather", true, true);

        ExprTimeState past = new ExprTimeState();
        assertTrue(past.init(new Expression[]{pastFuture}, 0, Kleenean.FALSE, parseResult("")));
        assertEquals(EventValues.TIME_PAST, pastFuture.getTime());
        assertEquals("past", past.getSingle(SkriptEvent.EMPTY));
        assertTrue(past.setTime(EventValues.TIME_PAST));
        assertFalse(past.setTime(EventValues.TIME_FUTURE));
        assertEquals("the past state of weather", past.toString(SkriptEvent.EMPTY, false));

        TimeAwareExpression futureOnly = new TimeAwareExpression("weather", true, true);
        ExprTimeState future = new ExprTimeState();
        assertTrue(future.init(new Expression[]{futureOnly}, 2, Kleenean.FALSE, parseResult("")));
        assertEquals(EventValues.TIME_FUTURE, futureOnly.getTime());
        assertEquals("future", future.getSingle(SkriptEvent.EMPTY));
        assertEquals("the future state of weather", future.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void timeStateRejectsDelayedOrUnsupportedStates() {
        ExprTimeState delayed = new ExprTimeState();
        assertFalse(delayed.init(new Expression[]{new TimeAwareExpression("value", true, true)}, 0, Kleenean.TRUE, parseResult("")));

        ExprTimeState unsupportedFuture = new ExprTimeState();
        assertFalse(unsupportedFuture.init(new Expression[]{new TimeAwareExpression("value", true, false)}, 2, Kleenean.FALSE, parseResult("")));
    }

    @Test
    void slotIndexUsesContainerOrRawIndex() {
        SimpleContainer container = new SimpleContainer(9);
        Slot slot = new Slot(container, 2, 0, 0);

        ExprSlotIndex normal = new ExprSlotIndex();
        assertTrue(normal.init(new Expression[]{new SimpleLiteral<>(slot, false)}, 0, Kleenean.FALSE, parseResult("")));
        assertEquals(2L, normal.getSingle(SkriptEvent.EMPTY));

        slot.index = 7;
        ExprSlotIndex raw = new ExprSlotIndex();
        SkriptParser.ParseResult rawParse = parseResult("");
        rawParse.tags.add("raw");
        assertTrue(raw.init(new Expression[]{new SimpleLiteral<>(slot, false)}, 0, Kleenean.FALSE, rawParse));
        assertEquals(7L, raw.getSingle(SkriptEvent.EMPTY));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static final class TimeAwareExpression extends SimpleExpression<Object> {

        private final String name;
        private final boolean supportsPast;
        private final boolean supportsFuture;
        private int time = EventValues.TIME_NOW;

        private TimeAwareExpression(String name, boolean supportsPast, boolean supportsFuture) {
            this.name = name;
            this.supportsPast = supportsPast;
            this.supportsFuture = supportsFuture;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{switch (time) {
                case EventValues.TIME_PAST -> "past";
                case EventValues.TIME_FUTURE -> "future";
                default -> "present";
            }};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }

        @Override
        public boolean setTime(int time) {
            if (time == EventValues.TIME_PAST && !supportsPast) {
                return false;
            }
            if (time == EventValues.TIME_FUTURE && !supportsFuture) {
                return false;
            }
            this.time = time;
            return true;
        }

        @Override
        public int getTime() {
            return time;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return name;
        }
    }
}
