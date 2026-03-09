package ch.njol.skript.config;

import java.util.concurrent.atomic.AtomicBoolean;

public class Node {

    private String key;
    private int line = -1;
    private boolean debug;
    private SectionNode parent;

    public Node() {
    }

    public Node(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        String previousKey = this.key;
        this.key = key;
        SectionNode currentParent = parent;
        if (currentParent != null) {
            currentParent.renamed(this, previousKey);
        }
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public boolean debug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public SectionNode getParent() {
        return parent;
    }

    public void setParent(SectionNode parent) {
        this.parent = parent;
    }

    public void remove() {
        SectionNode currentParent = parent;
        if (currentParent == null) {
            return;
        }
        currentParent.remove(this);
    }

    public static LineSplit splitLine(String line) {
        return splitLine(line, new AtomicBoolean(false));
    }

    public static LineSplit splitLine(String line, AtomicBoolean inBlockComment) {
        String trimmed = line.trim();
        if (trimmed.equals("###")) {
            inBlockComment.set(!inBlockComment.get());
            return new LineSplit("", line);
        }
        if (trimmed.startsWith("#")) {
            return new LineSplit("", line.substring(line.indexOf('#')));
        }
        if (inBlockComment.get()) {
            return new LineSplit("", line);
        }

        int length = line.length();
        StringBuilder code = new StringBuilder(line);
        int removedHashes = 0;
        SplitLineState state = SplitLineState.CODE;
        SplitLineState previousState = SplitLineState.CODE;
        for (int index = 0; index < length; index++) {
            char current = line.charAt(index);
            if (current == '%' || current == '"' || current == '#' || current == '{' || current == '}') {
                if ((current != '#' || state != SplitLineState.STRING)
                        && (current == '%' || current == '"' || current == '#')
                        && index + 1 < length
                        && line.charAt(index + 1) == current) {
                    if (current == '#') {
                        code.deleteCharAt(index - removedHashes);
                        removedHashes++;
                    }
                    index++;
                    continue;
                }
                SplitLineState previous = state;
                state = SplitLineState.update(current, state, previousState);
                if (state == SplitLineState.HALT) {
                    return new LineSplit(code.substring(0, index - removedHashes), line.substring(index));
                }
                if (current == '%' && state == SplitLineState.CODE) {
                    previousState = previous;
                }
            }
        }
        return new LineSplit(code.toString(), "");
    }

    public record LineSplit(String value, String comment) {
    }

    private enum SplitLineState {
        HALT,
        CODE,
        STRING,
        VARIABLE;

        private static SplitLineState update(char current, SplitLineState state, SplitLineState previousState) {
            if (state == HALT) {
                return HALT;
            }
            return switch (current) {
                case '%' -> state == CODE ? previousState : CODE;
                case '"' -> switch (state) {
                    case CODE -> STRING;
                    case STRING -> CODE;
                    default -> state;
                };
                case '{' -> state == STRING ? STRING : VARIABLE;
                case '}' -> state == STRING ? STRING : CODE;
                case '#' -> state == STRING ? STRING : HALT;
                default -> state;
            };
        }
    }
}
