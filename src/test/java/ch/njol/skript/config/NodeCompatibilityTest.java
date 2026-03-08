package ch.njol.skript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class NodeCompatibilityTest {

    @Test
    void splitLinePreservesHashesInsideStringsAndSeparatesTrailingComment() {
        Node.LineSplit split = Node.splitLine("set {_value} to \"emerald#block\" # trailing comment");

        assertEquals("set {_value} to \"emerald#block\"", split.value().trim());
        assertEquals("# trailing comment", split.comment());
    }

    @Test
    void splitLineUnescapesDoubledHashesOutsideStrings() {
        Node.LineSplit split = Node.splitLine("set {_value} to hello ##world # trailing comment");

        assertEquals("set {_value} to hello #world", split.value().trim());
        assertEquals("# trailing comment", split.comment());
    }

    @Test
    void splitLineTracksBlockCommentsAcrossLines() {
        AtomicBoolean inBlockComment = new AtomicBoolean(false);

        Node.LineSplit start = Node.splitLine("###", inBlockComment);
        Node.LineSplit inside = Node.splitLine("set {_value} to diamond_block", inBlockComment);
        Node.LineSplit end = Node.splitLine("###", inBlockComment);
        Node.LineSplit after = Node.splitLine("set {_value} to emerald_block", inBlockComment);

        assertTrue(start.value().isEmpty());
        assertTrue(inside.value().isEmpty());
        assertTrue(end.value().isEmpty());
        assertEquals("set {_value} to emerald_block", after.value().trim());
    }
}
