package ch.njol.skript.localization;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LocalizationCompatibilityTest {

    @AfterEach
    void resetLanguage() {
        Language.clear();
        Language.loadDefault(defaultEntries());
    }

    @Test
    void messageAndFormattedMessagesTrackLanguageReloads() {
        Language.loadDefault(defaultEntries());
        Message plain = new Message("hello");
        FormattedMessage formatted = new FormattedMessage("welcome", "Alex");

        assertEquals("hello world", plain.toString());
        assertEquals("Welcome Alex", formatted.toString());

        Language.load("pirate", Map.of("hello", "ahoy", "welcome", "Ahoy %s"));

        assertEquals("hello world", plain.toString());
        assertEquals("Welcome Alex", formatted.toString());
    }

    @Test
    void languageListenersRespectPriorityOrderAndImmediateInitReplay() {
        StringBuilder seen = new StringBuilder();
        Language.addListener(() -> seen.append('L'), Language.LanguageListenerPriority.LATEST);
        Language.addListener(() -> seen.append('E'), Language.LanguageListenerPriority.EARLIEST);

        Language.loadDefault(defaultEntries());

        assertTrue(seen.indexOf("E") != -1);
        assertTrue(seen.indexOf("L") != -1);
        assertTrue(seen.indexOf("E") < seen.lastIndexOf("L"));
    }

    @Test
    void regexMessageUsesCurrentLanguageValue() {
        Language.loadDefault(defaultEntries());
        RegexMessage regex = new RegexMessage("pattern");

        assertTrue(regex.matches("stone"));
        assertFalse(regex.find("diamond"));
    }

    @Test
    void nounAndAdjectiveApplyArticlesAndPluralMarkers() {
        Language.loadDefault(defaultEntries());
        Noun noun = new Noun("item");
        Adjective adjective = new Adjective("fancy");

        assertEquals("a shelf", noun.toString(Language.F_INDEFINITE_ARTICLE));
        assertEquals("the shelves", noun.toString(Language.F_DEFINITE_ARTICLE | Language.F_PLURAL));
        assertEquals("the shiny shelf", noun.toString(adjective, Language.F_DEFINITE_ARTICLE));
    }

    @Test
    void pluralizingArgsMessageChoosesPluralSegmentsFromFormattedNumbers() {
        Language.loadDefault(defaultEntries());
        PluralizingArgsMessage message = new PluralizingArgsMessage("count");

        assertEquals("1 shelf", message.toString(1));
        assertEquals("2 shelves", message.toString(2));
    }

    @Test
    void languageListsAndSpacingFollowUpstreamHelpers() {
        Language.loadDefault(defaultEntries());

        assertArrayEquals(new String[]{"red", "green", "blue"}, Language.getList("colors"));
        assertEquals(" and ", Language.getSpaced("and"));
    }

    @Test
    void pluralizationUsesUpstreamNumericParsingHelpers() {
        assertEquals(12.5, StringUtils.numberAfter(" 12.5 shelves", 0));
        assertEquals(12.5, StringUtils.numberBefore("12.5 shelves", 4));
        assertEquals(-1, StringUtils.numberAfter("2nd shelf", 0));
        assertEquals("1 shelf", PluralizingArgsMessage.format("1 shel¦f¦ves¦"));
        assertEquals("2 shelves", PluralizingArgsMessage.format("2 shel¦f¦ves¦"));
        assertEquals("2nd shelf", PluralizingArgsMessage.format("2nd shel¦f¦ves¦"));
    }

    private static Map<String, String> defaultEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("hello", "hello world");
        entries.put("welcome", "Welcome %s");
        entries.put("pattern", "stone");
        entries.put("item", "shel¦f¦ves¦@n");
        entries.put("fancy", "@+:shiny@");
        entries.put("count", "%s shel¦f¦ves¦");
        entries.put("colors", "red, green, blue");
        entries.put("and", "and");
        entries.put("genders.0.id", "n");
        entries.put("genders.0.indefinite article", "a");
        entries.put("genders.0.definite article", "the");
        entries.put("genders.plural.definite article", "the");
        return entries;
    }
}
