package ch.njol.skript.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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
        assertTrue(warningLiteral.multipleWarning());

        UnparsedLiteral convertedLiteral = new UnparsedLiteral("55");
        Literal<? extends Integer> converted = convertedLiteral.getConvertedExpression(Integer.class);
        assertNotNull(converted);
        assertFalse(convertedLiteral.multipleWarning());
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
        ClassInfo<NumericA> a = new ClassInfo<>(NumericA.class);
        a.setParser(new NumericParserA());
        Classes.registerClassInfo(a);

        ClassInfo<NumericB> b = new ClassInfo<>(NumericB.class);
        b.setParser(new NumericParserB());
        Classes.registerClassInfo(b);
    }

    private record FooType(int value) {
    }

    private record NumericA(int value) {
    }

    private record NumericB(int value) {
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
