package ch.njol.skript.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class SectionNode extends Node implements Iterable<Node> {

    private final List<Node> children = new ArrayList<>();
    private @Nullable NodeMap nodeMap;

    public SectionNode() {
    }

    public SectionNode(String key) {
        super(key);
    }

    private NodeMap nodeMap() {
        if (nodeMap == null) {
            nodeMap = new NodeMap();
            for (Node child : children) {
                nodeMap.put(child);
            }
        }
        return nodeMap;
    }

    public int size() {
        return children.size();
    }

    public void add(Node node) {
        node.remove();
        node.setParent(this);
        children.add(node);
        nodeMap().put(node);
    }

    public void remove(Node node) {
        if (!children.remove(node)) {
            return;
        }
        node.setParent(null);
        nodeMap().remove(node);
    }

    public @Nullable Node remove(@Nullable String key) {
        Node node = nodeMap().remove(key);
        if (node == null) {
            return null;
        }
        children.remove(node);
        node.setParent(null);
        return node;
    }

    public Node getNext(Node node) {
        int index = children.indexOf(node);
        if (index < 0 || index + 1 >= children.size()) {
            return null;
        }
        return children.get(index + 1);
    }

    public @Nullable Node get(@Nullable String key) {
        return nodeMap().get(key);
    }

    public @Nullable String getValue(String key) {
        Node node = get(key);
        return node instanceof EntryNode entryNode ? entryNode.getValue() : null;
    }

    public String get(String key, String defaultValue) {
        String value = getValue(key);
        return value == null ? defaultValue : value;
    }

    public void set(String key, String value) {
        Node node = get(key);
        if (node instanceof EntryNode entryNode) {
            entryNode.setValue(value);
            return;
        }
        set(key, new EntryNode(key, value));
    }

    public void set(String key, @Nullable Node node) {
        if (node == null) {
            remove(key);
            return;
        }
        Node existing = get(key);
        if (existing == null) {
            if (node.getKey() == null) {
                node.setKey(key);
            }
            add(node);
            return;
        }
        int index = children.indexOf(existing);
        if (index < 0) {
            if (node.getKey() == null) {
                node.setKey(key);
            }
            add(node);
            return;
        }
        existing.setParent(null);
        nodeMap().remove(existing);
        node.remove();
        if (node.getKey() == null) {
            node.setKey(key);
        }
        node.setParent(this);
        children.set(index, node);
        nodeMap().put(node);
    }

    void renamed(Node node, @Nullable String oldKey) {
        if (!children.contains(node)) {
            throw new IllegalArgumentException("node is not part of this section");
        }
        nodeMap().remove(oldKey);
        nodeMap().put(node);
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void convertToEntries(int levels) {
        if (levels < -1) {
            throw new IllegalArgumentException("levels must be >= -1");
        }
        for (int i = 0; i < children.size(); i++) {
            Node node = children.get(i);
            if (levels != 0 && node instanceof SectionNode sectionNode) {
                sectionNode.convertToEntries(levels == -1 ? -1 : levels - 1);
            }
            if (!(node instanceof SimpleNode simpleNode)) {
                continue;
            }
            String keyAndValue = simpleNode.getKey();
            if (keyAndValue == null) {
                continue;
            }
            int separator = keyAndValue.indexOf(':');
            if (separator < 0) {
                continue;
            }
            EntryNode entryNode = new EntryNode(
                    keyAndValue.substring(0, separator).trim(),
                    keyAndValue.substring(separator + 1).trim()
            );
            entryNode.setLine(simpleNode.getLine());
            entryNode.setDebug(simpleNode.debug());
            entryNode.setParent(this);
            children.set(i, entryNode);
            nodeMap().remove(simpleNode);
            nodeMap().put(entryNode);
        }
    }

    @Override
    public Iterator<Node> iterator() {
        return children.iterator();
    }
}
