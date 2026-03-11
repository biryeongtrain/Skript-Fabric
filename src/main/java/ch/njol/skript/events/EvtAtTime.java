package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricScheduledTickHandle;

public class EvtAtTime extends SkriptEvent {

    private static final int TICKS_PER_DAY = 24000;

    private int targetTime;
    private @Nullable Literal<ServerLevel> worlds;
    private long lastGlobalCheck = Long.MIN_VALUE;
    private final java.util.Map<ServerLevel, Long> lastWorldChecks = new java.util.WeakHashMap<>();

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtAtTime.class)) {
            return;
        }
        Skript.registerEvent(EvtAtTime.class, "at %time% [in %-worlds%]");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        Object rawTime = args[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY);
        Time time;
        if (rawTime instanceof Time parsedTime) {
            time = parsedTime;
        } else if (rawTime != null) {
            time = Time.parse(rawTime.toString());
        } else {
            time = null;
        }
        if (time == null) {
            return false;
        }
        targetTime = time.getTicks();
        if (args.length > 1 && args[1] != null) {
            @SuppressWarnings("unchecked")
            Literal<ServerLevel> literalWorlds = (Literal<ServerLevel>) args[1];
            worlds = literalWorlds;
        } else {
            worlds = null;
        }
        return true;
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof FabricScheduledTickHandle handle)) {
            return false;
        }
        if (worlds == null) {
            if (handle.level() == null) {
                return event.server() == null && crossedTarget(lastGlobalCheck, handle.dayTime(), true, null);
            }
            return crossedTarget(lastWorldChecks.getOrDefault(handle.level(), Long.MIN_VALUE), handle.dayTime(), false, handle.level());
        }
        if (handle.level() == null || !worlds.check(event, world -> world == handle.level())) {
            return false;
        }
        return crossedTarget(lastWorldChecks.getOrDefault(handle.level(), Long.MIN_VALUE), handle.dayTime(), false, handle.level());
    }

    private boolean crossedTarget(long previousCheck, long currentDayTime, boolean global, @Nullable ServerLevel level) {
        long current = Math.floorMod(currentDayTime, TICKS_PER_DAY);
        boolean matches;
        if (previousCheck == Long.MIN_VALUE) {
            matches = current == targetTime;
        } else {
            long previous = Math.floorMod(previousCheck, TICKS_PER_DAY);
            if (previous == current) {
                matches = false;
            } else if (previous < current) {
                matches = previous < targetTime && targetTime <= current;
            } else {
                matches = targetTime > previous || targetTime <= current;
            }
        }
        if (global) {
            lastGlobalCheck = current;
        } else if (level != null) {
            lastWorldChecks.put(level, current);
        }
        return matches;
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{FabricScheduledTickHandle.class};
    }

    @Override
    public boolean isEventPrioritySupported() {
        return false;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "at " + Time.toString(targetTime) + (worlds == null ? "" : " in " + worlds.toString(event, debug));
    }
}
