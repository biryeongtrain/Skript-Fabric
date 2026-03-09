package ch.njol.skript.config;

public class VoidNode extends Node {

    VoidNode(String line, String comment, SectionNode parent, int lineNum) {
        super(line == null ? null : line.trim());
        setComment(comment);
        setParent(parent);
        setLine(lineNum);
    }

    public void set(String value) {
        setKey(value);
    }
}
