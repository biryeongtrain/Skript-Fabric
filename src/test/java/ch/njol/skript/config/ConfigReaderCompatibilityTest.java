package ch.njol.skript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class ConfigReaderCompatibilityTest {

    @Test
    void readLineStripsUtf8BomFromFirstNonEmptyLine() throws IOException {
        byte[] bytes = "\uFEFFfirst\nsecond\n".getBytes(StandardCharsets.UTF_8);

        try (ConfigReader reader = new ConfigReader(new ByteArrayInputStream(bytes))) {
            assertEquals("first", reader.readLine());
            assertEquals(1, reader.getLineNum());
            assertEquals("first", reader.getLine());
            assertEquals("second", reader.readLine());
            assertEquals(2, reader.getLineNum());
        }
    }

    @Test
    void resetReplaysCurrentLineOnce() throws IOException {
        byte[] bytes = "first\nsecond\n".getBytes(StandardCharsets.UTF_8);

        try (ConfigReader reader = new ConfigReader(new ByteArrayInputStream(bytes))) {
            assertEquals("first", reader.readLine());
            reader.reset();
            assertEquals("first", reader.readLine());
            assertEquals(1, reader.getLineNum());
            assertEquals("second", reader.readLine());
            assertEquals(2, reader.getLineNum());
        }
    }

    @Test
    void doubleResetWithoutReadFailsLikeUpstream() throws IOException {
        byte[] bytes = "first\n".getBytes(StandardCharsets.UTF_8);

        try (ConfigReader reader = new ConfigReader(new ByteArrayInputStream(bytes))) {
            reader.readLine();
            reader.reset();

            assertThrows(IllegalStateException.class, reader::reset);
        }
    }
}
