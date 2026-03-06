package ch.njol.skript.lang;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public interface Debuggable {

    String toString(@Nullable SkriptEvent event, boolean debug);
}
