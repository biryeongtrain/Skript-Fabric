package ch.njol.skript.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

final class NodeMap {

    private final Map<String, Node> nodes = new HashMap<>();

    private static String normalize(String key) {
        return key.toLowerCase(Locale.ENGLISH);
    }

    private static boolean isMappable(Node node) {
        return node instanceof EntryNode || node instanceof SectionNode;
    }

    void put(Node node) {
        String key = node.getKey();
        if (!isMappable(node) || key == null) {
            return;
        }
        nodes.put(normalize(key), node);
    }

    public @Nullable Node get(@Nullable String key) {
        if (key == null) {
            return null;
        }
        return nodes.get(normalize(key));
    }

    public @Nullable Node remove(Node node) {
        String key = node.getKey();
        return key == null ? null : nodes.remove(normalize(key));
    }

    public @Nullable Node remove(@Nullable String key) {
        if (key == null) {
            return null;
        }
        return nodes.remove(normalize(key));
    }
}
