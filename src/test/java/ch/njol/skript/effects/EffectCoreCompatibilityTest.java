package ch.njol.skript.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class EffectCoreCompatibilityTest {

    @AfterEach
    void clearVariables() {
        Variables.clearAll();
        ParserInstance.get().reset();
    }

    @Test
    void localEffChangeSetsAndDeletesThroughCompatSurface() throws Exception {
        MutableStringExpression target = new MutableStringExpression();
        EffChange set = new EffChange();
        assertTrue(set.init(new Expression<?>[]{target, new ConstantExpression<>(String.class, "changed")}, 0, Kleenean.FALSE, new ParseResult()));

        execute(set);
        assertEquals("changed", target.value);

        EffChange delete = new EffChange();
        assertTrue(delete.init(new Expression<?>[]{target}, 4, Kleenean.FALSE, new ParseResult()));
        execute(delete);
        assertNull(target.value);
    }

    @Test
    void delayInitMarksParserAsDelayed() {
        Delay delay = new Delay();
        assertTrue(delay.init(
                new Expression<?>[]{new ConstantExpression<>(Timespan.class, new Timespan(Timespan.TimePeriod.TICK, 1))},
                0,
                Kleenean.FALSE,
                new ParseResult()
        ));
        assertTrue(ParserInstance.get().getHasDelayBefore().isTrue());
    }

    private void execute(Object effect) throws Exception {
        Method execute = effect.getClass().getSuperclass().getDeclaredMethod("execute", SkriptEvent.class);
        execute.setAccessible(true);
        execute.invoke(effect, SkriptEvent.EMPTY);
    }

    private static final class MutableStringExpression extends SimpleExpression<String> {

        private @Nullable String value;

        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return value == null ? null : new String[]{value};
        }

        @Override
        public Class<?>[] acceptChange(ChangeMode mode) {
            return switch (mode) {
                case SET -> new Class[]{String.class};
                case DELETE -> new Class[0];
                default -> null;
            };
        }

        @Override
        public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
            if (mode == ChangeMode.DELETE) {
                value = null;
                return;
            }
            value = delta == null || delta.length == 0 ? null : (String) delta[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "mutable-string";
        }
    }

    private static final class ConstantExpression<T> extends SimpleExpression<T> {

        private final Class<? extends T> returnType;
        private final T[] values;

        @SafeVarargs
        private ConstantExpression(Class<? extends T> returnType, T... values) {
            this.returnType = returnType;
            this.values = values;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return values.length == 0 || values[0] == null ? "null" : values[0].toString();
        }
    }
}
