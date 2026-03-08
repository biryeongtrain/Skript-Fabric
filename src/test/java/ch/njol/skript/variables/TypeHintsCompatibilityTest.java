package ch.njol.skript.variables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.script.Script;

class TypeHintsCompatibilityTest {

    @AfterEach
    void cleanupVariables() {
        Variables.clearAll();
        Variables.caseInsensitiveVariables = true;
        TypeHints.clear();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void typeHintsAddAndGetNarrowLocalVariablesThroughHintManager() {
        ParserInstance.get().setCurrentScript(new Script(null, List.of()));
        TypeHints.clear();

        assertNull(TypeHints.get("value"));

        TypeHints.add("value", Integer.class);

        assertEquals(Integer.class, TypeHints.get("value"));

        Variable<Object> variable = Variable.newInstance("_value", new Class[]{Object.class});

        assertNotNull(variable);
        assertEquals(Integer.class, variable.getReturnType());
    }

    @Test
    void typeHintsScopeExitAndClearRestoreVisibleRuntimeHints() {
        ParserInstance.get().setCurrentScript(new Script(null, List.of()));
        TypeHints.clear();
        TypeHints.add("value", Integer.class);

        Variable<Object> outer = Variable.newInstance("_value", new Class[]{Object.class});
        assertNotNull(outer);
        assertEquals(Integer.class, outer.getReturnType());

        TypeHints.enterScope();
        TypeHints.add("{_value}", String.class);

        Variable<Object> inner = Variable.newInstance("_value", new Class[]{Object.class});
        assertNotNull(inner);
        assertEquals(String.class, inner.getReturnType());
        assertEquals(String.class, TypeHints.get("value"));

        TypeHints.exitScope();

        Variable<Object> restored = Variable.newInstance("_value", new Class[]{Object.class});
        assertNotNull(restored);
        assertEquals(Integer.class, restored.getReturnType());
        assertEquals(Integer.class, TypeHints.get("value"));

        TypeHints.clear();

        assertNull(TypeHints.get("value"));

        Variable<Object> cleared = Variable.newInstance("_value", new Class[]{Object.class});
        assertNotNull(cleared);
        assertEquals(Object.class, cleared.getReturnType());
    }
}
