package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;

class ParserCompatibilityDataAndStackTest {

    @Test
    void defaultValueDataUsesLifoByType() {
        ParserInstance parser = new ParserInstance();
        DefaultValueData data = new DefaultValueData(parser);

        DummyDefaultExpression first = new DummyDefaultExpression(1);
        DummyDefaultExpression second = new DummyDefaultExpression(2);

        data.addDefaultValue(Integer.class, first);
        assertEquals(first, data.getDefaultValue(Integer.class));

        data.addDefaultValue(Integer.class, second);
        assertEquals(second, data.getDefaultValue(Integer.class));

        data.removeDefaultValue(Integer.class);
        assertEquals(first, data.getDefaultValue(Integer.class));

        data.removeDefaultValue(Integer.class);
        assertNull(data.getDefaultValue(Integer.class));
        assertThrows(IllegalStateException.class, () -> data.removeDefaultValue(Integer.class));
    }

    @Test
    void defaultValueDataRequiresExactTypeMatch() {
        ParserInstance parser = new ParserInstance();
        DefaultValueData data = new DefaultValueData(parser);

        DefaultExpression<Object> objectDefault = new SimpleLiteral<>(new Object[]{1}, Object.class, true, true, null);
        DefaultExpression<Number> numberDefault = new SimpleLiteral<>(new Number[]{2}, Number.class, true, true, null);
        DefaultExpression<Integer> integerDefault = new SimpleLiteral<>(3, true);

        data.addDefaultValue(Object.class, objectDefault);
        data.addDefaultValue(Number.class, numberDefault);
        assertNull(data.getDefaultValue(Integer.class));

        data.addDefaultValue(Integer.class, integerDefault);
        assertEquals(integerDefault, data.getDefaultValue(Integer.class));

        data.removeDefaultValue(Integer.class);
        assertNull(data.getDefaultValue(Integer.class));
    }

    @Test
    void parsingStackTracksElementsAndPrintsReadableState() {
        ParsingStack stack = new ParsingStack();
        SyntaxInfo<DummySyntax> info = new SyntaxInfo<>(DummySyntax.class, new String[]{"alpha", "beta"}, "test");
        ParsingStack.Element element = new ParsingStack.Element(info, 1);

        stack.push(element);
        assertEquals(1, stack.size());
        assertEquals(element, stack.peek());
        assertEquals("beta", stack.peek().getPattern());

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        stack.print(new PrintStream(output));
        String printed = output.toString();
        assertTrue(printed.contains("DummySyntax"));
        assertTrue(printed.contains(" @ 1"));

        assertEquals(element, stack.pop());
        assertTrue(stack.isEmpty());
        assertThrows(IllegalStateException.class, stack::pop);
    }

    @Test
    void parseStackOverflowExceptionEmbedsStackDump() {
        ParsingStack stack = new ParsingStack();
        SyntaxInfo<DummySyntax> info = new SyntaxInfo<>(DummySyntax.class, new String[]{"alpha"}, "test");
        stack.push(new ParsingStack.Element(info, 0));

        ParseStackOverflowException exception = new ParseStackOverflowException(new StackOverflowError(), stack);
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Stack:"));
        assertTrue(exception.getMessage().contains("DummySyntax"));
    }

    private static class DummyDefaultExpression implements DefaultExpression<Integer> {

        private final Integer value;

        private DummyDefaultExpression(Integer value) {
            this.value = value;
        }

        @Override
        public boolean init() {
            return true;
        }

        @Override
        public Integer[] getArray(SkriptEvent event) {
            return new Integer[]{value};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isDefault() {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return String.valueOf(value);
        }
    }

    public static class DummySyntax implements SyntaxElement {
        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "dummy";
        }
    }
}
