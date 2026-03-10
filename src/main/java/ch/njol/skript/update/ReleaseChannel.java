package ch.njol.skript.update;

import java.util.function.Function;

/**
 * Allows checking whether releases are in this channel or not.
 */
public class ReleaseChannel {

    private final Function<String, Boolean> checker;
    private final String name;

    public ReleaseChannel(Function<String, Boolean> checker, String name) {
        this.checker = checker;
        this.name = name;
    }

    public boolean check(String release) {
        return checker.apply(release);
    }

    public String getName() {
        return name;
    }
}
