package ch.njol.skript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.validate.EnumEntryValidator;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.ParsedEntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.log.RetainingLogHandler;
import ch.njol.skript.log.SkriptLogger;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ConfigValidationCompatibilityTest {

    @Test
    void configLoadsNestedOptionSectionsReflectively() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode outer = new SectionNode("group");
        outer.add(new EntryNode("answer", "stone"));
        SectionNode inner = new SectionNode("nested");
        inner.add(new EntryNode("count", "7"));
        outer.add(inner);
        config.getMainNode().add(outer);

        SampleOptions options = new SampleOptions();
        config.load(options);

        assertEquals("stone", options.answer.value());
        assertEquals(7, options.nested.count.value());
    }

    @Test
    void entryValidatorRejectsNonEntriesWithUpstreamMessage() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode section = new SectionNode("root");
        config.getMainNode().add(section);

        try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
            assertFalse(new EntryValidator().validate(section));
            assertEquals("'root' is not an entry (like 'name : value')", log.getFirstError().getMessage());
        }
    }

    @Test
    void parsedEntryValidatorParsesConfigContextValues() {
        AtomicInteger seen = new AtomicInteger();
        Parser<Integer> parser = new Parser<>() {
            @Override
            public Integer parse(String input, ParseContext context) {
                return context == ParseContext.CONFIG ? Integer.parseInt(input) : null;
            }

            @Override
            public String toString(Integer value, int flags) {
                return String.valueOf(value);
            }

            @Override
            public String toVariableNameString(Integer value) {
                return String.valueOf(value);
            }
        };

        boolean valid = new ParsedEntryValidator<>(parser, seen::set).validate(new EntryNode("count", "12"));

        assertTrue(valid);
        assertEquals(12, seen.get());
    }

    @Test
    void enumEntryValidatorAcceptsSpaceSeparatedNames() {
        AtomicReference<SampleEnum> seen = new AtomicReference<>();

        boolean valid = new EnumEntryValidator<>(SampleEnum.class, seen::set)
                .validate(new EntryNode("shape", "red stone"));

        assertTrue(valid);
        assertEquals(SampleEnum.RED_STONE, seen.get());
    }

    @Test
    void sectionValidatorRejectsUnexpectedEntriesAndRequiresMissingKeys() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode section = new SectionNode("root");
        section.setLine(3);
        section.add(new EntryNode("extra", "value"));
        config.getMainNode().add(section);

        SectionValidator validator = new SectionValidator().addEntry("required", false);

        try (RetainingLogHandler log = SkriptLogger.startRetainingLog()) {
            assertFalse(validator.validate(section));
            assertEquals(2, log.getNumErrors());
            assertEquals("Required entry 'required' is missing in 'root' (test.sk, starting at line 3)",
                    log.getLog().iterator().next().getMessage());
        }
    }

    private static final class SampleOptions extends OptionSection {
        private final Option<String> answer = new Option<>("answer", "fallback");
        private final NestedOptions nested = new NestedOptions();

        private SampleOptions() {
            super("group");
        }
    }

    private static final class NestedOptions extends OptionSection {
        private final Option<Integer> count = new Option<>("count", 0, Integer::parseInt);

        private NestedOptions() {
            super("nested");
        }
    }

    private enum SampleEnum {
        RED_STONE
    }
}
