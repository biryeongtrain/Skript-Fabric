package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.SyntaxElement;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.skriptlang.skript.registration.SyntaxInfo;

/**
 * A stack tracking currently parsed syntax elements.
 */
public class ParsingStack implements Iterable<ParsingStack.Element> {

    private final LinkedList<Element> stack;

    public ParsingStack() {
        this.stack = new LinkedList<>();
    }

    public ParsingStack(ParsingStack parsingStack) {
        this.stack = new LinkedList<>(parsingStack.stack);
    }

    public Element pop() throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return stack.pop();
    }

    public Element peek(int index) throws IndexOutOfBoundsException {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }
        return stack.get(index);
    }

    public Element peek() throws IllegalStateException {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return stack.peek();
    }

    public void push(Element element) {
        stack.push(element);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    public void print(PrintStream printStream) {
        synchronized (printStream) {
            printStream.println("Stack:");
            if (stack.isEmpty()) {
                printStream.println("<empty>");
                return;
            }
            for (Element element : stack) {
                printStream.println("\t" + element.getSyntaxElementClass().getName() + " @ " + element.patternIndex());
            }
        }
    }

    @Override
    public Iterator<Element> iterator() {
        return Collections.unmodifiableList(stack).iterator();
    }

    public record Element(SyntaxInfo<?> syntaxElementInfo, int patternIndex) {

        public Element {
            if (syntaxElementInfo == null) {
                throw new IllegalArgumentException("syntaxElementInfo must not be null");
            }
            if (patternIndex < 0 || patternIndex >= syntaxElementInfo.patterns().length) {
                throw new IllegalArgumentException("Invalid pattern index " + patternIndex);
            }
        }

        public SyntaxInfo<?> syntaxInfo() {
            return syntaxElementInfo;
        }

        public Class<? extends SyntaxElement> getSyntaxElementClass() {
            return syntaxElementInfo.type();
        }

        public String getPattern() {
            return getPatterns()[patternIndex];
        }

        public String[] getPatterns() {
            return syntaxElementInfo.patterns();
        }
    }
}
