package ch.njol.skript.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.Collection;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;

class LogHandlerCompatibilityTest {

    @Test
    void countingLogHandlerCountsMessagesAtOrAboveMinimum() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog();
             CountingLogHandler counter = new CountingLogHandler(Level.WARNING).start()) {
            SkriptLogger.log(new LogEntry(Level.INFO, "info"));
            SkriptLogger.log(new LogEntry(Level.WARNING, "warn"));
            SkriptLogger.log(new LogEntry(Level.SEVERE, "error"));

            assertEquals(2, counter.getCount());
            assertEquals(3, sink.size());
        }
    }

    @Test
    void filteringLogHandlerSuppressesMessagesBelowMinimumBeforeTheyReachOlderHandlers() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog();
             FilteringLogHandler filter = new FilteringLogHandler(Level.WARNING).start()) {
            SkriptLogger.log(new LogEntry(Level.INFO, "info"));
            SkriptLogger.log(new LogEntry(Level.WARNING, "warn"));

            assertEquals(1, sink.size());
            assertEquals("warn", sink.getLog().iterator().next().getMessage());
        }
    }

    @Test
    void blockingLogHandlerPreventsOlderHandlersFromReceivingEntries() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog();
             BlockingLogHandler blocker = new BlockingLogHandler().start()) {
            SkriptLogger.log(new LogEntry(Level.SEVERE, "blocked"));

            assertEquals(0, sink.size());
            assertEquals(0, sink.getNumErrors());
        }
    }

    @Test
    void errorDescLogHandlerLogsBeforeAndAfterErrorBoundaries() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog()) {
            ErrorDescLogHandler handler = new ErrorDescLogHandler("before", "after", "success").start();
            SkriptLogger.log(new LogEntry(Level.SEVERE, "boom"));
            handler.stop();

            assertEquals(3, sink.size());
            LogEntry[] entries = sink.getLog().toArray(LogEntry[]::new);
            assertEquals("before", entries[0].getMessage());
            assertEquals("boom", entries[1].getMessage());
            assertEquals("after", entries[2].getMessage());
        }
    }

    @Test
    void errorDescLogHandlerEmitsSuccessWhenNoErrorsWereSeen() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog()) {
            ErrorDescLogHandler handler = new ErrorDescLogHandler("before", "after", "success").start();
            SkriptLogger.log(new LogEntry(Level.INFO, "ok"));
            handler.stop();

            assertEquals(2, sink.size());
            LogEntry[] entries = sink.getLog().toArray(LogEntry[]::new);
            assertEquals("ok", entries[0].getMessage());
            assertEquals("success", entries[1].getMessage());
        }
    }

    @Test
    void parseLogHandlerStillReplaysRetainedErrorIntoOuterHandler() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog();
             ParseLogHandler parse = SkriptLogger.startParseLogHandler()) {
            SkriptLogger.log(new LogEntry(Level.SEVERE, ErrorQuality.NOT_AN_EXPRESSION, "specific"));

            assertTrue(parse.hasError());

            parse.printError("fallback", ErrorQuality.GENERIC);

            assertTrue(parse.isStopped());
            assertEquals(1, sink.getNumErrors());
            assertEquals("specific", sink.getFirstError().getMessage());
        }
    }

    @Test
    void retainingLogHandlerPrintErrorsLogsFallbackWhenNoErrorWasRetained() {
        try (RetainingLogHandler outer = SkriptLogger.startRetainingLog();
             RetainingLogHandler retained = SkriptLogger.startRetainingLog()) {
            boolean hadErrors = retained.printErrors("fallback", ErrorQuality.NOT_AN_EXPRESSION);

            assertFalse(hadErrors);

            LogEntry firstError = outer.getFirstError();
            assertNotNull(firstError);
            assertEquals("fallback", firstError.getMessage());
            assertEquals(ErrorQuality.NOT_AN_EXPRESSION, firstError.getQuality());
        }
    }

    @Test
    void retainingLogHandlerExposesOnlySevereEntriesThroughGetErrors() {
        try (RetainingLogHandler retained = SkriptLogger.startRetainingLog()) {
            SkriptLogger.log(new LogEntry(Level.INFO, "info"));
            SkriptLogger.log(new LogEntry(Level.WARNING, "warn"));
            SkriptLogger.log(new LogEntry(Level.SEVERE, "error"));

            Collection<LogEntry> errors = retained.getErrors();

            assertEquals(1, errors.size());
            assertEquals("error", errors.iterator().next().getMessage());
        }
    }

    @Test
    void logHandlersFollowParserInstanceBoundariesWhenSwitchingInstances() {
        ParserInstance outer = new ParserInstance();
        ParserInstance inner = new ParserInstance();

        ParserInstance.withInstance(outer, () -> {
            try (RetainingLogHandler outerHandler = SkriptLogger.startRetainingLog()) {
                SkriptLogger.log(new LogEntry(Level.WARNING, "outer"));

                ParserInstance.withInstance(inner, () -> {
                    try (RetainingLogHandler innerHandler = SkriptLogger.startRetainingLog()) {
                        SkriptLogger.log(new LogEntry(Level.SEVERE, "inner"));
                        assertEquals(1, innerHandler.size());
                        return null;
                    }
                });

                assertEquals(1, outerHandler.size());
                assertEquals("outer", outerHandler.getLog().iterator().next().getMessage());
            }
            return null;
        });
    }

    @Test
    void outOfOrderHandlerStopPopsInterveningHandlersFromTheStack() {
        try (RetainingLogHandler sink = SkriptLogger.startRetainingLog()) {
            CountingLogHandler older = new CountingLogHandler(Level.INFO).start();
            CountingLogHandler newer = new CountingLogHandler(Level.INFO).start();

            older.stop();
            SkriptLogger.log(new LogEntry(Level.INFO, "still routed"));

            assertTrue(older.isStopped());
            assertTrue(newer.isStopped());
            assertEquals(0, older.getCount());
            assertEquals(0, newer.getCount());
            assertEquals(1, sink.size());
            assertEquals("still routed", sink.getLog().iterator().next().getMessage());
        }
    }

    @Test
    void timingLogHandlerTracksElapsedTimeFromCreation() throws InterruptedException {
        TimingLogHandler handler = new TimingLogHandler();

        long start = handler.getStart();
        Thread.sleep(5L);

        assertTrue(handler.getTimeTaken() >= 0L);
        assertTrue(handler.getStart() == start);
    }

    @Test
    void verbosityBridgeMatchesUpstreamThresholdOrdering() {
        SkriptLogger.setVerbosity(Verbosity.HIGH);

        assertTrue(SkriptLogger.log(Verbosity.NORMAL));
        assertTrue(SkriptLogger.log(Verbosity.HIGH));
        assertFalse(SkriptLogger.log(Verbosity.VERY_HIGH));
        assertFalse(SkriptLogger.debug());

        SkriptLogger.setVerbosity(Verbosity.DEBUG);

        assertTrue(SkriptLogger.log(Verbosity.VERY_HIGH));
        assertTrue(SkriptLogger.debug());
    }

    @Test
    void nodeBridgeUsesCurrentParserInstanceStorage() {
        ParserInstance parser = new ParserInstance();
        SectionNode root = new SectionNode("root");
        SimpleNode child = new SimpleNode("child");
        root.add(child);

        ParserInstance.withInstance(parser, () -> {
            SkriptLogger.setNode(root);
            assertNull(SkriptLogger.getNode());

            SkriptLogger.setNode(child);
            assertEquals(child, SkriptLogger.getNode());
            return null;
        });
    }
}
