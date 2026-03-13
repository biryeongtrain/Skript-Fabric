package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.events.EvtSkript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;

public final class SkriptRuntime {

    private static final SkriptRuntime INSTANCE = new SkriptRuntime();

    private final List<Script> scripts = new ArrayList<>();

    private SkriptRuntime() {
    }

    public static SkriptRuntime instance() {
        return INSTANCE;
    }

    public synchronized void clearScripts() {
        List<Script> snapshot = new ArrayList<>(scripts);
        if (!snapshot.isEmpty()) {
            EvtSkript.onSkriptStop();
        }
        scripts.clear();
        unloadLoadedScripts(snapshot);
    }

    public synchronized Script loadFromPath(Path path) throws IOException {
        String source = Files.readString(path, StandardCharsets.UTF_8);
        return loadScript(path.getFileName().toString(), path.toString(), path.toFile(), source);
    }

    public synchronized Script loadFromPath(Path path, String displayPath) throws IOException {
        String source = Files.readString(path, StandardCharsets.UTF_8);
        return loadScript(path.getFileName().toString(), displayPath, path.toFile(), source);
    }

    public synchronized List<Script> loadedScripts() {
        return List.copyOf(scripts);
    }

    public synchronized int unloadScripts(Collection<Script> scriptsToUnload) {
        if (scriptsToUnload.isEmpty()) {
            return 0;
        }

        List<Script> removed = new ArrayList<>();
        for (Iterator<Script> iterator = scripts.iterator(); iterator.hasNext(); ) {
            Script script = iterator.next();
            if (scriptsToUnload.contains(script)) {
                removed.add(script);
                iterator.remove();
            }
        }
        if (removed.isEmpty()) {
            return 0;
        }
        if (scripts.isEmpty()) {
            EvtSkript.onSkriptStop();
        }
        unloadLoadedScripts(removed);
        return removed.size();
    }

    public synchronized Script loadFromResource(String resourcePath) {
        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SkriptRuntime.class.getClassLoader();
        }

        try (InputStream stream = classLoader.getResourceAsStream(normalized)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing script resource: " + normalized);
            }
            String source = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return loadScript(normalized, normalized, null, source);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read script resource: " + normalized, exception);
        }
    }

    public synchronized int dispatch(org.skriptlang.skript.lang.event.SkriptEvent event) {
        int executed = 0;
        for (Script script : scripts) {
            for (Structure structure : script.getStructures()) {
                if (!(structure instanceof ch.njol.skript.lang.SkriptEvent skriptEvent)) {
                    continue;
                }
                if (!skriptEvent.check(event)) {
                    continue;
                }
                Trigger trigger = skriptEvent.getTrigger();
                if (trigger == null) {
                    throw new IllegalStateException("Event trigger was not loaded for " + skriptEvent.getClass().getName());
                }
                boolean successful = trigger.execute(event);
                if (!successful) {
                    Throwable failure = TriggerItem.consumeExecutionFailure();
                    if (failure instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    if (failure instanceof Error error) {
                        throw error;
                    }
                    if (failure != null) {
                        throw new IllegalStateException(
                                "Trigger execution failed for " + trigger.getDebugLabel(),
                                failure
                        );
                    }
                    throw new IllegalStateException("Trigger execution failed for " + trigger.getDebugLabel());
                }
                executed++;
            }
        }
        return executed;
    }

    private Script loadScript(String scriptName, String fileName, @Nullable java.io.File file, String source) {
        SkriptFabricBootstrap.bootstrap();
        boolean wasEmpty = scripts.isEmpty();

        SectionNode root = parseScript(source, scriptName);
        Config config = new Config(stripExtension(scriptName), fileName, file);
        List<Structure> structures = new ArrayList<>();
        Script script = new Script(config, structures);

        ParserInstance parser = ParserInstance.get();
        Script previousScript = parser.getCurrentScript();
        Node previousNode = parser.getNode();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        List<ch.njol.skript.lang.TriggerSection> previousSections = parser.getCurrentSections();

        try {
            parser.setCurrentScript(script);
            parser.deleteCurrentEvent();
            parser.setCurrentSections(new ArrayList<>());
            for (Node node : root) {
                parser.setNode(node);
                Structure structure = parseTopLevelStructure(node);
                structures.add(structure);
            }

            List<Structure> ordered = orderedStructures(structures);
            for (Structure structure : ordered) {
                if (!structure.preLoad()) {
                    throw new IllegalStateException("preLoad failed for " + structure.getClass().getName());
                }
            }
            for (Structure structure : ordered) {
                if (!structure.load()) {
                    throw new IllegalStateException("load failed for " + structure.getClass().getName());
                }
            }
            for (Structure structure : ordered) {
                if (!structure.postLoad()) {
                    throw new IllegalStateException("postLoad failed for " + structure.getClass().getName());
                }
            }
        } finally {
            parser.setCurrentSections(previousSections);
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
            parser.setNode(previousNode);
            parser.setCurrentScript(previousScript);
        }

        scripts.add(script);
        if (wasEmpty) {
            EvtSkript.onSkriptStart();
        }
        return script;
    }

    private Structure parseTopLevelStructure(Node node) {
        String key = node.getKey();
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Top-level script node must not be blank.");
        }

        String expression = ScriptLoader.replaceOptions(key.trim());
        if (!(node instanceof SectionNode sectionNode)) {
            throw new IllegalArgumentException("Stage 2 runtime currently supports section-backed top-level structures only: " + expression);
        }

        Structure structure = Structure.parse(expression, sectionNode, null);
        if (structure != null) {
            return structure;
        }

        ch.njol.skript.lang.SkriptEvent event = ch.njol.skript.lang.SkriptEvent.parse(expression, sectionNode, null);
        if (event == null) {
            throw new IllegalArgumentException("Failed to parse top-level structure or event: " + expression);
        }
        return event;
    }

    private List<Structure> orderedStructures(List<Structure> structures) {
        List<Structure> ordered = new ArrayList<>(structures);
        ordered.sort(Comparator.comparingInt(structure -> structure.getPriority().getPriority()));
        return ordered;
    }

    private void unloadLoadedScripts(List<Script> loadedScripts) {
        for (Script script : loadedScripts) {
            List<Structure> structures = orderedStructures(script.getStructures());
            for (int i = structures.size() - 1; i >= 0; i--) {
                structures.get(i).unload();
            }
            for (int i = structures.size() - 1; i >= 0; i--) {
                structures.get(i).postUnload();
            }
            script.invalidate();
        }
    }

    private SectionNode parseScript(String source, String scriptName) {
        SectionNode root = new SectionNode(scriptName);
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame(0, root));
        AtomicBoolean inBlockComment = new AtomicBoolean(false);

        Node previousNode = root;
        int previousIndent = 0;
        String[] lines = source.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);

        for (int index = 0; index < lines.length; index++) {
            String rawLine = lines[index];
            Node.LineSplit split = Node.splitLine(rawLine, inBlockComment);
            String value = split.value();
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            int indent = indentation(value);
            if (indent > previousIndent) {
                if (!(previousNode instanceof SectionNode sectionNode)) {
                    throw new IllegalArgumentException("Indented line without owning section at line " + (index + 1));
                }
                stack.push(new Frame(indent, sectionNode));
            } else if (indent < previousIndent) {
                while (stack.size() > 1 && indent < stack.peek().indent()) {
                    stack.pop();
                }
                if (indent != stack.peek().indent()) {
                    throw new IllegalArgumentException("Inconsistent indentation at line " + (index + 1));
                }
            }

            Node node;
            if (trimmed.endsWith(":")) {
                SectionNode sectionNode = new SectionNode(trimmed.substring(0, trimmed.length() - 1).trim());
                node = sectionNode;
            } else if (isOptionEntry(trimmed, stack)) {
                int separator = trimmed.indexOf(':');
                String key = trimmed.substring(0, separator).trim();
                String entryValue = trimmed.substring(separator + 1).trim();
                node = new EntryNode(key, entryValue);
            } else {
                node = new SimpleNode(trimmed);
            }
            node.setLine(index + 1);
            stack.peek().section().add(node);

            previousNode = node;
            previousIndent = indent;
        }

        return root;
    }

    private int indentation(String line) {
        int indent = 0;
        while (indent < line.length()) {
            char current = line.charAt(indent);
            if (current == ' ') {
                indent++;
                continue;
            }
            if (current == '\t') {
                indent += 4;
                continue;
            }
            break;
        }
        return indent;
    }

    private boolean isOptionEntry(String trimmed, Deque<Frame> stack) {
        if (!trimmed.contains(":")) {
            return false;
        }
        for (Frame frame : stack) {
            if ("options".equalsIgnoreCase(frame.section().getKey())) {
                return true;
            }
        }
        return false;
    }

    private String stripExtension(String scriptName) {
        int index = scriptName.lastIndexOf('.');
        if (index <= 0) {
            return scriptName;
        }
        return scriptName.substring(0, index);
    }

    private record Frame(int indent, SectionNode section) {
    }
}
