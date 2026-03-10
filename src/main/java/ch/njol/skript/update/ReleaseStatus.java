package ch.njol.skript.update;

/**
 * Status of currently installed release.
 */
public enum ReleaseStatus {
    LATEST("latest"),
    OUTDATED("outdated"),
    UNKNOWN("unknown"),
    CUSTOM("custom"),
    DEVELOPMENT("development");

    private final String name;

    ReleaseStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
