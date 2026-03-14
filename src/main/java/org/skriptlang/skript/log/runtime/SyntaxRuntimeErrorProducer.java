package org.skriptlang.skript.log.runtime;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.SyntaxElement;
import org.jetbrains.annotations.NotNull;

/**
 * A runtime error producer intended for use with {@link SyntaxElement}s.
 */
public interface SyntaxRuntimeErrorProducer extends RuntimeErrorProducer {

	Node getNode();

	@Override
	default @NotNull ErrorSource getErrorSource() {
		return ErrorSource.fromNodeAndElement(getNode(), (SyntaxElement) this);
	}

}
