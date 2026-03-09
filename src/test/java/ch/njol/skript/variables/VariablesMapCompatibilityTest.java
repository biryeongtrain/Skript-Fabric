package ch.njol.skript.variables;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class VariablesMapCompatibilityTest {

    @Test
    void copyKeepsNestedListBranchesIndependent() {
        VariablesMap original = new VariablesMap();
        original.setVariable("scores", "lapis_block");
        original.setVariable("scores::group::2", "gold_block");

        VariablesMap copy = original.copy();
        copy.setVariable("scores::group::10", "emerald_block");
        copy.setVariable("scores", "diamond_block");

        Map<?, ?> originalRoot = assertInstanceOf(Map.class, original.getVariable("scores::*"));
        Map<?, ?> originalGroup = assertInstanceOf(Map.class, originalRoot.get("group"));
        assertEquals("lapis_block", originalRoot.get(null));
        assertEquals(List.of("2"), new ArrayList<>(originalGroup.keySet()));

        Map<?, ?> copiedRoot = assertInstanceOf(Map.class, copy.getVariable("scores::*"));
        Map<?, ?> copiedGroup = assertInstanceOf(Map.class, copiedRoot.get("group"));
        assertEquals("diamond_block", copiedRoot.get(null));
        assertEquals(List.of("2", "10"), new ArrayList<>(copiedGroup.keySet()));
    }

    @Test
    void deletingListBranchRemovesDescendantsButKeepsDirectNodeValue() {
        VariablesMap map = new VariablesMap();
        map.setVariable("scores::group", "diamond_block");
        map.setVariable("scores::group::1", "gold_block");
        map.setVariable("scores::group::nested::2", "emerald_block");

        map.setVariable("scores::group::*", null);

        assertEquals("diamond_block", map.getVariable("scores::group"));
        assertNull(map.getVariable("scores::group::1"));
        assertNull(map.getVariable("scores::group::nested::2"));

        Map<?, ?> root = assertInstanceOf(Map.class, map.getVariable("scores::*"));
        assertEquals(List.of("group"), new ArrayList<>(root.keySet()));
        assertEquals("diamond_block", root.get("group"));
    }
}
