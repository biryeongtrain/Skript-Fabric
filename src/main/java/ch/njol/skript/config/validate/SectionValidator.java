package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.SkriptLogger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class SectionValidator implements NodeValidator {

    private static final class NodeInfo {
        private final NodeValidator validator;
        private final boolean optional;

        private NodeInfo(NodeValidator validator, boolean optional) {
            this.validator = validator;
            this.optional = optional;
        }
    }

    private final HashMap<String, NodeInfo> nodes = new HashMap<>();
    private boolean allowUndefinedSections;
    private boolean allowUndefinedEntries;

    public SectionValidator addNode(String name, NodeValidator validator, boolean optional) {
        nodes.put(name.toLowerCase(Locale.ENGLISH), new NodeInfo(validator, optional));
        return this;
    }

    public SectionValidator addEntry(String name, boolean optional) {
        return addNode(name, new EntryValidator(), optional);
    }

    public SectionValidator addEntry(String name, Consumer<String> setter, boolean optional) {
        return addNode(name, new EntryValidator(setter), optional);
    }

    public <T> SectionValidator addEntry(
            String name,
            Parser<? extends T> parser,
            Consumer<T> setter,
            boolean optional
    ) {
        return addNode(name, new ParsedEntryValidator<>(parser, setter), optional);
    }

    public SectionValidator addSection(String name, boolean optional) {
        return addNode(
                name,
                new SectionValidator().setAllowUndefinedEntries(true).setAllowUndefinedSections(true),
                optional
        );
    }

    @Override
    public boolean validate(Node node) {
        if (!(node instanceof SectionNode sectionNode)) {
            notASectionError(node);
            return false;
        }
        boolean ok = true;
        for (Map.Entry<String, NodeInfo> entry : nodes.entrySet()) {
            Node child = sectionNode.get(entry.getKey());
            if (child == null && !entry.getValue().optional) {
                Skript.error("Required entry '" + entry.getKey() + "' is missing in "
                        + (node.getParent() == null
                        ? node.getConfig().getFileName()
                        : "'" + node.getKey() + "' (" + node.getConfig().getFileName()
                        + ", starting at line " + node.getLine() + ")"));
                ok = false;
            } else if (child != null) {
                ok &= entry.getValue().validator.validate(child);
            }
        }
        SkriptLogger.setNode(null);
        if (allowUndefinedSections && allowUndefinedEntries) {
            return ok;
        }
        for (Node child : sectionNode) {
            String key = child.getKey();
            if (key != null && !nodes.containsKey(key.toLowerCase(Locale.ENGLISH))) {
                if ((child instanceof SectionNode && allowUndefinedSections)
                        || (child instanceof EntryNode && allowUndefinedEntries)) {
                    continue;
                }
                SkriptLogger.setNode(child);
                Skript.error("Unexpected entry '" + child.getKey()
                        + "'. Check whether it's spelled correctly or remove it.");
                ok = false;
            }
        }
        SkriptLogger.setNode(null);
        return ok;
    }

    public static void notASectionError(Node node) {
        SkriptLogger.setNode(node);
        Skript.error("'" + node.getKey()
                + "' is not a section (like 'name:', followed by one or more indented lines)");
    }

    public SectionValidator setAllowUndefinedSections(boolean allowUndefinedSections) {
        this.allowUndefinedSections = allowUndefinedSections;
        return this;
    }

    public SectionValidator setAllowUndefinedEntries(boolean allowUndefinedEntries) {
        this.allowUndefinedEntries = allowUndefinedEntries;
        return this;
    }
}
