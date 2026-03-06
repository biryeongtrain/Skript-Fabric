package ch.njol.skript.lang.simplification;

import ch.njol.skript.lang.SyntaxElement;

public interface Simplifiable<S extends SyntaxElement> {

    S simplify();
}
