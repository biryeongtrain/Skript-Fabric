package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Wake And Sleep")
@Description({
        "Make bats and foxes sleep or wake up.",
        "Make villagers or players sleep by providing a bed location."
})
@Example("make {_fox} go to sleep")
@Example("make player wake up without spawn location update")
@Since("2.11")
public class EffWakeupSleep extends Effect {

    private static boolean registered;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffWakeupSleep.class,
                "make %livingentities% (start sleeping|[go to] sleep) [%-direction% %-location%]",
                "force %livingentities% to (start sleeping|[go to] sleep) [%-direction% %-location%]",
                "make %players% (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
                "force %players% to (start sleeping|[go to] sleep) %direction% %location% (force:with force)",
                "make %livingentities% (stop sleeping|wake up)",
                "force %livingentities% to (stop sleeping|wake up)",
                "make %players% (stop sleeping|wake up) (spawn:without spawn [location] update)",
                "force %players% to (stop sleeping|wake up) (spawn:without spawn [location] update)"
        );
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Skript.error("EffWakeupSleep is blocked in the Fabric port until Direction compatibility is imported.");
        return false;
    }

    @Override
    protected void execute(SkriptEvent event) {
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "sleep/wake";
    }
}
