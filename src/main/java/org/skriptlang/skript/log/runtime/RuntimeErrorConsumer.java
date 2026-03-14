package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Consumes runtime errors.
 */
public interface RuntimeErrorConsumer {

	void printError(RuntimeError error);

	default @Nullable RuntimeErrorFilter getFilter() {
		return RuntimeErrorManager.standardFilter;
	}

	void printFrameOutput(Frame.FrameOutput output, Level level);

}
