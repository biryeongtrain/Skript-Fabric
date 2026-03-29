package ch.njol.skript.util;

import net.minecraft.server.level.ServerLevel;

public enum MoonPhase {
    FULL_MOON,
    WANING_GIBBOUS,
    LAST_QUARTER,
    WANING_CRESCENT,
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS;

    public static MoonPhase of(ServerLevel level) {
        long time = level.getDefaultClockTime();
        int phase = (int) (time / 24000L % 8L + 8L) % 8;
        return values()[phase];
    }
}
