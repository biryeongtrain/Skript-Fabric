package ch.njol.skript.config.validate;

import ch.njol.skript.Skript;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.log.SkriptLogger;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public class EntryValidator implements NodeValidator {

    private final @Nullable Consumer<String> setter;

    public EntryValidator() {
        this.setter = null;
    }

    public EntryValidator(Consumer<String> setter) {
        this.setter = setter;
    }

    @Override
    public boolean validate(Node node) {
        if (!(node instanceof EntryNode entryNode)) {
            notAnEntryError(node);
            return false;
        }
        if (setter != null) {
            setter.accept(entryNode.getValue());
        }
        return true;
    }

    public static void notAnEntryError(Node node) {
        notAnEntryError(node, node.getConfig().getSeparator());
    }

    public static void notAnEntryError(Node node, String separator) {
        SkriptLogger.setNode(node);
        Skript.error("'" + node.getKey() + "' is not an entry (like 'name " + separator + " value')");
    }
}
