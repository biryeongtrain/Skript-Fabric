package org.skriptlang.skript.fabric.runtime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;

public final class SkriptFabricTaskScheduler {

    private static final Map<MinecraftServer, List<ScheduledTask>> TASKS = new WeakHashMap<>();

    private SkriptFabricTaskScheduler() {
    }

    public static void schedule(MinecraftServer server, long delayTicks, Runnable action) {
        long dueTick = server.overworld().getGameTime() + Math.max(delayTicks, 1L);
        synchronized (TASKS) {
            TASKS.computeIfAbsent(server, ignored -> new ArrayList<>()).add(new ScheduledTask(dueTick, action));
        }
    }

    static void tick(MinecraftServer server) {
        List<ScheduledTask> due = new ArrayList<>();
        long currentTick = server.overworld().getGameTime();
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
}
