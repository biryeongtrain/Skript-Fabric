package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.event.SkriptEvent;

class UnparsedLiteralCompatibilityTest {

    @AfterEach
    void cleanupClassInfos() {
        Classes.clearClassInfos();
    }

    @Test
    void conversionUsesRegisteredClassInfoParsers() {
        registerFooParser();

        UnparsedLiteral literal = new UnparsedLiteral("foo-12");
        Literal<? extends FooType> converted = literal.getConvertedExpression(FooType.class);

        assertNotNull(converted);
        assertEquals(12, converted.getSingle(SkriptEvent.EMPTY).value());
        assertTrue(literal.wasConverted());
    }

    @Test
    void reparseMarksStateWhenSuccessful() {
        registerFooParser();

        UnparsedLiteral literal = new UnparsedLiteral("foo-44");
        SimpleLiteral<FooType> reparsed = literal.reparse(FooType.class);

        assertNotNull(reparsed);
        assertEquals(44, reparsed.getSingle(SkriptEvent.EMPTY).value());
        assertTrue(literal.wasReparsed());
    }

    @Test
    void valueAccessFailsBeforeConversion() {
        UnparsedLiteral literal = new UnparsedLiteral("10");

        assertThrows(SkriptAPIException.class, () -> literal.getSingle(SkriptEvent.EMPTY));
        assertThrows(SkriptAPIException.class, () -> literal.getArray(SkriptEvent.EMPTY));
        assertThrows(SkriptAPIException.class, () -> literal.getAll(SkriptEvent.EMPTY));
    }

    @Test
    void multipleWarningOnlyTriggersWhenStillAmbiguous() {
        registerNumericParsers();

        UnparsedLiteral warningLiteral = new UnparsedLiteral("55");
        assertEquals("numerica and numericb", Classes.toString(warningLiteral.getPossibleInfos().toArray(), true));
        assertTrue(warningLiteral.multipleWarning());

        UnparsedLiteral convertedLiteral = new UnparsedLiteral("55");
        Literal<? extends Integer> converted = convertedLiteral.getConvertedExpression(Integer.class);
        assertNotNull(converted);
        assertFalse(convertedLiteral.multipleWarning());
    }

    @Test
    void quotedStringsStayStringsInObjectContext() {
        registerQuotedFooParser();

        Expression<?> parsed = new SkriptParser("\"foo-12\"", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{Object.class});

        assertNotNull(parsed);
        assertInstanceOf(LiteralString.class, parsed);
        assertEquals("foo-12", parsed.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void quotedStringsDoNotParseAsCustomTypes() {
        registerQuotedFooParser();

        Expression<?> parsed = new SkriptParser("\"foo-12\"", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{FooType.class});

        assertNull(parsed);
    }

    @Test
    void conversionUsesRegisteredConvertersAfterParsingAnotherType() {
        ClassInfo<BridgeSource> info = new ClassInfo<>(BridgeSource.class, "bridgesource");
        info.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public BridgeSource parse(String input, ParseContext context) {
                if (!input.startsWith("bridge-")) {
                    return null;
                }
                try {
                    return new BridgeSource(Integer.parseInt(input.substring(7)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(info);
        Converters.registerConverter(BridgeSource.class, BridgeTarget.class, value -> new BridgeTarget(value.value()));

        UnparsedLiteral literal = new UnparsedLiteral("bridge-9");
        Literal<? extends BridgeTarget> converted = literal.getConvertedExpression(BridgeTarget.class);

        assertNotNull(converted);
        assertEquals(new BridgeTarget(9), converted.getSingle(SkriptEvent.EMPTY));
        assertTrue(literal.wasConverted());
    }

    private static void registerFooParser() {
        ClassInfo<FooType> info = new ClassInfo<>(FooType.class);
        info.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public FooType parse(String input, ParseContext context) {
                if (!input.startsWith("foo-")) {
                    return null;
                }
                try {
                    return new FooType(Integer.parseInt(input.substring(4)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(info);
    }

    private static void registerNumericParsers() {
        ClassInfo<NumericA> a = new ClassInfo<>(NumericA.class, "numerica");
        a.setParser(new NumericParserA());
        Classes.registerClassInfo(a);

        ClassInfo<NumericB> b = new ClassInfo<>(NumericB.class, "numericb");
        b.setParser(new NumericParserB());
        Classes.registerClassInfo(b);
    }

    private static void registerQuotedFooParser() {
        ClassInfo<FooType> info = new ClassInfo<>(FooType.class, "quotedfoo");
        info.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public FooType parse(String input, ParseContext context) {
                String normalized = input == null ? "" : input.trim();
                if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() >= 2) {
                    normalized = normalized.substring(1, normalized.length() - 1);
                }
                if (!normalized.startsWith("foo-")) {
                    return null;
                }
                try {
                    return new FooType(Integer.parseInt(normalized.substring(4)));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(info);
    }

    private record FooType(int value) {
    }

    private record NumericA(int value) {
    }

    private record NumericB(int value) {
    }

    private record BridgeSource(int value) {
    }

    private record BridgeTarget(int value) {
    }

    private static class NumericParserA implements ClassInfo.Parser<NumericA> {
        @Override
        public boolean canParse(ParseContext context) {
            return true;
        }

        @Override
        public NumericA parse(String input, ParseContext context) {
            try {
                return new NumericA(Integer.parseInt(input));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    private static class NumericParserB implements ClassInfo.Parser<NumericB> {
        @Override
        public boolean canParse(ParseContext context) {
            return true;
        }

        @Override
        public NumericB parse(String input, ParseContext context) {
            try {
                return new NumericB(Integer.parseInt(input));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
