package ch.njol.skript.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
