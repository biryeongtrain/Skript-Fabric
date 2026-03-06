package ch.njol.skript;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public final class ScriptLoader {

    private ScriptLoader() {
    }

    public static String replaceOptions(String text) {
        return text;
    }

    public static List<TriggerItem> loadItems(SectionNode node) {
        List<TriggerItem> items = new ArrayList<>();
        ParserInstance parser = ParserInstance.get();
        Node previousNode = parser.getNode();
        TriggerItem previousItem = null;
        try {
            for (Node child : node) {
                parser.setNode(child);
                Statement statement = parseStatement(child, items);
                if (statement != null) {
                    if (previousItem != null) {
                        previousItem.setNext(statement);
                    }
                    items.add(statement);
                    previousItem = statement;
                }
            }
        } finally {
            parser.setNode(previousNode);
        }
        return items;
    }

    public static List<TriggerItem> loadTriggerItems(SectionNode node) {
        return loadItems(node);
    }

    private static @Nullable Statement parseStatement(Node node, List<TriggerItem> triggerItems) {
        String key = node.getKey();
        if (key == null || key.isBlank()) {
            return null;
        }
        String parsedInput = replaceOptions(key.trim());
        if (node instanceof SectionNode sectionNode) {
            return Statement.parse(parsedInput, null, sectionNode, triggerItems);
        }
        return Statement.parse(parsedInput, null);
    }
}
