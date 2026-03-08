package ch.njol.skript.registrations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClassesCompatibilityTest {

    @AfterEach
    void cleanupClassInfos() {
        Classes.clearClassInfos();
    }

    @Test
    void registrationIndexesCodeNamesAndLiteralPatterns() {
        ClassInfo<FooType> info = new ClassInfo<>(FooType.class, "foo");
        info.literalPatterns("foo", "foo alias");

        Classes.registerClassInfo(info);

        assertSame(info, Classes.getClassInfo("foo"));
        assertSame(info, Classes.getClassInfoNoError("foo"));
        assertEquals(List.of(info), Classes.getClassInfos());
        assertEquals(List.of(info), Classes.getPatternInfos("FOO"));
        assertEquals(List.of(info), Classes.getPatternInfos("  foo alias  "));
    }

    @Test
    void explicitLiteralPatternsTakePrecedenceOverParserFallback() {
        ClassInfo<ExplicitType> explicit = new ClassInfo<>(ExplicitType.class, "explicit");
        explicit.literalPatterns("shared");
        Classes.registerClassInfo(explicit);

        ClassInfo<ParserType> parser = new ClassInfo<>(ParserType.class, "parser");
        parser.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public ParserType parse(String input, ParseContext context) {
                return "shared".equalsIgnoreCase(input.trim()) || "parser-only".equalsIgnoreCase(input.trim())
                        ? new ParserType()
                        : null;
            }
        });
        Classes.registerClassInfo(parser);

        assertEquals(List.of(explicit), Classes.getPatternInfos("shared"));
        assertEquals(List.of(parser), Classes.getPatternInfos("parser-only"));
    }

    @Test
    void duplicateCodeNamesAreRejected() {
        Classes.registerClassInfo(new ClassInfo<>(FooType.class, "duplicate"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Classes.registerClassInfo(new ClassInfo<>(BarType.class, "duplicate"))
        );

        assertEquals("Code name 'duplicate' is already used by " + FooType.class.getName(), exception.getMessage());
    }

    @Test
    void getClassInfoThrowsForUnknownCodeName() {
        assertThrows(SkriptAPIException.class, () -> Classes.getClassInfo("missing"));
    }

    @Test
    void classInfoProvidesDerivedAndExplicitCodeNames() {
        assertEquals("explicitname", new ClassInfo<>(FooType.class, "explicitname").getCodeName());
        assertEquals("autoderivedtype", new ClassInfo<>(AutoDerivedType.class).getCodeName());
    }

    @Test
    void userInputLookupNormalizesWhitespaceAndPlurality() {
        ClassInfo<FooBarType> info = new ClassInfo<>(FooBarType.class, "foobar");
        Classes.registerClassInfo(info);

        assertSame(info, Classes.getClassInfoFromUserInput("foo bar"));
        assertSame(info, Classes.getClassInfoFromUserInput("foobars"));
        assertSame(info, Classes.getClassInfoFromUserInput("foo-bars"));
        assertTrue(Classes.isPluralClassInfoUserInput("foo bars", info));
    }

    private static final class FooType {
    }

    private static final class BarType {
    }

    private static final class ExplicitType {
    }

    private static final class ParserType {
    }

    private static final class AutoDerivedType {
    }

    private static final class FooBarType {
    }
}
