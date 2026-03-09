package ch.njol.skript.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.njol.skript.localization.Language;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ExceptionUtilsCompatibilityTest {

    @AfterEach
    void resetLanguage() {
        Language.clear();
    }

    @Test
    void ioExceptionsUseLocalizedLanguageEntriesWhenAvailable() {
        Language.loadDefault(Map.of("io exceptions.IOException", "IO failed: %s"));

        assertEquals("IO failed: boom", ExceptionUtils.toString(new IOException("boom")));
    }
}
