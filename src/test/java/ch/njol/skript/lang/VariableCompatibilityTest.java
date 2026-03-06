package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.variables.Variables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class VariableCompatibilityTest {

    @AfterEach
    void cleanupVariables() {
        Variables.clearAll();
    }

    @Test
    void validatesVariableNameRules() {
        assertTrue(Variable.isValidVariableName("health", true, false));
        assertTrue(Variable.isValidVariableName("data::*", true, false));
        assertFalse(Variable.isValidVariableName("::bad", true, false));
        assertFalse(Variable.isValidVariableName("bad::", true, false));
        assertFalse(Variable.isValidVariableName("bad:*", true, false));
    }

    @Test
    void newInstanceResolvesLocalAndListFlags() {
        Variable<Integer> variable = Variable.newInstance("_stats::*", new Class[]{Integer.class});

        assertNotNull(variable);
        assertTrue(variable.isLocal());
        assertTrue(variable.isList());
        assertFalse(variable.isSingle());
    }

    @Test
    void singleVariableSupportsSetAndAdd() {
        Variable<Number> variable = Variable.newInstance("count", new Class[]{Number.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{5}, ChangeMode.SET);
        assertEquals(5, variable.getSingle(SkriptEvent.EMPTY));

        variable.change(SkriptEvent.EMPTY, new Object[]{3}, ChangeMode.ADD);
        assertEquals(8L, variable.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void listVariableExposesValuesAndKeys() {
        Variable<Integer> variable = Variable.newInstance("scores::*", new Class[]{Integer.class});
        assertNotNull(variable);

        variable.change(SkriptEvent.EMPTY, new Object[]{1, 2}, ChangeMode.SET);
        variable.change(SkriptEvent.EMPTY, new Object[]{9}, ChangeMode.SET, new String[]{"foo"});

        assertArrayEquals(new Integer[]{1, 2, 9}, variable.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"1", "2", "foo"}, variable.getArrayKeys(SkriptEvent.EMPTY));

        variable.change(SkriptEvent.EMPTY, new Object[]{0}, ChangeMode.DELETE, new String[]{"foo"});
        assertArrayEquals(new String[]{"1", "2"}, variable.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void localVariablesAreScopedByEventHandle() {
        Variable<String> variable = Variable.newInstance("_session", new Class[]{String.class});
        assertNotNull(variable);

        SkriptEvent first = new SkriptEvent(new Object(), null, null, null);
        SkriptEvent second = new SkriptEvent(new Object(), null, null, null);

        variable.change(first, new Object[]{"alpha"}, ChangeMode.SET);

        assertEquals("alpha", variable.getSingle(first));
        assertNull(variable.getSingle(second));
    }
}
