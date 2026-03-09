package ch.njol.skript.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.jetbrains.annotations.Nullable;

public class ConfigReader extends BufferedReader {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private @Nullable String line;
    private boolean reset;
    private int lineNumber;
    private boolean hasNonEmptyLine;

    public ConfigReader(InputStream source) {
        super(new InputStreamReader(source, UTF_8));
    }

    @Override
    public @Nullable String readLine() throws IOException {
        if (reset) {
            reset = false;
        } else {
            line = stripUtf8Bom(super.readLine());
            lineNumber++;
        }
        return line;
    }

    private @Nullable String stripUtf8Bom(@Nullable String line) {
        if (!hasNonEmptyLine && line != null && !line.isEmpty()) {
            hasNonEmptyLine = true;
            if (line.startsWith("\uFEFF")) {
                return line.substring(1);
            }
        }
        return line;
    }

    @Override
    public void reset() {
        if (reset) {
            throw new IllegalStateException("reset was called twice without a readLine inbetween");
        }
        reset = true;
    }

    public int getLineNum() {
        return lineNumber;
    }

    public @Nullable String getLine() {
        return line;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        throw new UnsupportedOperationException();
    }
}
