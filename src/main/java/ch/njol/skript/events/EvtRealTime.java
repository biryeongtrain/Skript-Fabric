package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.event.Event;

public class EvtRealTime extends SkriptEvent {

    static final Timer TIMER = new Timer("EvtRealTime-Tasks", true);

    private Literal<?> times;
    private final List<TimerTask> timerTasks = new ArrayList<>();
    private volatile boolean unloaded;

    public static synchronized void register() {
        if (EventSyntaxRegistry.isRegistered(EvtRealTime.class)) {
            return;
        }
        Skript.registerEvent(EvtRealTime.class, "at %times% [in] real time");
    }

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
        times = args[0];
        return true;
    }

    @Override
    public boolean postLoad() {
        for (Object rawTime : times.getArray(org.skriptlang.skript.lang.event.SkriptEvent.EMPTY)) {
            Time time;
            if (rawTime instanceof Time parsedTime) {
                time = parsedTime;
            } else if (rawTime != null) {
                time = Time.parse(rawTime.toString());
            } else {
                continue;
            }
            if (time == null) {
                continue;
            }
            schedule(time);
        }
        return true;
    }

    @Override
    public void unload() {
        unloaded = true;
        synchronized (timerTasks) {
            for (TimerTask task : timerTasks) {
                task.cancel();
            }
            timerTasks.clear();
        }
        TIMER.purge();
    }

    @Override
    public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?>[] getEventClasses() {
        return new Class<?>[]{RealTimeEvent.class};
    }

    @Override
    public boolean isEventPrioritySupported() {
        return false;
    }

    @Override
    public String toString(@Nullable org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "at " + times.toString(event, debug) + " in real time";
    }

    private void schedule(Time time) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (timerTasks) {
                    timerTasks.remove(this);
                }
                execute();
                if (!unloaded) {
                    schedule(time);
                }
            }
        };

        synchronized (timerTasks) {
            if (unloaded) {
                return;
            }
            timerTasks.add(task);
        }

        TIMER.schedule(task, Date.from(nextExecutionInstant(time, ZoneId.systemDefault(), Instant.now())));
    }

    private void execute() {
        if (unloaded) {
            return;
        }
        trigger.execute(new org.skriptlang.skript.lang.event.SkriptEvent(new RealTimeEvent(), null, null, null));
    }

    static Instant nextExecutionInstant(Time time, ZoneId zone, Instant now) {
        ZonedDateTime current = now.atZone(zone);
        ZonedDateTime candidate = current
                .withHour(time.getHour())
                .withMinute(time.getMinute())
                .withSecond(0)
                .withNano(0);
        if (!candidate.isAfter(current)) {
            candidate = candidate.plusDays(1);
        }
        return candidate.toInstant();
    }

    static long initialDelayMillis(Time time, ZoneId zone, Instant now) {
        return Duration.between(now, nextExecutionInstant(time, zone, now)).toMillis();
    }

    public static final class RealTimeEvent implements Event {
    }
}
