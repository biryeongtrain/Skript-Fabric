package ch.njol.skript.update;

/**
 * State of updater.
 */
public enum UpdaterState {
    NOT_STARTED,
    CHECKING,
    DOWNLOADING,
    INACTIVE,
    ERROR
}
