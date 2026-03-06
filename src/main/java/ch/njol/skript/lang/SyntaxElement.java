package ch.njol.skript.lang;

import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;

public interface SyntaxElement extends Debuggable {

    default boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        return true;
    }

    default String getSyntaxTypeName() {
        return "syntax";
    }

    default ParserInstance getParser() {
        return ParserInstance.get();
    }
}
