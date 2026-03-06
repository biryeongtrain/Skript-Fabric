package ch.njol.skript.lang.util.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AnyCommonCompatibilityTest {

    @AfterEach
    void cleanupClassInfos() {
        Classes.clearClassInfos();
    }

    @Test
    void anyContainsUsesSafetyCheckBeforeContainment() {
        AnyContains<Integer> positiveOnly = new AnyContains<>() {
            @Override
            public boolean contains(Integer value) {
                return value != null && value > 0;
            }

            @Override
            public boolean isSafeToCheck(Object value) {
                return value instanceof Integer;
            }
        };

        assertTrue(positiveOnly.checkSafely(3));
        assertFalse(positiveOnly.checkSafely(-1));
        assertFalse(positiveOnly.checkSafely("3"));
    }

    @Test
    void anyValuedSupportsParserBackedSafeChange() {
        ClassInfo<WrappedInt> info = new ClassInfo<>(WrappedInt.class);
        info.setParser(new ClassInfo.Parser<>() {
            @Override
            public boolean canParse(ParseContext context) {
                return true;
            }

            @Override
            public WrappedInt parse(String input, ParseContext context) {
                try {
                    return new WrappedInt(Integer.parseInt(input));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        });
        Classes.registerClassInfo(info);

        WrappedValue value = new WrappedValue();
        value.changeValueSafely("12");
        assertEquals(new WrappedInt(12), value.value());
    }

    @Test
    void anyValuedStringBranchUsesMessageString() {
        StringValue value = new StringValue();
        value.changeValueSafely(123);
        assertEquals("123", value.value());

        value.resetValue();
        assertNull(value.value());
    }

    private record WrappedInt(int value) {
    }

    private static class WrappedValue implements AnyValued<WrappedInt> {

        private WrappedInt value;

        @Override
        public WrappedInt value() {
            return value;
        }

        @Override
        public boolean supportsValueChange() {
            return true;
        }

        @Override
        public void changeValue(WrappedInt value) {
            this.value = value;
        }

        @Override
        public Class<WrappedInt> valueType() {
            return WrappedInt.class;
        }
    }

    private static class StringValue implements AnyValued<String> {

        private String value;

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean supportsValueChange() {
            return true;
        }

        @Override
        public void changeValue(String value) {
            this.value = value;
        }

        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }
}
