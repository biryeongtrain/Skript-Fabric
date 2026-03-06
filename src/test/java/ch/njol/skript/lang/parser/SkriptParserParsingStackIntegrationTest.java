package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

class SkriptParserParsingStackIntegrationTest {

    @BeforeEach
    void resetRegistry() {
        Skript.instance().syntaxRegistry().clearAll();
        StackAwareEffect.stackEmptyDuringInit = false;
        StackAwareEffect.initCalls = 0;
    }

    @Test
    void currentSyntaxElementIsNotOnStackDuringInitAndStackIsRestoredAfterParse() {
        Skript.registerEffect(StackAwareEffect.class, "stack aware");
        ParserInstance parser = ParserInstance.get();

        Statement parsed = Statement.parse("stack aware", "failed");

        assertNotNull(parsed);
        assertInstanceOf(StackAwareEffect.class, parsed);
        assertTrue(StackAwareEffect.stackEmptyDuringInit);
        assertEquals(1, StackAwareEffect.initCalls);
        assertTrue(parser.getParsingStack().isEmpty());
    }

    @Test
    void parseModernWrapsStackOverflowWithParsingSnapshot() {
        Skript.registerEffect(OverflowEffect.class, "overflow effect");
        ParserInstance parser = ParserInstance.get();

        ParseStackOverflowException exception = assertThrows(
                ParseStackOverflowException.class,
                () -> Statement.parse("overflow effect", null)
        );

        assertTrue(exception.getMessage().contains("OverflowEffect"));
        assertTrue(parser.getParsingStack().isEmpty());
    }

    @Test
    void parseStaticWrapsLegacyOverflowWithParsingSnapshot() {
        SyntaxElementInfo<LegacyOverflowSyntax> info = new SyntaxElementInfo<>(
                new String[]{"legacy overflow"},
                LegacyOverflowSyntax.class,
                LegacyOverflowSyntax.class.getName()
        );
        ParserInstance parser = ParserInstance.get();

        ParseStackOverflowException exception = assertThrows(
                ParseStackOverflowException.class,
                () -> SkriptParser.parseStatic("legacy overflow", java.util.List.of(info).iterator(), ParseContext.DEFAULT, null)
        );

        assertTrue(exception.getMessage().contains("LegacyOverflowSyntax"));
        assertTrue(parser.getParsingStack().isEmpty());
    }

    public static class StackAwareEffect extends Effect {

        static boolean stackEmptyDuringInit;
        static int initCalls;

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            initCalls++;
            stackEmptyDuringInit = getParser().getParsingStack().isEmpty();
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stack aware";
        }
    }

    public static class OverflowEffect extends Effect {

        public OverflowEffect() {
            throw new StackOverflowError("boom");
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "overflow";
        }
    }

    public static class LegacyOverflowSyntax implements SyntaxElement {

        public LegacyOverflowSyntax() {
            throw new StackOverflowError("legacy boom");
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return true;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "legacy overflow";
        }
    }
}
