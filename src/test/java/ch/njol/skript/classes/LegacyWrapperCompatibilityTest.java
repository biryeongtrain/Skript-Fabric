package ch.njol.skript.classes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.converter.ConverterInfo;

@SuppressWarnings("removal")
class LegacyWrapperCompatibilityTest {

    @AfterEach
    void cleanupClassInfos() {
        Classes.clearClassInfos();
    }

    @Test
    void parserWrapperRegistersThroughClassInfoAndHonorsStringModes() {
        LegacyParser parser = new LegacyParser();
        ClassInfo<LegacyValue> info = new ClassInfo<>(LegacyValue.class, "legacyvalue");
        info.setParser(parser);
        Classes.registerClassInfo(info);

        LegacyValue parsed = Classes.parse("legacy-7", LegacyValue.class, ParseContext.DEFAULT);

        assertEquals(new LegacyValue(7), parsed);
        assertSame(parser, info.getParser());
        assertEquals("legacy 7", parser.toString(parsed, StringMode.MESSAGE));
        assertEquals("debug legacy 7", parser.toString(parsed, StringMode.DEBUG));
        assertEquals("legacy-7", parser.toString(parsed, StringMode.VARIABLE_NAME));
    }

    @Test
    void classesToStringUsesLegacyParserFormatting() {
        LegacyParser parser = new LegacyParser();
        ClassInfo<LegacyValue> info = new ClassInfo<>(LegacyValue.class, "legacyvalue");
        info.setParser(parser);
        Classes.registerClassInfo(info);

        LegacyValue value = new LegacyValue(7);

        assertEquals("legacy 7", Classes.toString(value, StringMode.MESSAGE));
        assertEquals("debug legacy 7", Classes.toString(value, StringMode.DEBUG));
        assertEquals("legacy 7 and legacy 8", Classes.toString(new Object[]{value, new LegacyValue(8)}, true));
    }

    @Test
    void patternedParserExposesCombinedPatternsAndParsesThroughClasses() {
        CompassParser parser = new CompassParser();
        ClassInfo<CompassPoint> info = new ClassInfo<>(CompassPoint.class, "compasspoint");
        info.setParser(parser);
        Classes.registerClassInfo(info);

        CompassPoint parsed = Classes.parse("south", CompassPoint.class, ParseContext.DEFAULT);

        assertEquals(new CompassPoint("south"), parsed);
        assertArrayEquals(new String[]{"north", "south"}, parser.getPatterns());
        assertEquals("north, south", parser.getCombinedPatterns());
    }

    @Test
    void converterWrapperBridgeRegistersRetrievesAndConvertsValues() {
        int before = ch.njol.skript.registrations.Converters.getConverters().size();
        Converter<LegacySource, LegacyTarget> converter = value -> value.value() >= 0
                ? new LegacyTarget("wrapped-" + value.value())
                : null;

        ch.njol.skript.registrations.Converters.registerConverter(
                LegacySource.class,
                LegacyTarget.class,
                converter,
                Converter.NO_LEFT_CHAINING
        );

        assertTrue(ch.njol.skript.registrations.Converters.converterExists(LegacySource.class, LegacyTarget.class));
        assertEquals(
                new LegacyTarget("wrapped-5"),
                ch.njol.skript.registrations.Converters.convert(new LegacySource(5), LegacyTarget.class)
        );
        assertNull(ch.njol.skript.registrations.Converters.convert(new LegacySource(-1), LegacyTarget.class));

        Converter<? super LegacySource, ? extends LegacyTarget> lookedUp =
                ch.njol.skript.registrations.Converters.getConverter(LegacySource.class, LegacyTarget.class);
        assertNotNull(lookedUp);
        assertEquals(new LegacyTarget("wrapped-8"), lookedUp.convert(new LegacySource(8)));

        ConverterInfo<? super LegacySource, ? extends LegacyTarget> info =
                ch.njol.skript.registrations.Converters.getConverterInfo(LegacySource.class, LegacyTarget.class);
        assertNotNull(info);
        assertEquals(LegacySource.class, info.getFrom());
        assertEquals(LegacyTarget.class, info.getTo());
        assertEquals(Converter.NO_LEFT_CHAINING, info.getFlags());

        List<ConverterInfo<?, ?>> converters = ch.njol.skript.registrations.Converters.getConverters();
        assertEquals(before + 1, converters.size());
        assertTrue(converters.stream().anyMatch(candidate ->
                candidate.getFrom() == LegacySource.class
                        && candidate.getTo() == LegacyTarget.class
                        && candidate.getFlags() == Converter.NO_LEFT_CHAINING
        ));

        LegacyTarget[] convertedArray = ch.njol.skript.registrations.Converters.convertArray(
                new Object[]{new LegacySource(1), new LegacySource(2)},
                LegacyTarget.class
        );
        assertArrayEquals(
                new LegacyTarget[]{new LegacyTarget("wrapped-1"), new LegacyTarget("wrapped-2")},
                convertedArray
        );
        assertNull(ch.njol.skript.registrations.Converters.convertArray(null, LegacyTarget.class));
    }

    @Test
    void converterUtilsCreateInstanceCheckedWrappers() {
        Converter<Double, Integer> floor = value -> value == null ? null : value.intValue();

        @SuppressWarnings("unchecked")
        Converter<Object, Integer> fromChecked = (Converter<Object, Integer>)
                Converter.ConverterUtils.createInstanceofConverter(Double.class, floor);
        assertEquals(3, fromChecked.convert(3.9d));
        assertNull(fromChecked.convert("3.9"));

        Converter<Number, Integer> toChecked = Converter.ConverterUtils.createInstanceofConverter(
                value -> value.doubleValue() > 0 ? Integer.valueOf(value.intValue()) : "bad",
                Integer.class
        );
        assertEquals(4, toChecked.convert(4.2d));
        assertNull(toChecked.convert(-1));

        @SuppressWarnings("unchecked")
        Converter<Object, Integer> fullyChecked = (Converter<Object, Integer>) Converter.ConverterUtils
                .createDoubleInstanceofConverter(
                Number.class,
                value -> value.doubleValue() > 0 ? Integer.valueOf(value.intValue()) : "bad",
                Integer.class
        );
        assertEquals(2, fullyChecked.convert(2.8d));
        assertNull(fullyChecked.convert("2.8"));
        assertNull(fullyChecked.convert(-1));
    }

    private record LegacyValue(int value) {
    }

    private record CompassPoint(String name) {
    }

    private record LegacySource(int value) {
    }

    private record LegacyTarget(String value) {
    }

    private static final class LegacyParser extends Parser<LegacyValue> {

        @Override
        public LegacyValue parse(String input, ParseContext context) {
            if (!input.startsWith("legacy-")) {
                return null;
            }
            try {
                return new LegacyValue(Integer.parseInt(input.substring(7)));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        @Override
        public String toString(LegacyValue object, int flags) {
            return "legacy " + object.value();
        }

        @Override
        public String toVariableNameString(LegacyValue object) {
            return "legacy-" + object.value();
        }

        @Override
        public String getDebugMessage(LegacyValue object) {
            return "debug legacy " + object.value();
        }
    }

    private static final class CompassParser extends PatternedParser<CompassPoint> {

        @Override
        public String[] getPatterns() {
            return new String[]{"north", "south"};
        }

        @Override
        public CompassPoint parse(String input, ParseContext context) {
            for (String pattern : getPatterns()) {
                if (pattern.equalsIgnoreCase(input)) {
                    return new CompassPoint(pattern);
                }
            }
            return null;
        }

        @Override
        public String toString(CompassPoint object, int flags) {
            return object.name();
        }

        @Override
        public String toVariableNameString(CompassPoint object) {
            return object.name();
        }
    }
}
