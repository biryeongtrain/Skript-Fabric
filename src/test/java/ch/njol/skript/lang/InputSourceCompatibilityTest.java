package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class InputSourceCompatibilityTest {

    @AfterEach
    void cleanupClassInfos() {
        Classes.clearClassInfos();
    }

    @Test
    void parseExpressionRestoresPreviousInputSource() {
        ParserInstance parser = ParserInstance.get();
        InputSource.InputData inputData = parser.getData(InputSource.InputData.class);

        DummyInputSource previous = new DummyInputSource();
        DummyInputSource current = new DummyInputSource();
        inputData.setSource(previous);

        Expression<?> parsed = current.parseExpression("anything", parser, SkriptParser.ALL_FLAGS);
        assertNotNull(parsed);
        assertEquals("anything", parsed.getSingle(SkriptEvent.EMPTY));
        assertSame(previous, inputData.getSource());
    }

    @Test
    void parseExpressionUsesPassedParserInsteadOfAmbientThreadLocalParser() {
        ParserInstance ambientParser = ParserInstance.get();
        InputSource.InputData ambientInputData = ambientParser.getData(InputSource.InputData.class);
        DummyInputSource ambientSource = new DummyInputSource("ambient");
        ambientInputData.setSource(ambientSource);

        ParserInstance parser = new ParserInstance();
        DummyInputSource source = new DummyInputSource("alpha");

        Expression<?> parsed = source.parseExpression("input", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertEquals("alpha", parsed.getSingle(SkriptEvent.EMPTY));
        assertSame(ambientSource, ambientInputData.getSource());
        assertNull(parser.getData(InputSource.InputData.class).getSource());
    }

    @Test
    void dependentInputsCollectionIsExposed() {
        DummyInputSource source = new DummyInputSource();
        DummyExprInput expression = new DummyExprInput();
        source.getDependentInputs().add(expression);

        assertEquals(1, source.getDependentInputs().size());
    }

    @Test
    void parseExpressionResolvesInputKeywordAgainstCurrentValue() {
        ParserInstance parser = ParserInstance.get();
        DummyInputSource source = new DummyInputSource("alpha");

        Expression<?> parsed = source.parseExpression("input", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertInstanceOf(ExprInput.class, parsed);
        assertEquals("alpha", parsed.getSingle(SkriptEvent.EMPTY));
        assertEquals("input", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void parseExpressionAcceptsBareStringLiteralMappings() {
        ParserInstance parser = ParserInstance.get();
        DummyInputSource source = new DummyInputSource("alpha");

        Expression<?> parsed = source.parseExpression("plain text", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertEquals("plain text", parsed.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parseExpressionResolvesInputIndexWhenSourceHasIndices() {
        ParserInstance parser = ParserInstance.get();
        DummyIndexedInputSource source = new DummyIndexedInputSource("value", "slot-3");

        Expression<?> parsed = source.parseExpression("input index", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertInstanceOf(ExprInput.class, parsed);
        assertEquals("slot-3", parsed.getSingle(SkriptEvent.EMPTY));
        assertEquals("input index", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void parseExpressionFallsBackToLiteralWhenInputIndexIsUnavailable() {
        ParserInstance parser = ParserInstance.get();
        DummyInputSource source = new DummyInputSource("alpha");

        Expression<?> parsed = source.parseExpression("input index", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertEquals("input index", parsed.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void parseExpressionResolvesTypedInputAgainstRegisteredClassInfo() {
        ParserInstance parser = ParserInstance.get();
        FooValue fooValue = new FooValue();
        DummyInputSource source = new DummyInputSource(fooValue);
        Classes.registerClassInfo(new ClassInfo<>(FooValue.class, "foo"));

        Expression<?> parsed = source.parseExpression("foo input", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertInstanceOf(ExprInput.class, parsed);
        assertSame(fooValue, parsed.getSingle(SkriptEvent.EMPTY));
        assertEquals("foo input", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void parseExpressionMatchesSpacedUserInputToCompactCodeName() {
        ParserInstance parser = ParserInstance.get();
        FooBarValue fooBarValue = new FooBarValue();
        DummyInputSource source = new DummyInputSource(fooBarValue);
        Classes.registerClassInfo(new ClassInfo<>(FooBarValue.class, "foobar"));

        Expression<?> parsed = source.parseExpression("foo bar input", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertSame(fooBarValue, parsed.getSingle(SkriptEvent.EMPTY));
        assertEquals("foobar input", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void parseExpressionFallsBackToLiteralWhenTypedInputFormIsPlural() {
        ParserInstance parser = ParserInstance.get();
        DummyInputSource source = new DummyInputSource(new FooValue());
        Classes.registerClassInfo(new ClassInfo<>(FooValue.class, "foo"));

        Expression<?> parsed = source.parseExpression("foos input", parser, SkriptParser.ALL_FLAGS);

        assertNotNull(parsed);
        assertEquals("foos input", parsed.getSingle(SkriptEvent.EMPTY));
    }

    private static class DummyInputSource implements InputSource {

        private final Set<ExprInput<?>> dependents = new HashSet<>();
        private final Object currentValue;

        private DummyInputSource() {
            this(1);
        }

        private DummyInputSource(Object currentValue) {
            this.currentValue = currentValue;
        }

        @Override
        public Set<ExprInput<?>> getDependentInputs() {
            return dependents;
        }

        @Override
        public Object getCurrentValue() {
            return currentValue;
        }
    }

    private static final class DummyIndexedInputSource extends DummyInputSource {

        private final String currentIndex;

        private DummyIndexedInputSource(Object currentValue, String currentIndex) {
            super(currentValue);
            this.currentIndex = currentIndex;
        }

        @Override
        public boolean hasIndices() {
            return true;
        }

        @Override
        public String getCurrentIndex() {
            return currentIndex;
        }
    }

    private static class DummyExprInput extends ExprInput<Object> {
    }

    private static final class FooValue {
    }

    private static final class FooBarValue {
    }
}
