package ch.njol.skript.test.runner;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.structure.Structure;

public class EvtTestCase extends Structure {

    private final String testName;

    public EvtTestCase(String testName) {
        this.testName = testName;
    }

    public String getTestName() {
        return testName;
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult,
                        @Nullable EntryContainer entryContainer) {
        return true;
    }

    @Override
    public boolean load() {
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return testName;
    }
}
