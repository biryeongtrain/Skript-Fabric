package ch.njol.skript.effects;

import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class IndeterminateDelay extends Delay {

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "wait for operation to finish";
    }
}
