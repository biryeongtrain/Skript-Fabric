package ch.njol.skript.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class Task implements Runnable {

    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "skript-task");
            thread.setDaemon(true);
            return thread;
        }
    });

    private final ScheduledFuture<?> future;

    protected Task(Object owner, long delayTicks, long periodTicks, boolean async) {
        long delayMillis = ticksToMillis(delayTicks);
        long periodMillis = ticksToMillis(periodTicks);
        future = EXECUTOR.scheduleAtFixedRate(this, delayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public final void cancel() {
        future.cancel(false);
    }

    private static long ticksToMillis(long ticks) {
        return Math.max(1L, ticks) * 50L;
    }
}
