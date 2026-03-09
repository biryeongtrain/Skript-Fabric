package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.localization.Language;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TimeCompatibilityTest {

    @AfterEach
    void resetLanguage() {
        Language.clear();
    }

    @Test
    void parsesAndFormatsTwelveAndTwentyFourHourForms() {
        Language.loadDefault(languageEntries());

        Time dawn = Time.parse("6:00");
        Time midnight = Time.parse("12:00 am");
        Time afternoon = Time.parse("1:05 pm");

        assertNotNull(dawn);
        assertNotNull(midnight);
        assertNotNull(afternoon);
        assertEquals(0, dawn.getTicks());
        assertEquals("0:00", midnight.toString());
        assertEquals("13:05", afternoon.toString());
        assertEquals(13, afternoon.getHour());
        assertEquals(5, afternoon.getMinute());
    }

    @Test
    void rejectsOutOfRangeHoursAndMinutes() {
        Language.loadDefault(languageEntries());

        assertNull(Time.parse("25:00"));
        assertNull(Time.parse("13:65"));
        assertNull(Time.parse("13:00 am"));
    }

    @Test
    void timeperiodWrapsAcrossMidnight() {
        Timeperiod overnight = new Timeperiod(23000, 1000);

        assertTrue(overnight.contains(23500));
        assertTrue(overnight.contains(new Time(500)));
        assertFalse(overnight.contains(12000));
        assertEquals("5:00-7:00", overnight.toString());
    }

    @Test
    void experienceAndGameruleValueExposeCompatibilityAccessors() {
        Experience any = new Experience();
        Experience fixed = new Experience(7);
        GameruleValue<Boolean> gamerule = new GameruleValue<>(Boolean.TRUE);

        assertEquals(1, any.getXP());
        assertEquals(-1, any.getInternalXP());
        assertEquals("xp", any.toString());
        assertEquals("7 xp", fixed.toString());
        assertEquals(new Experience(7), fixed);
        assertEquals(Boolean.TRUE, gamerule.getGameruleValue());
        assertEquals("true", gamerule.toString());
        assertEquals(new GameruleValue<>(Boolean.TRUE), gamerule);
    }

    private static Map<String, String> languageEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("time.errors.24 hours", "24h");
        entries.put("time.errors.12 hours", "12h");
        entries.put("time.errors.60 minutes", "60m");
        return entries;
    }
}
