package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.util.StringMode;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class VariableStringCompatibilityTest {

    @Test
    void messageStringLeavesPatboxPlaceholderUntouchedWithoutContext() {
        VariableString string = VariableString.newInstance("%player:name%", StringMode.MESSAGE);

        assertEquals("%player:name%", string.toString(SkriptEvent.EMPTY));
    }

    @Test
    void variableNameModeDoesNotResolvePatboxPlaceholderSyntax() {
        VariableString string = VariableString.newInstance("%server:online%", StringMode.VARIABLE_NAME);

        assertEquals("%server:online%", string.toString(SkriptEvent.EMPTY));
    }
}
