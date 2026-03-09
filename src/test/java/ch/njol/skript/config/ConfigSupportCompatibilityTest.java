package ch.njol.skript.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ConfigSupportCompatibilityTest {

    @Test
    void configNavigatesNestedPathsThroughNodeNavigator() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode section = new SectionNode("options");
        section.add(new EntryNode("mode", "emerald"));
        config.getMainNode().add(section);

        assertSame(section, config.getNodeAt("options"));
        assertEquals("emerald", config.getByPath("options.mode"));
        assertSame(config.getMainNode(), config.getCurrentNode());
    }

    @Test
    void invalidNodeIncrementsConfigErrors() {
        Config config = new Config("test", "test.sk", new File("test.sk"));

        new InvalidNode("broken", "# comment", config.getMainNode(), 4);

        assertEquals(1, config.errorCount());
        assertFalse(config.valid());
    }

    @Test
    void optionLoadsConfiguredValueAndCallsSetter() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode section = new SectionNode("group");
        section.add(new EntryNode("answer", "42"));
        config.getMainNode().add(section);
        AtomicReference<String> seen = new AtomicReference<>();
        Option<String> option = new Option<>("answer", "fallback").setter(seen::set);

        option.set(config, "group.");

        assertEquals("42", option.value());
        assertEquals("42", seen.get());
    }

    @Test
    void optionSectionReflectsLoadedOptionValues() {
        Config config = new Config("test", "test.sk", new File("test.sk"));
        SectionNode section = new SectionNode("group");
        section.add(new EntryNode("answer", "emerald"));
        config.getMainNode().add(section);
        SampleOptions options = new SampleOptions();

        options.answer.set(config, "group.");

        assertEquals("emerald", options.get("answer"));
        assertNull(options.get("missing"));
    }

    @Test
    void enumParserAcceptsSpaceSeparatedValues() {
        EnumParser<SampleEnum> parser = new EnumParser<>(SampleEnum.class, "sample");

        assertSame(SampleEnum.REDSTONE_BLOCK, parser.convert("redstone block"));
    }

    private static final class SampleOptions extends OptionSection {
        private final Option<String> answer = new Option<>("answer", "fallback");

        private SampleOptions() {
            super("group");
        }
    }

    private enum SampleEnum {
        REDSTONE_BLOCK
    }
}
