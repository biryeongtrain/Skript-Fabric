package ch.njol.skript.config;

import java.util.Collections;
import java.util.Iterator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NodeNavigator extends Iterable<Node> {

    @Override
    default Iterator<Node> iterator() {
        return Collections.emptyIterator();
    }

    @Nullable Node get(String key);

    @NotNull Node getCurrentNode();

    default @Nullable Node getNodeAt(@NotNull String @NotNull ... steps) {
        Node node = getCurrentNode();
        for (String step : steps) {
            if (node == null) {
                return null;
            }
            node = node.get(step);
        }
        return node;
    }

    @Contract("null -> this")
    default @Nullable Node getNodeAt(@Nullable String path) {
        if (path == null || path.isEmpty()) {
            return getCurrentNode();
        }
        if (!path.contains(".")) {
            return getNodeAt(new String[]{path});
        }
        return getNodeAt(path.split("\\."));
    }

    default @Nullable String getValue(String path) {
        Node node = getNodeAt(path);
        if (node instanceof EntryNode entryNode) {
            return entryNode.getValue();
        }
        return null;
    }
}
