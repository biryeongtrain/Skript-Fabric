package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TimespanCompatibilityTest {

    @AfterEach
    void resetLanguage() {
        Language.clear();
    }

    @Test
    void parsesNaturalShortAndClockForms() {
        Language.loadDefault(languageEntries());

        Timespan natural = Timespan.parse("2 minutes and 5 seconds");
        Timespan command = Timespan.parse("1.5h", ParseContext.COMMAND);
        Timespan clock = Timespan.parse("1:02:03");

        assertNotNull(natural);
        assertNotNull(command);
        assertNotNull(clock);
        assertEquals(125_000L, natural.millis());
        assertEquals(5_400_000L, command.millis());
        assertEquals(3_723_000L, clock.millis());
    }

    @Test
    void supportsInfiniteAndLocalizedForeverFormatting() {
        Language.loadDefault(languageEntries());

        Timespan forever = Timespan.parse("eternity");

        assertNotNull(forever);
        assertTrue(forever.isInfinite());
        assertEquals(Long.MAX_VALUE, forever.millis());
        assertEquals("forever", forever.toString());
        assertEquals("forever", Timespan.infinite().toString());
    }

    @Test
    void exposesArithmeticDurationAndTemporalAmountHelpers() {
        Language.loadDefault(languageEntries());

        Timespan left = new Timespan(Timespan.TimePeriod.MINUTE, 2);
        Timespan right = new Timespan(Timespan.TimePeriod.SECOND, 30);

        assertEquals(150_000L, left.add(right).millis());
        assertEquals(90_000L, left.subtract(right).millis());
        assertEquals(240_000L, left.multiply(2).millis());
        assertEquals(60_000L, left.divide(2).millis());
        assertEquals(4.0, left.divide(new Timespan(Timespan.TimePeriod.SECOND, 30)));
        assertEquals(90_000L, left.difference(new Timespan(Timespan.TimePeriod.SECOND, 30)).millis());
        assertEquals(Duration.ofMinutes(2), left.getDuration());
        assertEquals(Instant.parse("2026-03-10T00:00:00Z").plusMillis(120_000L), left.addTo(Instant.parse("2026-03-10T00:00:00Z")));
    }

    @Test
    void formatsLargestUnitsUsingLanguageBackedNames() {
        Language.loadDefault(languageEntries());

        Timespan timespan = new Timespan(Timespan.TimePeriod.MINUTE, 2).add(new Timespan(Timespan.TimePeriod.SECOND, 5));

        assertEquals("2 minutes and 5 seconds", timespan.toString());
        assertEquals("1 tick", new Timespan(Timespan.TimePeriod.TICK, 1).toString());
    }

    private static Map<String, String> languageEntries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("and", "and");
        entries.put("time.forever", "forever");
        entries.put("time.real", "real");
        entries.put("time.minecraft", "minecraft");
        entries.put("time.millisecond.full", "millisecond¦s@x");
        entries.put("time.millisecond.short", "ms");
        entries.put("time.tick.full", "tick¦s@x");
        entries.put("time.tick.short", "tick¦s@x");
        entries.put("time.second.full", "second¦s@x");
        entries.put("time.second.short", "sec¦s@x");
        entries.put("time.minute.full", "minute¦s@x");
        entries.put("time.minute.short", "min¦s@x");
        entries.put("time.hour.full", "hour¦s@x");
        entries.put("time.hour.short", "hr¦s@x");
        entries.put("time.day.full", "day¦s@x");
        entries.put("time.day.short", "day¦s@x");
        entries.put("time.week.full", "week¦s@x");
        entries.put("time.week.short", "week¦s@x");
        entries.put("time.month.full", "month¦s@x");
        entries.put("time.month.short", "month¦s@x");
        entries.put("time.year.full", "year¦s@x");
        entries.put("time.year.short", "year¦s@x");
        entries.put("genders.0.id", "n");
        entries.put("genders.0.indefinite article", "a");
        entries.put("genders.0.definite article", "the");
        entries.put("genders.plural.definite article", "the");
        return entries;
    }
}
