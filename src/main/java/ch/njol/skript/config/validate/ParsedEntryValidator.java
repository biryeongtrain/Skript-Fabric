package ch.njol.skript.config.validate;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.lang.ParseContext;
import java.util.function.Consumer;

public class ParsedEntryValidator<T> extends EntryValidator {

    private final Parser<? extends T> parser;
    private final Consumer<T> setter;

    public ParsedEntryValidator(Parser<? extends T> parser, Consumer<T> setter) {
        this.parser = parser;
        this.setter = setter;
    }

    @Override
    public boolean validate(Node node) {
        if (!super.validate(node)) {
            return false;
        }
        T parsed = parser.parse(((EntryNode) node).getValue(), ParseContext.CONFIG);
        if (parsed == null) {
            return false;
        }
        setter.accept(parsed);
        return true;
    }
}
