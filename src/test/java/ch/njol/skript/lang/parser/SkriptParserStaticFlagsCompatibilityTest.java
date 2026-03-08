package ch.njol.skript.lang.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.event.SkriptEvent;

import java.util.List;

/**
 * Verifies legacy parse path (parseStatic) honors placeholder flag masks and allows expressions,
 * matching upstream behavior. Previously, parseStatic used PARSE_LITERALS which rejected
 * expression-only placeholders like %~type%.
 */
class SkriptParserStaticFlagsCompatibilityTest {

    @BeforeEach
    void resetRegistry() {
        Skript.instance().syntaxRegistry().clearAll();
    }

    @Test
    void parseStaticAcceptsExpressionOnlyPlaceholder() {
        // Register a simple named integer expression to be used as an expression placeholder value
        Skript.registerExpression(NamedIntegerExpression.class, Integer.class, "named number");

        SyntaxElementInfo<LegacyExpressionOnlyEffect> info = new SyntaxElementInfo<>(
                new String[]{"legacy expression-only %~integer%"},
                LegacyExpressionOnlyEffect.class,
                LegacyExpressionOnlyEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseStatic(
                "legacy expression-only named number",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                "failed"
        );

        assertNotNull(parsed);
        assertInstanceOf(LegacyExpressionOnlyEffect.class, parsed);
    }

    @Test
    void parseStaticStillRejectsLiteralForExpressionOnlyPlaceholder() {
        SyntaxElementInfo<LegacyExpressionOnlyEffect> info = new SyntaxElementInfo<>(
                new String[]{"legacy expression-only %~integer%"},
                LegacyExpressionOnlyEffect.class,
                LegacyExpressionOnlyEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseStatic(
                "legacy expression-only 5",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        assertNull(parsed);
    }

    public static class LegacyExpressionOnlyEffect extends Effect {
        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 1 && expressions[0] != null;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "legacy expression-only";
        }
    }

    public static class NamedIntegerExpression extends SimpleExpression<Integer> {

        @Override
        protected Integer @Nullable [] get(SkriptEvent event) {
            return new Integer[]{7};
        }

        @Override
        public Class<? extends Integer> getReturnType() {
            return Integer.class;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
            return expressions.length == 0;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "named number";
        }
    }
}

