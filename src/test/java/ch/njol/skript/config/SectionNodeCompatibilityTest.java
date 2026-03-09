package ch.njol.skript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SectionNodeCompatibilityTest {

    @Test
    void getAndGetValueAreCaseInsensitiveForMappedNodes() {
        SectionNode node = new SectionNode("root");
        EntryNode entry = new EntryNode("Marker", "emerald_block");
        SectionNode child = new SectionNode("Nested");
        node.add(entry);
        node.add(child);

        assertSame(entry, node.get("marker"));
        assertSame(child, node.get("nested"));
        assertEquals("emerald_block", node.getValue("MARKER"));
    }

    @Test
    void addMovesNodesBetweenParents() {
        SectionNode first = new SectionNode("first");
        SectionNode second = new SectionNode("second");
        EntryNode entry = new EntryNode("marker", "emerald_block");
        first.add(entry);

        second.add(entry);

        assertNull(first.get("marker"));
        assertSame(entry, second.get("marker"));
        assertSame(second, entry.getParent());
    }

    @Test
    void setReplacesExistingNodeWithoutChangingOrder() {
        SectionNode node = new SectionNode("root");
        EntryNode first = new EntryNode("first", "one");
        EntryNode second = new EntryNode("second", "two");
        node.add(first);
        node.add(second);

        SectionNode replacement = new SectionNode("second");
        node.set("second", replacement);

        assertSame(first, node.iterator().next());
        assertSame(replacement, node.get("SECOND"));
        assertSame(node, replacement.getParent());
        assertNull(second.getParent());
    }

    @Test
    void removeByKeyClearsParentAndLookup() {
        SectionNode node = new SectionNode("root");
        EntryNode entry = new EntryNode("marker", "emerald_block");
        node.add(entry);

        Node removed = node.remove("MARKER");

        assertSame(entry, removed);
        assertNull(entry.getParent());
        assertNull(node.get("marker"));
        assertTrue(node.isEmpty());
    }

    @Test
    void renamingMappedNodeRefreshesLookupKeyLikeUpstream() {
        SectionNode node = new SectionNode("root");
        EntryNode entry = new EntryNode("Marker", "emerald_block");
        node.add(entry);

        entry.setKey("Beacon");

        assertNull(node.get("marker"));
        assertSame(entry, node.get("beacon"));
        assertEquals("emerald_block", node.getValue("BEACON"));
    }

    @Test
    void convertToEntriesUpdatesMappedLookups() {
        SectionNode node = new SectionNode("root");
        node.add(new SimpleNode("Marker: emerald_block"));

        node.convertToEntries(-1);

        assertEquals("emerald_block", node.getValue("marker"));
        assertTrue(node.get("marker") instanceof EntryNode);
    }
}
