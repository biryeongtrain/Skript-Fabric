package ch.njol.skript.aliases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AliasesCompatibilityTest {

    @Test
    void matchQualityOrdersBetterMatchesFirst() {
        assertTrue(MatchQuality.EXACT.isBetter(MatchQuality.SAME_ITEM));
        assertTrue(MatchQuality.SAME_ITEM.isBetter(MatchQuality.SAME_MATERIAL));
        assertTrue(MatchQuality.SAME_MATERIAL.isBetter(MatchQuality.DIFFERENT));
        assertFalse(MatchQuality.DIFFERENT.isBetter(MatchQuality.EXACT));
    }

    @Test
    void matchQualityAtLeastUsesOrdinalOrdering() {
        assertTrue(MatchQuality.EXACT.isAtLeast(MatchQuality.EXACT));
        assertTrue(MatchQuality.SAME_ITEM.isAtLeast(MatchQuality.SAME_MATERIAL));
        assertFalse(MatchQuality.DIFFERENT.isAtLeast(MatchQuality.SAME_MATERIAL));
    }

    @Test
    void invalidMinecraftIdExceptionRetainsOriginalId() {
        InvalidMinecraftIdException exception = new InvalidMinecraftIdException("minecraft:not valid");

        assertEquals("minecraft:not valid", exception.getId());
    }
}
