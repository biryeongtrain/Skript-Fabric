package ch.njol.skript.lang.parser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Signals a stack overflow during Skript parsing with parsing stack details.
 */
public class ParseStackOverflowException extends RuntimeException {

    protected final ParsingStack parsingStack;

    public ParseStackOverflowException(StackOverflowError cause, ParsingStack parsingStack) {
        super(createMessage(parsingStack), cause);
        this.parsingStack = parsingStack;
    }

    private static String createMessage(ParsingStack stack) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream);
        stack.print(printStream);
        return stream.toString();
    }
}
