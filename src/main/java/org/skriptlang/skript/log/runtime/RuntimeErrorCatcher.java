package org.skriptlang.skript.log.runtime;

import ch.njol.skript.Skript;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.log.runtime.Frame.FrameOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link RuntimeErrorConsumer} to catch {@link RuntimeError}s programmatically.
 */
public class RuntimeErrorCatcher implements RuntimeErrorConsumer, AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger("Skript");

	private List<RuntimeErrorConsumer> storedConsumers = new ArrayList<>();

	private final List<RuntimeError> cachedErrors = new ArrayList<>();

	private static final int ERROR_LIMIT = 1000;

	private boolean stopped = false;

	public RuntimeErrorCatcher() {}

	private RuntimeErrorManager getManager() {
		return Skript.getRuntimeErrorManager();
	}

	@Override
	public @Nullable RuntimeErrorFilter getFilter() {
		return RuntimeErrorFilter.NO_FILTER;
	}

	public RuntimeErrorCatcher start() {
		stopped = false;
		storedConsumers = getManager().removeAllConsumers();
		getManager().addConsumer(this);
		return this;
	}

	public void stop() {
		if (stopped)
			return;
		stopped = true;
		if (!getManager().removeConsumer(this)) {
			LOGGER.severe("[Skript] A 'RuntimeErrorCatcher' was stopped incorrectly.");
			return;
		}
		getManager().addConsumers(storedConsumers.toArray(RuntimeErrorConsumer[]::new));
	}

	public @UnmodifiableView List<RuntimeError> getCachedErrors() {
		return Collections.unmodifiableList(cachedErrors);
	}

	public RuntimeErrorCatcher clearCachedErrors() {
		cachedErrors.clear();
		return this;
	}

	@Override
	public void printError(RuntimeError error) {
		if (cachedErrors.size() < ERROR_LIMIT)
			cachedErrors.add(error);
	}

	@Override
	public void printFrameOutput(FrameOutput output, Level level) {
		// no-op — catcher has no filter so this won't be called
	}

	@Override
	public void close() {
		this.clearCachedErrors().stop();
	}

}
