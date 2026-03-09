package ch.njol.skript.classes.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.comparator.Relation;

class JavaClassesCompatibilityTest {

    @AfterEach
    void cleanup() {
        Classes.clearClassInfos();
        Language.clear();
    }

    @Test
    void registerAddsPureJavaClassInfosAndParsers() {
        Language.loadDefault(Map.of(
                "boolean.true.pattern", "true|yes|on",
                "boolean.false.pattern", "false|no|off",
                "boolean.true.name", "true",
                "boolean.false.name", "false"
        ));

        JavaClasses.register();
        JavaClasses.register();

        assertNotNull(Classes.getExactClassInfo(Object.class));
        assertNotNull(Classes.getExactClassInfo(Number.class));
        assertNotNull(Classes.getExactClassInfo(Long.class));
        assertNotNull(Classes.getExactClassInfo(Integer.class));
        assertNotNull(Classes.getExactClassInfo(Double.class));
        assertNotNull(Classes.getExactClassInfo(Float.class));
        assertNotNull(Classes.getExactClassInfo(Boolean.class));
        assertNotNull(Classes.getExactClassInfo(Short.class));
        assertNotNull(Classes.getExactClassInfo(Byte.class));
        assertNotNull(Classes.getExactClassInfo(String.class));
        assertNotNull(Classes.getExactClassInfo(UUID.class));

        ClassInfo<Number> numberInfo = Classes.getExactClassInfo(Number.class);
        assertEquals(
                180.0D,
                numberInfo.getParser().parse("3.141592653589793 radians", ParseContext.DEFAULT).doubleValue(),
                1.0E-9
        );
        assertEquals(0.25D, numberInfo.getParser().parse("25%", ParseContext.DEFAULT));
        assertEquals("1.5", ((ch.njol.skript.classes.Parser<Number>) numberInfo.getParser()).toVariableNameString(1.5D));

        ClassInfo<Integer> integerInfo = Classes.getExactClassInfo(Integer.class);
        assertEquals(1_024, integerInfo.getParser().parse("1_024", ParseContext.DEFAULT));
        assertNull(integerInfo.getParser().parse("1 radian", ParseContext.DEFAULT));

        ClassInfo<Boolean> booleanInfo = Classes.getExactClassInfo(Boolean.class);
        assertEquals(Boolean.TRUE, booleanInfo.getParser().parse("yes", ParseContext.DEFAULT));
        assertEquals("true", ((ch.njol.skript.classes.Parser<Boolean>) booleanInfo.getParser()).toString(Boolean.TRUE, 0));

        ClassInfo<String> stringInfo = Classes.getExactClassInfo(String.class);
        assertEquals("quoted \"value\"", stringInfo.getParser().parse("\"quoted \"\"value\"\"\"", ParseContext.SCRIPT));
        assertEquals("plain", stringInfo.getParser().parse("plain", ParseContext.CONFIG));
        assertNull(stringInfo.getParser().parse("plain", ParseContext.SCRIPT));

        ClassInfo<UUID> uuidInfo = Classes.getExactClassInfo(UUID.class);
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        assertEquals(uuid, uuidInfo.getParser().parse(uuid.toString().toUpperCase(), ParseContext.DEFAULT));
    }

    @Test
    void javaDefaultConvertersAndComparatorRegisterIdempotently() {
        JavaClasses.register();
        DefaultConverters.register();
        DefaultConverters.register();
        DefaultComparators.register();
        DefaultComparators.register();

        assertEquals(3, ch.njol.skript.registrations.Converters.convert(3.9D, Integer.class));
        assertEquals(3L, ch.njol.skript.registrations.Converters.convert(3.9D, Long.class));
        assertEquals(Relation.EQUAL, ch.njol.skript.registrations.Comparators.compare(1.0F, 1.0D));
        assertEquals(Relation.SMALLER, ch.njol.skript.registrations.Comparators.compare(1, 2L));
        assertTrue(ch.njol.skript.registrations.Comparators.getComparator(Number.class, Number.class).supportsOrdering());
    }
}
