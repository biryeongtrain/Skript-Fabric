package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.util.Kleenean;
import ch.njol.skript.lang.ParseContext;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression for parity with upstream pattern/runtime default handling:
 * If a required placeholder is omitted via an optional bracket and no default expression exists,
 * the pattern must fail to parse (skip this pattern), rather than proceed with a null expression.
 */
class OmittedPlaceholderRequiredDefaultCompatibilityTest {

    @Test
    void requiredOmittedPlaceholderWithoutDefaultFailsPattern() {
        // Pattern has a required string placeholder inside an optional group.
        // Input omits the group entirely, so the placeholder is omitted as well.
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe [%string%]"},
            NullAcceptingEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseModern(
                "probe",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        // Upstream: fails this pattern because %string% is required and no default exists.
        // Prior behavior here accepted and called init with a null expression; assert the upstream result.
        assertNull(parsed, "pattern with required omitted %string% and no default must fail");
    }

    @Test
    void unmatchedChoiceBranchPlaceholderDoesNotRequireDefault() {
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe (text %string%|count %number%)"},
            NullAcceptingEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseModern(
                "probe text \"ok\"",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        assertNotNull(parsed, "unmatched choice-branch placeholder must not force a default");
    }

    public static final class NullAcceptingEffect extends Effect {
        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            // Intentionally accept even if expressions[0] is null to surface parser behavior differences.
            return true;
        }

        @Override
        protected void execute(SkriptEvent event) {
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "null-accepting";
        }
    }
}
