package ch.njol.skript.log;

public enum ErrorQuality {
    GENERIC(0),
    NOT_AN_EXPRESSION(1),
    SEMANTIC_ERROR(2);

    private final int priority;

    ErrorQuality(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
