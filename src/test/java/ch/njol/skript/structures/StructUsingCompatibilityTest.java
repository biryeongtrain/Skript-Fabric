package ch.njol.skript.structures;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.lang.experiment.Experiment;
import org.skriptlang.skript.lang.experiment.ExperimentSet;
import org.skriptlang.skript.lang.experiment.LifeCycle;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

class StructUsingCompatibilityTest {

    @BeforeEach
    void setUp() {
        Skript.instance().syntaxRegistry().clearAll();
        ParserInstance.get().setCurrentScript(new Script(
                new Config("using", "using.sk", new File("using.sk")),
                List.of()
        ));
        StructUsing.register();
    }

    @AfterEach
    void tearDown() {
        Skript.instance().syntaxRegistry().clearAll();
        ParserInstance.get().setCurrentScript(null);
    }

    @Test
    void usingStructureAddsRegisteredExperimentToCurrentScript() {
        Experiment queues = Skript.experiments().register(
                Skript.getAddonInstance(),
                "queues",
                LifeCycle.EXPERIMENTAL
        );

        Structure structure = Structure.parse("using queues", new SimpleNode("using queues"), null);

        assertNotNull(structure);
        StructUsing parsed = assertInstanceOf(StructUsing.class, structure);
        assertEquals(StructUsing.PRIORITY, parsed.getPriority());
        assertTrue(parsed.load());
        ExperimentSet enabled = ParserInstance.get().getCurrentScript().getData(ExperimentSet.class);
        assertNotNull(enabled);
        assertTrue(enabled.hasExperiment(queues));
        assertEquals("using queues", parsed.toString(null, false));
    }

    @Test
    void usingStructureWarnsForUnknownExperimentsButStillRecordsThem() {
        try (TestLogAppender logs = TestLogAppender.attach()) {
            Structure structure = Structure.parse("using mystery feature", new SimpleNode("using mystery feature"), null);

            assertNotNull(structure);
            assertInstanceOf(StructUsing.class, structure);
            ExperimentSet enabled = ParserInstance.get().getCurrentScript().getData(ExperimentSet.class);
            assertNotNull(enabled);
            assertTrue(enabled.hasExperiment("mystery feature"));
            assertTrue(logs.messages().stream().anyMatch(message ->
                    message.contains("The experimental feature 'mystery feature' was not found.")
            ));
        }
    }

    private static final class TestLogAppender extends AbstractAppender implements AutoCloseable {

        private final List<String> messages = new ArrayList<>();
        private final Logger logger;

        private TestLogAppender(Logger logger) {
            super("StructUsingCompatibilityTest", null, PatternLayout.createDefaultLayout(), false, null);
            this.logger = logger;
        }

        static TestLogAppender attach() {
            Logger logger = (Logger) LogManager.getLogger("skript-fabric");
            TestLogAppender appender = new TestLogAppender(logger);
            appender.start();
            appender.logger.addAppender(appender);
            return appender;
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        List<String> messages() {
            return List.copyOf(messages);
        }

        @Override
        public void close() {
            logger.removeAppender((Appender) this);
            stop();
        }
    }
}
