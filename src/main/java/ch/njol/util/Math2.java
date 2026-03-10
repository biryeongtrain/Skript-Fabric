package ch.njol.util;

import ch.njol.skript.Skript;

public final class Math2 {

    private Math2() {
    }

    public static double fit(double min, double value, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static long floor(double value) {
        return (long) Math.floor(value + Skript.EPSILON);
    }

    public static long ceil(double value) {
        return (long) Math.ceil(value - Skript.EPSILON);
    }

    public static long round(double value) {
        return Math.round(value + Skript.EPSILON);
    }
}
