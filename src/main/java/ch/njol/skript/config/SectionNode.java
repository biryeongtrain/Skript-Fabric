package ch.njol.skript.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SectionNode extends Node implements Iterable<Node> {

    private final List<Node> children = new ArrayList<>();

    public SectionNode() {
    }

    public SectionNode(String key) {
        super(key);
    }

    public void add(Node node) {
        children.add(node);
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }
}
