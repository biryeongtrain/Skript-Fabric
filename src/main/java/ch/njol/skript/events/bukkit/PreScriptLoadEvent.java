package ch.njol.skript.events.bukkit;

import ch.njol.skript.config.Config;
import java.util.List;
import org.skriptlang.skript.util.event.Event;

public class PreScriptLoadEvent implements Event {

    private final List<Config> scripts;

    public PreScriptLoadEvent(List<Config> scripts) {
        this.scripts = List.copyOf(scripts);
    }

    public List<Config> getScripts() {
        return scripts;
    }
}
