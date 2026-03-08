package ch.njol.skript;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptData;

public final class ScriptLoader {

    private static final Pattern OPTION_PATTERN = Pattern.compile("\\{@(.+?)}");

    private ScriptLoader() {
    }

    public static String replaceOptions(String text) {
        ParserInstance parser = ParserInstance.get();
        if (!parser.isActive()) {
            return text;
        }
        var currentScript = parser.getCurrentScript();
        if (currentScript == null) {
            return text;
        }
        OptionsData optionsData = currentScript.getData(OptionsData.class);
        return optionsData == null ? text : optionsData.replaceOptions(text);
    }

    public static List<TriggerItem> loadItems(SectionNode node) {
        List<TriggerItem> items = new ArrayList<>();
        ParserInstance parser = ParserInstance.get();
        Node previousNode = parser.getNode();
        TriggerItem previousItem = null;
        try {
            for (Node child : node) {
                parser.setNode(child);
                TriggerItem item = parseTriggerItem(child, items);
                if (item != null) {
                    if (previousItem != null) {
                        previousItem.setNext(item);
                    }
                    items.add(item);
                    previousItem = item;
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

    private static @Nullable TriggerItem parseTriggerItem(Node node, List<TriggerItem> triggerItems) {
        String key = node.getKey();
        if (key == null || key.isBlank()) {
            return null;
        }
        String parsedInput = replaceOptions(key.trim());
        if (!SkriptParser.validateLine(parsedInput)) {
            return null;
        }
        if (node instanceof SectionNode sectionNode) {
            TriggerItem section = ch.njol.skript.lang.Section.parse(parsedInput, null, sectionNode, triggerItems);
            if (section != null) {
                return section;
            }
            return Statement.parse(parsedInput, "Can't understand this section: " + parsedInput, sectionNode, triggerItems);
        }
        return Statement.parse(parsedInput, triggerItems, "Can't understand this condition/effect: " + parsedInput);
    }

    public static final class OptionsData implements ScriptData {

        private final Map<String, String> options = new ConcurrentHashMap<>();

        public void put(String key, String value) {
            options.put(key, value);
        }

        public @Nullable String get(String key) {
            return options.get(key);
        }

        public Map<String, String> getOptions() {
            return Map.copyOf(options);
        }

        public String replaceOptions(String text) {
            Matcher matcher = OPTION_PATTERN.matcher(text);
            StringBuffer replaced = new StringBuffer();
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = options.get(key);
                if (value == null) {
                    Skript.error("undefined option {@" + key + "}");
                    matcher.appendReplacement(replaced, Matcher.quoteReplacement(matcher.group()));
                    continue;
                }
                matcher.appendReplacement(replaced, Matcher.quoteReplacement(value));
            }
            matcher.appendTail(replaced);
            return replaced.toString();
        }
    }
}
