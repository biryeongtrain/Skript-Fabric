package ch.njol.skript;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.script.ScriptWarning;

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
        boolean executionStops = false;
        try {
            for (Node child : node) {
                parser.setNode(child);
                TriggerItem item = parseTriggerItem(child, items);
                if (item != null) {
                    if (executionStops && shouldWarnAboutUnreachableCode(parser)) {
                        Skript.warning("Unreachable code. The previous statement stops further execution.");
                    }
                    if (previousItem != null) {
                        previousItem.setNext(item);
                    }
                    items.add(item);
                    previousItem = item;
                    executionStops = (item instanceof Statement statement
                            && statement.loaderExecutionIntent() != null)
                            || (item instanceof TriggerSection triggerSection
                            && triggerSection.loaderExecutionIntent() != null);
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
            return parseSectionTriggerItem(parsedInput, sectionNode, triggerItems);
        }
        return Statement.parse(parsedInput, triggerItems, "Can't understand this condition/effect: " + parsedInput);
    }

    private static @Nullable TriggerItem parseSectionTriggerItem(
            String parsedInput,
            SectionNode sectionNode,
            List<TriggerItem> triggerItems
    ) {
        String sectionError = "Can't understand this section: " + parsedInput;
        try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
            TriggerItem section = ch.njol.skript.lang.Section.parse(parsedInput, sectionError, sectionNode, triggerItems);
            if (section != null) {
                log.printLog();
                return section;
            }

            ParseLogHandler sectionLog = log.backup();
            log.clear();
            log.clearError();

            TriggerItem statement = Statement.parse(
                    parsedInput,
                    "Can't understand this condition/effect: " + parsedInput,
                    sectionNode,
                    triggerItems
            );
            if (statement != null) {
                log.printLog();
                return statement;
            }

            ParseLogHandler statementLog = log.backup();
            if (shouldRestoreSectionLog(parsedInput, sectionLog, statementLog)) {
                log.restore(sectionLog);
            } else {
                log.restore(statementLog);
            }

            if (log.getErrors().isEmpty()) {
                log.printError(sectionError);
            } else {
                log.printLog();
            }
            return null;
        }
    }

    private static boolean shouldRestoreSectionLog(
            String parsedInput,
            ParseLogHandler sectionLog,
            ParseLogHandler statementLog
    ) {
        List<LogEntry> statementErrors = statementLog.getErrors();
        if (statementErrors.isEmpty()) {
            return true;
        }

        String defaultStatementError = "Can't understand this condition/effect: " + parsedInput;
        boolean onlyDefaultStatementErrors = true;
        for (LogEntry error : statementErrors) {
            if (!defaultStatementError.equals(error.getMessage())) {
                onlyDefaultStatementErrors = false;
                break;
            }
        }
        if (onlyDefaultStatementErrors) {
            return true;
        }

        for (LogEntry error : sectionLog.getErrors()) {
            if (error.getMessage().contains("tried to claim the current section")) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldWarnAboutUnreachableCode(ParserInstance parser) {
        if (!parser.isActive()) {
            return false;
        }
        var currentScript = parser.getCurrentScript();
        return currentScript != null && !currentScript.suppressesWarning(ScriptWarning.UNREACHABLE_CODE);
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
