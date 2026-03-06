package ch.njol.skript.lang;

/**
 * A syntax element that restricts the runtime event contexts it supports.
 */
@FunctionalInterface
public interface EventRestrictedSyntax {

    /**
     * Supported event context classes.
     * This is checked against {@link ch.njol.skript.lang.parser.ParserInstance#isCurrentEvent(Class[])}
     * before a syntax element is initialized.
     */
    Class<?>[] supportedEvents();
}
