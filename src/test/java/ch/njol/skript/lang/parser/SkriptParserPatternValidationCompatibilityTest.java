package ch.njol.skript.lang.parser;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.NonNullPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkriptParserPatternValidationCompatibilityTest {

    @Test
    void validatePatternNormalizesKnownPluralPlaceholderTypes() {
        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number"));
        Classes.registerClassInfo(new ClassInfo<>(String.class, "text"));

        try {
            NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> validated =
                    SkriptParser.validatePattern("track %numbers% as %text%");

            assertNotNull(validated);
            assertEquals("track %numbers% as %text%", validated.first());
            assertEquals(2, validated.second().length);
            assertEquals("number", validated.second()[0].first().getCodeName());
            assertTrue(validated.second()[0].second());
            assertEquals("text", validated.second()[1].first().getCodeName());
            assertTrue(!validated.second()[1].second());
        } finally {
            Classes.clearClassInfos();
        }
    }

    @Test
    void validatePatternRejectsPipesOutsideGroupsWithUpstreamMessage() {
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            NonNullPair<String, NonNullPair<ClassInfo<?>, Boolean>[]> validated =
                    SkriptParser.validatePattern("track | text");

            assertNull(validated);
            assertTrue(log.hasError());
            assertNotNull(log.getError());
            assertTrue(log.getError().getMessage().contains("Cannot use the pipe character '|' outside of groups"));
        }
    }
}
