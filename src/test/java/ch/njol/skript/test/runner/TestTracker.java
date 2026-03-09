package ch.njol.skript.test.runner;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

public final class TestTracker {

    public record Failure(String message, @Nullable Script script, int line) {
    }

    private static final List<Failure> FAILURES = new ArrayList<>();
    private static @Nullable String lastParsingStarted;

    private TestTracker() {
    }

    public static void reset() {
        FAILURES.clear();
        lastParsingStarted = null;
    }

    public static void parsingStarted(@Nullable String name) {
        lastParsingStarted = name;
    }

    public static void testFailed(String message) {
        FAILURES.add(new Failure(message, null, -1));
    }

    public static void testFailed(String message, @Nullable Script script, int line) {
        FAILURES.add(new Failure(message, script, line));
    }

    public static @Nullable String lastParsingStarted() {
        return lastParsingStarted;
    }

    public static List<Failure> failures() {
        return List.copyOf(FAILURES);
    }
}
