package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricScheduledTickHandle;

public class EvtPeriodical extends SkriptEvent {

    private long periodTicks;
    private @Nullable Literal<ServerLevel> worlds;
    private long lastGlobalTrigger = Long.MIN_VALUE;
    private final java.util.Map<ServerLevel, Long> lastWorldTriggers = new java.util.WeakHashMap<>();

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtPeriodical.class)) {
            return;
        }
        Skript.registerEvent(
                EvtPeriodical.class,
                "every %timespan%",
                "every %timespan% in [world[s]] %worlds%"
        );
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        Object rawPeriod = args[0].getSingle(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY);
        if (!(rawPeriod instanceof Timespan period)) {
            return false;
        }
        periodTicks = Math.max(period.getAs(Timespan.TimePeriod.TICK), 1L);
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
            if (handle.level() != null) {
                return false;
            }
            if (handle.tick() <= 0 || handle.tick() % periodTicks != 0 || handle.tick() == lastGlobalTrigger) {
                return false;
            }
            lastGlobalTrigger = handle.tick();
            return true;
        }
        if (handle.level() == null || !worlds.check(event, world -> world == handle.level())) {
            return false;
        }
        long lastTrigger = lastWorldTriggers.getOrDefault(handle.level(), Long.MIN_VALUE);
        if (handle.tick() <= 0 || handle.tick() % periodTicks != 0 || handle.tick() == lastTrigger) {
            return false;
        }
        lastWorldTriggers.put(handle.level(), handle.tick());
        return true;
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
        return "every " + periodTicks + " tick" + (periodTicks == 1 ? "" : "s")
                + (worlds == null ? "" : " in " + worlds.toString(event, debug));
    }
}
