package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEggThrowEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Make Egg Hatch")
@Description("Makes the egg hatch in a Player Egg Throw event.")
@Example("""
        on player egg throw:
            # EGGS FOR DAYZ!
            make the egg hatch
        """)
@Events("Egg Throw")
@Since("2.7")
public class EffMakeEggHatch extends Effect {

    private static boolean registered;

    private boolean not;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffMakeEggHatch.class, "make [the] egg [:not] hatch");
        registered = true;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricEffectEventHandles.PlayerEggThrow.class)) {
            Skript.error("You can't use the 'make the egg hatch' effect outside of a Player Egg Throw event.");
            return false;
        }
        not = parseResult.hasTag("not");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEggThrowEventHandle eggThrow)) {
            return;
        }
        eggThrow.setHatching(!not);
        if (!not && eggThrow.hatches() == 0) {
            eggThrow.setHatches((byte) 1);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make the egg " + (not ? "not " : "") + "hatch";
    }
}
