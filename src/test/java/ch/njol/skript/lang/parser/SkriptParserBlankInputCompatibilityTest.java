package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.util.Kleenean;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;

class SkriptParserBlankInputCompatibilityTest {

    @BeforeEach
    void resetRegistry() {
        Skript.instance().syntaxRegistry().clearAll();
    }

    @Test
    void parseModernRejectsBlankInputEvenWhenPatternIsFullyOptional() {
        SyntaxInfo<BlankAcceptingEffect> info = new SyntaxInfo<>(
                BlankAcceptingEffect.class,
                new String[]{"[optional]"},
                BlankAcceptingEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseModern(
                "   ",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        assertNull(parsed);
    }

    @Test
    void parseStaticRejectsBlankInputEvenWhenPatternIsFullyOptional() {
        SyntaxElementInfo<BlankAcceptingEffect> info = new SyntaxElementInfo<>(
                new String[]{"[optional]"},
                BlankAcceptingEffect.class,
                BlankAcceptingEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseStatic(
                "   ",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        assertNull(parsed);
    }

    public static final class BlankAcceptingEffect extends Effect {
        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "blank-accepting";
        }
    }
}
