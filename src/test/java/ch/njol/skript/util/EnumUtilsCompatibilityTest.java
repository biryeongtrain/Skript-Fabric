package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.localization.Language;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EnumUtilsCompatibilityTest {

    private enum TestTool {
        HAMMER,
        APPLE_PIE
    }

    @AfterEach
    void resetLanguage() {
        Language.clear();
    }

    @Test
    void enumUtilsParsesLocalizedNamesAndArticles() {
        Language.loadDefault(defaultEntries());
        EnumUtils<TestTool> tools = new EnumUtils<>(TestTool.class, "tool");

        assertEquals(TestTool.HAMMER, tools.parse("hammer"));
        assertEquals(TestTool.APPLE_PIE, tools.parse("an apple pie"));
        assertEquals("apple pie", tools.toString(TestTool.APPLE_PIE, StringMode.MESSAGE));
        assertTrue(tools.getAllNames().contains("hammer"));
        assertTrue(tools.getAllNames().contains("apple pie"));
    }

    @Test
    void enumUtilsFallsBackToDerivedNameWhenLanguageEntryIsMissing() {
        Language.loadDefault(defaultEntries());
        EnumUtils<TestTool> tools = new EnumUtils<>(TestTool.class, "missing");

        assertEquals(TestTool.APPLE_PIE, tools.parse("apple pie missing"));
        assertEquals("APPLE_PIE", tools.toString(TestTool.APPLE_PIE, 0));
    }

    @Test
    void enumUtilsRefreshesAfterLanguageReload() {
        Language.loadDefault(defaultEntries());
        EnumUtils<TestTool> tools = new EnumUtils<>(TestTool.class, "tool");
        Language.loadDefault(updatedEntries());

        assertEquals(TestTool.HAMMER, tools.parse("mallet"));
        assertEquals("mallet", tools.toString(TestTool.HAMMER, 0));
        assertNull(tools.parse("hammer"));
        assertNotNull(tools.parse("mallet"));
    }

    private static Map<String, String> defaultEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("tool.hammer", "hammer");
        entries.put("tool.apple_pie", "apple pie@n");
        entries.put("genders.0.id", "n");
        entries.put("genders.0.indefinite article", "an");
        entries.put("genders.0.definite article", "the");
        entries.put("genders.plural.definite article", "the");
        return entries;
    }

    private static Map<String, String> updatedEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("tool.hammer", "mallet");
        entries.put("tool.apple_pie", "tart@n");
        entries.put("genders.0.id", "n");
        entries.put("genders.0.indefinite article", "an");
        entries.put("genders.0.definite article", "the");
        entries.put("genders.plural.definite article", "the");
        return entries;
    }
}
