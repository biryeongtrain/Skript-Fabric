package ch.njol.skript.lang;

import ch.njol.skript.config.Config;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * @deprecated Regular {@link org.skriptlang.skript.lang.structure.Structure} lifecycle methods should be used.
 */
@Deprecated(since = "2.7.0", forRemoval = true)
public abstract class SelfRegisteringSkriptEvent extends ch.njol.skript.lang.SkriptEvent {

    @Deprecated(since = "2.10.0", forRemoval = true)
    public abstract void register(Trigger trigger);

    @Deprecated(since = "2.10.0", forRemoval = true)
    public abstract void unregister(Trigger trigger);

    @Deprecated(since = "2.10.0", forRemoval = true)
    public abstract void unregisterAll();

    @Override
    public boolean load() {
        boolean load = super.load();
        if (load) {
            var script = getParser().getCurrentScript();
            if (script != null) {
                afterParse(script.getConfig());
            }
        }
        return load;
    }

    @Override
    public boolean postLoad() {
        register(trigger);
        return true;
    }

    @Override
    public void unload() {
        unregister(trigger);
    }

    @Override
    public final boolean check(SkriptEvent event) {
        throw new UnsupportedOperationException();
    }

    @Deprecated(since = "2.7.0", forRemoval = true)
    public void afterParse(Config config) {
    }

    @Override
    public boolean isEventPrioritySupported() {
        return false;
    }
}
