package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.TimeZone;
import org.junit.jupiter.api.Test;

class DateCompatibilityTest {

    @Test
    void supportsJavaDateConversionArithmeticAndDifference() {
        Date base = new Date(1_000L);
        Timespan span = new Timespan(Timespan.TimePeriod.SECOND, 2);

        assertEquals(1_000L, Date.fromJavaDate(new java.util.Date(1_000L)).getTime());
        assertEquals(3_000L, base.plus(span).getTime());
        assertEquals(0L, base.minus(new Timespan(Timespan.TimePeriod.SECOND, 1)).getTime());
        assertEquals(2_000L, base.difference(new Date(3_000L)).millis());
    }

    @Test
    void mutatingOperationsAndTimezoneConstructorMatchUpstreamShape() {
        Date shifted = new Date(3_600_000L, TimeZone.getTimeZone("GMT+01:00"));
        Date now = Date.now();
        Date mutable = new Date(10_000L);

        mutable.add(new Timespan(Timespan.TimePeriod.SECOND, 5));
        mutable.subtract(new Timespan(Timespan.TimePeriod.SECOND, 2));

        assertEquals(0L, shifted.getTime());
        assertEquals(13_000L, mutable.getTime());
        assertEquals(13_000L, mutable.getTimestamp());
        assertTrue(now.getTime() > 0L);
        assertNotSame(now, Date.fromJavaDate(new java.util.Date(now.getTime())));
    }
}
