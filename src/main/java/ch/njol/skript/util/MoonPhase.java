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
        int phase = level.getMoonPhase();
        return values()[Math.floorMod(phase, values().length)];
    }
}
