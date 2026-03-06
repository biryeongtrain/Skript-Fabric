package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InputSourceCompatibilityTest {

    @Test
    void parseExpressionRestoresPreviousInputSource() {
        ParserInstance parser = ParserInstance.get();
        InputSource.InputData inputData = parser.getData(InputSource.InputData.class);

        DummyInputSource previous = new DummyInputSource();
        DummyInputSource current = new DummyInputSource();
        inputData.setSource(previous);

        Expression<?> parsed = current.parseExpression("anything", parser, SkriptParser.ALL_FLAGS);
        assertNull(parsed);
        assertSame(previous, inputData.getSource());
    }

    @Test
    void dependentInputsCollectionIsExposed() {
        DummyInputSource source = new DummyInputSource();
        DummyExprInput expression = new DummyExprInput();
        source.getDependentInputs().add(expression);

        assertEquals(1, source.getDependentInputs().size());
    }

    private static class DummyInputSource implements InputSource {

        private final Set<ExprInput<?>> dependents = new HashSet<>();

        @Override
        public Set<ExprInput<?>> getDependentInputs() {
            return dependents;
        }

        @Override
        public Object getCurrentValue() {
            return 1;
        }
    }

    private static class DummyExprInput extends ExprInput<Object> {
    }
}
