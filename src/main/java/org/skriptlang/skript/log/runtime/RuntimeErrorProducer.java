package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * A RuntimeErrorProducer can throw runtime errors in a standardized and controlled manner.
 */
public interface RuntimeErrorProducer {

	@Contract(" -> new")
	@NotNull ErrorSource getErrorSource();

	default void error(String message) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.SEVERE, getErrorSource(), message, null)
		);
	}

	default void error(String message, String highlight) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.SEVERE, getErrorSource(), message, highlight)
		);
	}

	default void warning(String message) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.WARNING, getErrorSource(), message, null)
		);
	}

	default void warning(String message, String highlight) {
		getRuntimeErrorManager().error(
			new RuntimeError(Level.WARNING, getErrorSource(), message, highlight)
		);
	}

	default RuntimeErrorManager getRuntimeErrorManager() {
		return Skript.getRuntimeErrorManager();
	}

}
