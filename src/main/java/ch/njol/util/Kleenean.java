package ch.njol.util;

public enum Kleenean {
    TRUE,
    FALSE,
    UNKNOWN;

    public boolean isTrue() {
        return this == TRUE;
    }

    public boolean isFalse() {
        return this == FALSE;
    }

    public Kleenean and(Kleenean other) {
        if (this == FALSE || other == FALSE) {
            return FALSE;
        }
        if (this == TRUE && other == TRUE) {
            return TRUE;
        }
        return UNKNOWN;
    }

    public Kleenean or(Kleenean other) {
        if (this == TRUE || other == TRUE) {
            return TRUE;
        }
        if (this == FALSE && other == FALSE) {
            return FALSE;
        }
        return UNKNOWN;
    }

    public Kleenean not() {
        return switch (this) {
            case TRUE -> FALSE;
            case FALSE -> TRUE;
            case UNKNOWN -> UNKNOWN;
        };
    }

    public static Kleenean get(boolean value) {
        return value ? TRUE : FALSE;
    }
}
