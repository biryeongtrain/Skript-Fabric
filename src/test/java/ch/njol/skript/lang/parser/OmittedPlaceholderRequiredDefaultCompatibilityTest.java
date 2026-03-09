package ch.njol.skript.lang.parser;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.util.Kleenean;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.lang.ParseContext;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
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

    @Test
    void chosenPlaceholderFreeChoiceBranchDoesNotRequireSiblingDefaults() {
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe (nothing|text %string%)"},
            NullAcceptingEffect.class.getName()
        );

        Effect parsed = SkriptParser.parseModern(
                "probe nothing",
                List.of(info).iterator(),
                ParseContext.DEFAULT,
                null
        );

        assertNotNull(parsed, "matching a placeholder-free choice branch must not require defaults from sibling branches");
    }

    @Test
    void requiredOmittedPlaceholderRetainsMissingAndInvalidDefaultDiagnostics() {
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe [%~number/text%]"},
            NullAcceptingEffect.class.getName()
        );

        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number"));
        Classes.registerClassInfo(new ClassInfo<>(String.class, "text"));

        DefaultValueData data = ParserInstance.get().getData(DefaultValueData.class);
        data.addDefaultValue(Integer.class, new SimpleLiteral<>(7, true));
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            Effect parsed = SkriptParser.parseModern(
                    "probe",
                    List.of(info).iterator(),
                    ParseContext.DEFAULT,
                    null
            );

            assertNull(parsed);
            assertTrue(log.hasError());
            assertNotNull(log.getError());
            assertTrue(log.getError().getMessage().contains("default expression of 'number' is a literal"));
            assertTrue(log.getError().getMessage().contains("class 'text' does not provide a default expression"));
        } finally {
            data.removeDefaultValue(Integer.class);
            Classes.clearClassInfos();
        }
    }

    @Test
    void omittedOptionalChoiceRequiresDefaultsForAllRequiredBranches() {
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe [text %string%|count %number%]"},
            NullAcceptingEffect.class.getName()
        );

        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number"));
        Classes.registerClassInfo(new ClassInfo<>(String.class, "string"));

        DefaultValueData data = ParserInstance.get().getData(DefaultValueData.class);
        data.addDefaultValue(String.class, new SimpleLiteral<>("fallback", true));
        try {
            Effect parsed = SkriptParser.parseModern(
                    "probe",
                    List.of(info).iterator(),
                    ParseContext.DEFAULT,
                    null
            );

            assertNull(parsed, "omitted optional alternation should still require defaults for each required placeholder");
        } finally {
            data.removeDefaultValue(String.class);
            Classes.clearClassInfos();
        }
    }

    @Test
    void omittedOptionalChoiceStillRequiresAllBranchDefaultsAfterEarlierMatch() {
        SyntaxInfo<NullAcceptingEffect> info = new SyntaxInfo<>(
            NullAcceptingEffect.class,
            new String[]{"probe %number% [text %string%|count %number%]"},
            NullAcceptingEffect.class.getName()
        );

        Classes.clearClassInfos();
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number"));
        Classes.registerClassInfo(new ClassInfo<>(String.class, "string"));

        DefaultValueData data = ParserInstance.get().getData(DefaultValueData.class);
        data.addDefaultValue(String.class, new SimpleLiteral<>("fallback", true));
        try {
            Effect parsed = SkriptParser.parseModern(
                    "probe 1",
                    List.of(info).iterator(),
                    ParseContext.DEFAULT,
                    null
            );

            assertNull(
                    parsed,
                    "omitted optional alternation after an earlier match should still require defaults for each branch placeholder"
            );
        } finally {
            data.removeDefaultValue(String.class);
            Classes.clearClassInfos();
        }
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
