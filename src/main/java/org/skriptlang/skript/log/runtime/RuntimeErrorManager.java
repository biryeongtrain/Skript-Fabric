package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.log.runtime.Frame.FrameLimit;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Handles passing runtime errors between producers and consumers via a frame collection system.
 * Simplified Fabric port — uses ScheduledExecutorService instead of Bukkit Task.
 */
public class RuntimeErrorManager implements Closeable {

	private static RuntimeErrorManager instance;

	@ApiStatus.Internal
	public static RuntimeErrorManager getInstance() {
		return instance;
	}

	static RuntimeErrorFilter standardFilter;

	/**
	 * Initializes or refreshes the runtime error manager with default limits.
	 */
	public static void refresh() {
		// Default frame length: 100 ticks (5 seconds at 20 TPS)
		long frameLengthMs = 5000;
		if (instance == null) {
			instance = new RuntimeErrorManager(frameLengthMs);
		} else {
			var oldMap = instance.filterMap;
			instance = new RuntimeErrorManager(frameLengthMs);
			instance.filterMap.putAll(oldMap);
		}

		// Default limits
		FrameLimit errorLimits = new FrameLimit(50, 10, 20, 5);
		FrameLimit warningLimits = new FrameLimit(50, 10, 20, 5);

		if (standardFilter == null) {
			standardFilter = new RuntimeErrorFilter(errorLimits, warningLimits);
		} else {
			standardFilter.setErrorFrameLimits(errorLimits);
			standardFilter.setWarningFrameLimits(warningLimits);
		}
	}

	private final ScheduledExecutorService scheduler;
	private final ScheduledFuture<?> scheduledTask;
	private final Map<RuntimeErrorFilter, Set<RuntimeErrorConsumer>> filterMap = new ConcurrentHashMap<>();

	public RuntimeErrorManager(long frameLengthMs) {
		scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "Skript-RuntimeError-Frame");
			t.setDaemon(true);
			return t;
		});
		scheduledTask = scheduler.scheduleAtFixedRate(() -> {
			for (var entry : filterMap.entrySet()) {
				RuntimeErrorFilter filter = entry.getKey();
				if (filter == null)
					continue;
				Set<RuntimeErrorConsumer> consumers = entry.getValue();

				Frame errorFrame = filter.getErrorFrame();
				consumers.forEach(consumer -> consumer.printFrameOutput(errorFrame.getFrameOutput(), Level.SEVERE));
				errorFrame.nextFrame();

				Frame warningFrame = filter.getWarningFrame();
				consumers.forEach(consumer -> consumer.printFrameOutput(warningFrame.getFrameOutput(), Level.WARNING));
				warningFrame.nextFrame();
			}
		}, frameLengthMs, frameLengthMs, TimeUnit.MILLISECONDS);
	}

	public void error(@NotNull RuntimeError error) {
		for (var entry : filterMap.entrySet()) {
			RuntimeErrorFilter filter = entry.getKey();
			Set<RuntimeErrorConsumer> consumers = entry.getValue();
			if (filter == null || filter.test(error)) {
				consumers.forEach(consumer -> consumer.printError(error));
			}
		}
	}

	public void addConsumer(RuntimeErrorConsumer consumer) {
		synchronized (filterMap) {
			filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
		}
	}

	public void addConsumers(RuntimeErrorConsumer... newConsumers) {
		synchronized (filterMap) {
			for (var consumer : newConsumers) {
				filterMap.computeIfAbsent(consumer.getFilter(), key -> new HashSet<>()).add(consumer);
			}
		}
	}

	public boolean removeConsumer(RuntimeErrorConsumer consumer) {
		synchronized (filterMap) {
			var set = filterMap.get(consumer.getFilter());
			if (set == null)
				return false;
			boolean removed = set.remove(consumer);
			if (set.isEmpty())
				filterMap.remove(consumer.getFilter());
			return removed;
		}
	}

	public List<RuntimeErrorConsumer> removeAllConsumers() {
		synchronized (filterMap) {
			List<RuntimeErrorConsumer> currentConsumers = new ArrayList<>();
			for (var set : filterMap.values())
				currentConsumers.addAll(set);
			filterMap.clear();
			return currentConsumers;
		}
	}

	@Override
	public void close() {
		scheduledTask.cancel(false);
		scheduler.shutdown();
	}

}
