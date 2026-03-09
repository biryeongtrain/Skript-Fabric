package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VersionCompatibilityTest {

    @Test
    void parseIntSaturatesOnOverflow() {
        assertEquals(Integer.MAX_VALUE, Utils.parseInt("999999999999"));
        assertEquals(Integer.MIN_VALUE, Utils.parseInt("-999999999999"));
    }

    @Test
    void stableReleaseBeatsPreReleaseButNotNightly() {
        Version stable = new Version("2.10");

        assertTrue(stable.compareTo(new Version("2.10-beta1")) > 0);
        assertTrue(stable.compareTo(new Version("2.10-nightly")) < 0);
    }

    @Test
    void prereleaseNumbersCompareWithinSamePrefix() {
        assertTrue(new Version("2.10-beta2").compareTo(new Version("2.10-beta1")) > 0);
        assertEquals(0, Version.compare("2.10-alpha1", "2.10-alpha1"));
    }
}
