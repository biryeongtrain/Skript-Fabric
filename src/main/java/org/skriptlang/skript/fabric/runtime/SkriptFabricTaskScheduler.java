package org.skriptlang.skript.fabric.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.server.MinecraftServer;

public final class SkriptFabricTaskScheduler {

    private static final Map<MinecraftServer, List<ScheduledTask>> TASKS = new WeakHashMap<>();
    private static final Map<Integer, RepeatingTask> REPEATING_TASKS = new ConcurrentHashMap<>();
    private static final AtomicInteger NEXT_REPEATING_ID = new AtomicInteger();

    private SkriptFabricTaskScheduler() {
    }

    public static void schedule(MinecraftServer server, long delayTicks, Runnable action) {
        long dueTick = server.overworld().getGameTime() + Math.max(delayTicks, 1L);
        synchronized (TASKS) {
            TASKS.computeIfAbsent(server, ignored -> new ArrayList<>()).add(new ScheduledTask(dueTick, action));
        }
    }

    /**
     * Schedules a repeating task that runs every {@code intervalTicks} server ticks.
     * The task starts running on the next server tick after registration.
     *
     * @return a task ID that can be passed to {@link #cancelRepeating(int)}
     */
    public static int scheduleRepeating(long intervalTicks, Runnable action) {
        int id = NEXT_REPEATING_ID.incrementAndGet();
        REPEATING_TASKS.put(id, new RepeatingTask(Math.max(intervalTicks, 1L), action, new AtomicLong(0)));
        return id;
    }

    public static void cancelRepeating(int id) {
        REPEATING_TASKS.remove(id);
    }

    static void tick(MinecraftServer server) {
        long currentTick = server.overworld().getGameTime();

        // Tick repeating tasks
        for (var entry : REPEATING_TASKS.entrySet()) {
            RepeatingTask task = entry.getValue();
            if (currentTick - task.lastRun().get() >= task.intervalTicks()) {
                task.lastRun().set(currentTick);
                try {
                    task.action().run();
                } catch (Exception e) {
                    // Remove broken repeating tasks to avoid spamming errors
                    REPEATING_TASKS.remove(entry.getKey());
                }
            }
        }

        // Tick one-shot tasks
        List<ScheduledTask> due = new ArrayList<>();
        synchronized (TASKS) {
            List<ScheduledTask> tasks = TASKS.get(server);
            if (tasks == null || tasks.isEmpty()) {
                return;
            }
            tasks.sort(Comparator.comparingLong(ScheduledTask::dueTick));
            tasks.removeIf(task -> {
                if (task.dueTick() <= currentTick) {
                    due.add(task);
                    return true;
                }
                return false;
            });
            if (tasks.isEmpty()) {
                TASKS.remove(server);
            }
        }
        for (ScheduledTask task : due) {
            task.action().run();
        }
    }

    private record ScheduledTask(long dueTick, Runnable action) {
    }

    private record RepeatingTask(long intervalTicks, Runnable action, AtomicLong lastRun) {
    }
}
