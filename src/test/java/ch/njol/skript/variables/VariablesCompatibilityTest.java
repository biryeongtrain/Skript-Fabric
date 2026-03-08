package ch.njol.skript.variables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class VariablesCompatibilityTest {

    @AfterEach
    void cleanupVariables() {
        Variables.clearAll();
        Variables.caseInsensitiveVariables = true;
    }

    @Test
    void getVariableReturnsNestedListMapForDirectParentAndDescendants() {
        Variables.setVariable("scores::plain", "emerald_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group", "diamond_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group::10", "emerald_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group::2", "gold_block", SkriptEvent.EMPTY, false);

        Object raw = Variables.getVariable("scores::*", SkriptEvent.EMPTY, false);

        Map<?, ?> values = assertInstanceOf(Map.class, raw);
        assertEquals(List.of("group", "plain"), new ArrayList<>(values.keySet()));
        assertEquals("emerald_block", values.get("plain"));

        Map<?, ?> group = assertInstanceOf(Map.class, values.get("group"));
        assertEquals(Arrays.asList(null, "2", "10"), new ArrayList<>(group.keySet()));
        assertEquals("diamond_block", group.get(null));
        assertEquals("gold_block", group.get("2"));
        assertEquals("emerald_block", group.get("10"));
    }

    @Test
    void getVariablesWithPrefixKeepsShallowDirectParentValuesWhenDescendantsExist() {
        Variables.setVariable("scores::plain", "emerald_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group", "diamond_block", SkriptEvent.EMPTY, false);
        Variables.setVariable("scores::group::1", "gold_block", SkriptEvent.EMPTY, false);

        Map<String, Object> values = Variables.getVariablesWithPrefix("scores::", SkriptEvent.EMPTY, false);
        Object raw = Variables.getVariable("scores::*", SkriptEvent.EMPTY, false);

        assertEquals(List.of("scores::group", "scores::plain"), new ArrayList<>(values.keySet()));
        assertEquals(List.of("diamond_block", "emerald_block"), new ArrayList<>(values.values()));

        Map<?, ?> nested = assertInstanceOf(Map.class, raw);
        Map<?, ?> group = assertInstanceOf(Map.class, nested.get("group"));
        assertEquals("diamond_block", group.get(null));
        assertEquals("gold_block", group.get("1"));
    }

    @Test
    void getVariableReturnsNestedListMapForDescendantOnlyEntries() {
        Variables.setVariable("scores::group::1", "gold_block", SkriptEvent.EMPTY, false);

        Object raw = Variables.getVariable("scores::*", SkriptEvent.EMPTY, false);

        Map<?, ?> values = assertInstanceOf(Map.class, raw);
        Map<?, ?> group = assertInstanceOf(Map.class, values.get("group"));
        assertEquals(List.of("group"), new ArrayList<>(values.keySet()));
        assertEquals(List.of("1"), new ArrayList<>(group.keySet()));
        assertNull(group.get(null));
        assertEquals("gold_block", group.get("1"));
    }
}
