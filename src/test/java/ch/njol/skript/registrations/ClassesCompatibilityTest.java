package ch.njol.skript.registrations;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.lang.util.SimpleLiteral;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.converter.Converters;

class ClassesCompatibilityTest {

    private static final int CONVERTER_NO_COMMAND_ARGUMENTS = 8;

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
        // Upstream getPatternInfos only matches explicit literal patterns; parser-only input yields no matches.
        assertNull(Classes.getPatternInfos("parser-only"));
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
    void classInfoLookupIsCaseSensitive() {
        ClassInfo<FooType> info = new ClassInfo<>(FooType.class, "foo");
        Classes.registerClassInfo(info);

        assertSame(info, Classes.getClassInfo("foo"));
        assertNull(Classes.getClassInfoNoError("FoO"));
        assertThrows(SkriptAPIException.class, () -> Classes.getClassInfo("FoO"));
    }

    @Test
    void classInfoProvidesDerivedAndExplicitCodeNames() {
        assertEquals("explicitname", new ClassInfo<>(FooType.class, "explicitname").getCodeName());
        assertEquals("autoderivedtype", new ClassInfo<>(AutoDerivedType.class).getCodeName());
    }

    @Test
    void defaultExpressionLookupUsesRegisteredClassInfos() {
        SimpleLiteral<Integer> defaultNumber = new SimpleLiteral<>(11, true);
        Classes.registerClassInfo(new ClassInfo<>(Integer.class, "number").defaultExpression(defaultNumber));

        assertSame(defaultNumber, Classes.getDefaultExpression("number"));
        assertSame(defaultNumber, Classes.getDefaultExpression(Integer.class));
        assertNull(Classes.getDefaultExpression(Long.class));
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

    @Test
    void userInputLookupSupportsRegisteredRegexAliases() {
        ClassInfo<FooBarType> info = new ClassInfo<>(FooBarType.class, "foobar").user("materials?");
        Classes.registerClassInfo(info);

        assertSame(info, Classes.getClassInfoFromUserInput("material"));
        assertSame(info, Classes.getClassInfoFromUserInput("materials"));
        assertTrue(Classes.isPluralClassInfoUserInput("materials", info));
    }

    @Test
    void superClassLookupPrefersMostSpecificRegisteredAssignableType() {
        ClassInfo<ParentType> parent = new ClassInfo<>(ParentType.class, "parent");
        ClassInfo<ChildType> child = new ClassInfo<>(ChildType.class, "child");
        Classes.registerClassInfo(parent);
        Classes.registerClassInfo(child);

        assertSame(child, Classes.getSuperClassInfo(GrandChildType.class));
        assertEquals(List.of(child, parent), Classes.getClassInfos());
    }

    @Test
    void classInfoOrderHonorsBeforeAndAfterDependencies() {
        ClassInfo<BetaType> beta = new ClassInfo<>(BetaType.class, "beta").after("gamma");
        ClassInfo<AlphaType> alpha = new ClassInfo<>(AlphaType.class, "alpha").before("beta");
        ClassInfo<GammaType> gamma = new ClassInfo<>(GammaType.class, "gamma");

        Classes.registerClassInfo(beta);
        Classes.registerClassInfo(alpha);
        Classes.registerClassInfo(gamma);

        assertEquals(List.of(alpha, gamma, beta), Classes.getClassInfos());
    }

    @Test
    void explicitLiteralPatternMatchesUseRegistrationOrderForLiterals() {
        ClassInfo<BetaType> beta = new ClassInfo<>(BetaType.class, "beta").after("gamma");
        beta.literalPatterns("shared");
        ClassInfo<AlphaType> alpha = new ClassInfo<>(AlphaType.class, "alpha").before("beta");
        alpha.literalPatterns("shared");
        ClassInfo<GammaType> gamma = new ClassInfo<>(GammaType.class, "gamma");
        gamma.literalPatterns("shared");

        Classes.registerClassInfo(beta);
        Classes.registerClassInfo(alpha);
        Classes.registerClassInfo(gamma);

        // Upstream getPatternInfos keeps registration order for literal-pattern matches.
        assertEquals(List.of(beta, alpha, gamma), Classes.getPatternInfos("shared"));
    }

    @Test
    void emptyArrayToStringUsesNullSentinelLikeUpstream() {
        assertEquals("null", Classes.toString(new Object[0], true));
    }

    @Test
    void variableNameStringFallbackUsesObjectPrefixLikeUpstream() {
        assertEquals("object:fallback", Classes.toString("fallback", ch.njol.skript.util.StringMode.VARIABLE_NAME));
    }

    @Test
    void objectTypedArraysUseBracketedElementStringificationLikeUpstream() {
        Object value = new Object[]{"alpha", "beta"};

        assertEquals("[alpha, beta]", Classes.toString(value, ch.njol.skript.util.StringMode.MESSAGE));
    }

    @Test
    void parseFallsBackThroughRegisteredConverters() {
        ClassInfo<ParsedType> parsed = new ClassInfo<>(ParsedType.class, "parsed");
        parsed.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public ParsedType parse(String input, ParseContext context) {
                if (!input.startsWith("parsed-")) {
                    return null;
                }
                try {
                    return new ParsedType(Integer.parseInt(input.substring(7)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(parsed);
        Converters.registerConverter(ParsedType.class, ConvertedType.class, value -> new ConvertedType(value.value()));

        ConvertedType parsedValue = Classes.parse("parsed-42", ConvertedType.class, ParseContext.DEFAULT);

        assertEquals(new ConvertedType(42), parsedValue);
    }

    @Test
    void parseSkipsNoCommandArgumentConvertersInCommandAndParseContexts() {
        ClassInfo<FlaggedParsedType> parsed = new ClassInfo<>(FlaggedParsedType.class, "flaggedparsed");
        parsed.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public FlaggedParsedType parse(String input, ParseContext context) {
                if (!input.startsWith("flagged-")) {
                    return null;
                }
                try {
                    return new FlaggedParsedType(Integer.parseInt(input.substring(8)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(parsed);
        Converters.registerConverter(
                FlaggedParsedType.class,
                FlaggedConvertedType.class,
                value -> new FlaggedConvertedType(value.value()),
                CONVERTER_NO_COMMAND_ARGUMENTS
        );

        assertEquals(new FlaggedConvertedType(9), Classes.parse("flagged-9", FlaggedConvertedType.class, ParseContext.DEFAULT));
        assertNull(Classes.parse("flagged-9", FlaggedConvertedType.class, ParseContext.COMMAND));
        assertNull(Classes.parse("flagged-9", FlaggedConvertedType.class, ParseContext.PARSE));
    }

    @Test
    void getParserFallsBackThroughRegisteredConverters() {
        ClassInfo<ParserSourceType> parsed = new ClassInfo<>(ParserSourceType.class, "parsersource");
        parsed.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public ParserSourceType parse(String input, ParseContext context) {
                if (!input.startsWith("parsed-")) {
                    return null;
                }
                try {
                    return new ParserSourceType(Integer.parseInt(input.substring(7)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(parsed);
        Converters.registerConverter(
                ParserSourceType.class,
                ParserTargetType.class,
                value -> new ParserTargetType(value.value())
        );

        ClassInfo.Parser<? extends ParserTargetType> parser = Classes.getParser(ParserTargetType.class);

        assertNotNull(parser);
        assertTrue(parser.canParse(ParseContext.DEFAULT));
        assertEquals(new ParserTargetType(73), parser.parse("parsed-73", ParseContext.DEFAULT));
        assertNull(parser.parse("invalid", ParseContext.DEFAULT));
    }

    @Test
    void parseSimpleClearsFailedParserErrorsBeforeLaterSuccess() {
        ClassInfo<FallbackSourceType> first = new ClassInfo<>(FallbackSourceType.class, "fallbacksource");
        first.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public FallbackSourceType parse(String input, ParseContext context) {
                Skript.error("first parser should not leak");
                return null;
            }
        });
        Classes.registerClassInfo(first);

        ClassInfo<FallbackTargetType> second = new ClassInfo<>(FallbackTargetType.class, "fallbacktarget");
        second.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public FallbackTargetType parse(String input, ParseContext context) {
                if (!input.startsWith("fallback-")) {
                    return null;
                }
                try {
                    return new FallbackTargetType(Integer.parseInt(input.substring(9)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(second);

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            FallbackBaseType parsed = Classes.parseSimple("fallback-14", FallbackBaseType.class, ParseContext.DEFAULT);

            assertEquals(new FallbackTargetType(14), parsed);
            assertNull(log.getError());
            assertTrue(log.getErrors().isEmpty());
        }
    }

    @Test
    void parseSimplePrefersMostSpecificRegisteredParserOverExactBaseType() {
        ClassInfo<SpecificBaseType> base = new ClassInfo<>(SpecificBaseType.class, "specificbase");
        base.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public SpecificBaseType parse(String input, ParseContext context) {
                return "shared".equals(input) ? new SpecificBaseType("base") : null;
            }
        });
        Classes.registerClassInfo(base);

        ClassInfo<SpecificChildType> child = new ClassInfo<>(SpecificChildType.class, "specificchild");
        child.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public SpecificChildType parse(String input, ParseContext context) {
                return "shared".equals(input) ? new SpecificChildType("child") : null;
            }
        });
        Classes.registerClassInfo(child);

        assertEquals(new SpecificChildType("child"), Classes.parseSimple("shared", SpecificBaseType.class, ParseContext.DEFAULT));
    }

    @Test
    void parseSimplePrefersRegisteredParserOverPrimitiveFallback() {
        ClassInfo<String> text = new ClassInfo<>(String.class, "text");
        text.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public String parse(String input, ParseContext context) {
                return input.startsWith("wrapped-") ? "<" + input.substring(8) + ">" : null;
            }
        });
        Classes.registerClassInfo(text);

        assertEquals("<value>", Classes.parseSimple("wrapped-value", String.class, ParseContext.DEFAULT));
    }

    @Test
    void parseClearsFailedDirectErrorsBeforeConverterSuccess() {
        ClassInfo<ConverterFallbackTarget> direct = new ClassInfo<>(ConverterFallbackTarget.class, "converterfallbacktarget");
        direct.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public ConverterFallbackTarget parse(String input, ParseContext context) {
                Skript.error("direct parser should not leak");
                return null;
            }
        });
        Classes.registerClassInfo(direct);

        ClassInfo<ConverterFallbackSource> source = new ClassInfo<>(ConverterFallbackSource.class, "converterfallbacksource");
        source.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public ConverterFallbackSource parse(String input, ParseContext context) {
                if (!input.startsWith("convert-")) {
                    return null;
                }
                try {
                    return new ConverterFallbackSource(Integer.parseInt(input.substring(8)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(source);
        Converters.registerConverter(
                ConverterFallbackSource.class,
                ConverterFallbackTarget.class,
                value -> new ConverterFallbackTarget(value.value())
        );

        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            ConverterFallbackTarget parsed = Classes.parse("convert-28", ConverterFallbackTarget.class, ParseContext.DEFAULT);

            assertEquals(new ConverterFallbackTarget(28), parsed);
            assertNull(log.getError());
            assertTrue(log.getErrors().isEmpty());
        }
    }

    @Test
    void cloneRecursivelyCopiesNestedArrays() {
        int[] inner = {1, 2};
        Object[] original = {inner};

        Object[] clone = Classes.clone(original);

        assertNotSame(original, clone);
        assertNotSame(original[0], clone[0]);

        int[] clonedInner = (int[]) clone[0];
        clonedInner[0] = 99;

        assertArrayEquals(new int[]{1, 2}, inner);
        assertArrayEquals(new int[]{99, 2}, clonedInner);
    }

    @Test
    void cloneUsesRegisteredClassInfoClonerBeforeReturningOriginalValue() {
        ClassInfo<CloneTrackedType> info = new ClassInfo<>(CloneTrackedType.class, "clonetracked")
                .cloner(value -> new CloneTrackedType(value.value() + 1));
        Classes.registerClassInfo(info);

        CloneTrackedType original = new CloneTrackedType(5);

        CloneTrackedType clone = Classes.clone(original);

        assertNotSame(original, clone);
        assertEquals(new CloneTrackedType(6), clone);
        assertEquals(new CloneTrackedType(5), original);
    }

    @Test
    void cloneDoesNotReflectivelyCloneCloneableValuesWithoutClassInfoCloner() {
        CloneableType original = new CloneableType(5);
        Classes.registerClassInfo(new ClassInfo<>(CloneableType.class, "cloneabletype"));

        CloneableType clone = Classes.clone(original);

        assertSame(original, clone);
        assertEquals(0, original.cloneCalls);
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

    private static class ParentType {
    }

    private static class ChildType extends ParentType {
    }

    private static final class GrandChildType extends ChildType {
    }

    private static final class AlphaType {
    }

    private static final class BetaType {
    }

    private static final class GammaType {
    }

    private record CloneTrackedType(int value) {
    }

    private static final class CloneableType implements Cloneable {

        private final int value;
        private int cloneCalls;

        private CloneableType(int value) {
            this.value = value;
        }

        @Override
        protected CloneableType clone() {
            cloneCalls++;
            return new CloneableType(value + 1);
        }
    }

    private record ParsedType(int value) {
    }

    private record ConvertedType(int value) {
    }

    private record FlaggedParsedType(int value) {
    }

    private record FlaggedConvertedType(int value) {
    }

    private record ParserSourceType(int value) {
    }

    private record ParserTargetType(int value) {
    }

    private sealed interface FallbackBaseType permits FallbackSourceType, FallbackTargetType {
    }

    private record FallbackSourceType(int value) implements FallbackBaseType {
    }

    private record FallbackTargetType(int value) implements FallbackBaseType {
    }

    private record ConverterFallbackSource(int value) {
    }

    private record ConverterFallbackTarget(int value) {
    }

    private static class SpecificBaseType {

        private final String value;

        private SpecificBaseType(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof SpecificBaseType that && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    private static final class SpecificChildType extends SpecificBaseType {

        private SpecificChildType(String value) {
            super(value);
        }
    }
}
