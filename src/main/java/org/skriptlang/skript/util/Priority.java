package org.skriptlang.skript.util;

public final class Priority implements Comparable<Priority> {

    private final int value;

    private Priority(int value) {
        this.value = value;
    }

    public static Priority base() {
        return new Priority(1000);
    }

    public static Priority before(Priority other) {
        return new Priority(other.value - 1);
    }

    public static Priority after(Priority other) {
        return new Priority(other.value + 1);
    }

    public int value() {
        return value;
    }

    @Override
    public int compareTo(Priority other) {
        return Integer.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Priority other)) {
            return false;
        }
        return value == other.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
