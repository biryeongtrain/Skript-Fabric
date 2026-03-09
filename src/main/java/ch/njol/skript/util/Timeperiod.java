package ch.njol.skript.util;

import org.jetbrains.annotations.Nullable;

public class Timeperiod {

    public final int start;
    public final int end;

    public Timeperiod() {
        start = 0;
        end = 0;
    }

    public Timeperiod(int start, int end) {
        this.start = Math.floorMod(start, 24000);
        this.end = Math.floorMod(end, 24000);
    }

    public Timeperiod(int time) {
        start = Math.floorMod(time, 24000);
        end = start;
    }

    public boolean contains(int time) {
        return start <= end ? time >= start && time <= end : time <= end || time >= start;
    }

    public boolean contains(Time time) {
        return contains(time.getTicks());
    }

    @Override
    public String toString() {
        return Time.toString(start) + (start == end ? "" : "-" + Time.toString(end));
    }

    @Override
    public int hashCode() {
        return start + (end << 16);
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Timeperiod other)) {
            return false;
        }
        return start == other.start && end == other.end;
    }
}
