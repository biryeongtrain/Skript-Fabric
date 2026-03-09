package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.NonNullPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.SyntaxInfo;

public class StructVariables extends Structure {

    public static final Priority PRIORITY = new Priority(300);

    static {
        Skript.registerStructure(StructVariables.class, SyntaxInfo.Structure.NodeType.SECTION, "variables");
    }

    public static final class DefaultVariables implements ScriptData {

        private final List<NonNullPair<String, Object>> variables;
        private final java.util.Deque<Map<String, Class<?>[]>> hints = new java.util.ArrayDeque<>();
        private boolean loaded;

        public DefaultVariables(Collection<NonNullPair<String, Object>> variables) {
            this.variables = List.copyOf(variables);
        }

        public void add(String variable, Class<?>... hints) {
            if (hints == null || hints.length == 0 || containsOnlyObject(hints)) {
                return;
            }
            Map<String, Class<?>[]> map = this.hints.peekFirst();
            if (map != null) {
                map.put(variable, hints);
            }
        }

        public void enterScope() {
            hints.push(new HashMap<>());
        }

        public void exitScope() {
            hints.pop();
        }

        public Class<?> @Nullable [] get(String variable) {
            for (Map<String, Class<?>[]> map : hints) {
                Class<?>[] result = map.get(variable);
                if (result != null && result.length > 0) {
                    return result;
                }
            }
            return null;
        }

        public boolean hasDefaultVariables() {
            return !variables.isEmpty();
        }

        @Unmodifiable
        public List<NonNullPair<String, Object>> getVariables() {
            return Collections.unmodifiableList(variables);
        }

        private boolean isLoaded() {
            return loaded;
        }

        private static boolean containsOnlyObject(Class<?>[] hints) {
            for (Class<?> hint : hints) {
                if (hint != Object.class) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean init(
            Literal<?>[] args,
            int matchedPattern,
            ParseResult parseResult,
            @Nullable EntryContainer entryContainer
    ) {
        if (entryContainer == null) {
            return false;
        }

        SectionNode node = entryContainer.getSource();
        List<NonNullPair<String, Object>> variables;
        Script script = getParser().getCurrentScript();
        DefaultVariables existing = script.getData(DefaultVariables.class);
        if (existing != null && existing.hasDefaultVariables()) {
            variables = new ArrayList<>(existing.variables);
        } else {
            variables = new ArrayList<>();
        }

        for (Node child : node) {
            EntryNode entry = asVariableEntry(child);
            if (entry == null) {
                Skript.error("Invalid line in variables structure");
                continue;
            }

            String normalizedName = normalizeVariableName(entry.getKey());
            if (normalizedName == null) {
                continue;
            }

            Object value = parseVariableValue(entry.getValue());
            if (value == null) {
                continue;
            }
            variables.add(new NonNullPair<>(normalizedName, value));
        }

        script.addData(new DefaultVariables(variables));
        return true;
    }

    @Override
    public boolean load() {
        DefaultVariables data = getParser().getCurrentScript().getData(DefaultVariables.class);
        if (data == null) {
            Skript.error("Default variables data missing");
            return false;
        }
        if (data.isLoaded()) {
            return true;
        }
        for (NonNullPair<String, Object> pair : data.getVariables()) {
            if (Variables.getVariable(pair.first(), null, false) == null) {
                Variables.setVariable(pair.first(), pair.second(), null, false);
            }
        }
        data.loaded = true;
        return true;
    }

    @Override
    public void postUnload() {
        Script script = getParser().getCurrentScript();
        DefaultVariables data = script.getData(DefaultVariables.class);
        if (data == null) {
            return;
        }
        for (NonNullPair<String, Object> pair : data.getVariables()) {
            String name = pair.first();
            if (name.contains("<") && name.contains(">")) {
                Variables.setVariable(name, null, null, false);
            }
        }
        script.removeData(DefaultVariables.class);
    }

    @Override
    public Priority getPriority() {
        return PRIORITY;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "variables";
    }

    private static @Nullable EntryNode asVariableEntry(Node node) {
        if (node instanceof EntryNode entryNode) {
            return entryNode;
        }
        if (!(node instanceof ch.njol.skript.config.SimpleNode simpleNode)) {
            return null;
        }
        String key = simpleNode.getKey();
        if (key == null) {
            return null;
        }
        int separator = key.indexOf('=');
        if (separator < 0) {
            return null;
        }
        EntryNode entryNode = new EntryNode(
                key.substring(0, separator).trim(),
                key.substring(separator + 1).trim()
        );
        entryNode.setLine(simpleNode.getLine());
        entryNode.setDebug(simpleNode.debug());
        return entryNode;
    }

    private static @Nullable String normalizeVariableName(String name) {
        String normalized = name.toLowerCase(Locale.ENGLISH);
        if (normalized.startsWith("{") && normalized.endsWith("}")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        } else {
            Skript.warning(
                    "It is suggested to use brackets around the name of a variable. Example: {example::%player%} = 5\n"
                            + "Excluding brackets is deprecated, meaning this warning will become an error in the future."
            );
        }

        if (normalized.startsWith(Variable.LOCAL_VARIABLE_TOKEN)) {
            Skript.error("'" + normalized + "' cannot be a local variable in default variables structure");
            return null;
        }
        if (normalized.contains("<") || normalized.contains(">")) {
            Skript.error("'" + normalized + "' cannot have symbol '<' or '>' within the definition");
            return null;
        }

        String original = normalized;
        normalized = replacePlaceholders(normalized, original);
        if (normalized == null) {
            return null;
        }
        if (normalized.contains("%")) {
            Skript.error("Invalid use of percent signs in variable name");
            return null;
        }
        return normalized;
    }

    private static @Nullable String replacePlaceholders(String name, String original) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < name.length()) {
            int start = name.indexOf('%', index);
            if (start < 0) {
                result.append(name, index, name.length());
                return result.toString();
            }
            int end = name.indexOf('%', start + 1);
            if (end < 0) {
                result.append(name, index, name.length());
                return result.toString();
            }

            result.append(name, index, start);
            String typeName = name.substring(start + 1, end);
            if (typeName.contains("{") || typeName.contains("}") || typeName.contains("%")) {
                Skript.error("'" + original + "' is not a valid name for a default variable");
                return null;
            }
            ClassInfo<?> classInfo = Classes.getClassInfoFromUserInput(typeName);
            if (classInfo == null) {
                Skript.error("Can't understand the type '" + typeName + "'");
                return null;
            }
            result.append('<').append(classInfo.getCodeName()).append('>');
            index = end + 1;
        }
        return result.toString();
    }

    private static @Nullable Object parseVariableValue(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
        }

        ParseLogHandler log = SkriptLogger.startParseLogHandler();
        try {
            Object parsed = Classes.parseSimple(value, Object.class, ParseContext.SCRIPT);
            if (parsed == null) {
                log.printError("Can't understand the value '" + value + "'");
                return null;
            }
            log.printLog();
            return parsed;
        } finally {
            log.stop();
        }
    }
}
