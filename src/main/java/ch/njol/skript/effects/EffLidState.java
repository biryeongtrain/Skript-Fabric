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
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Open/Close Lid")
@Description("Open or close the lid of the block(s).")
@Example("open the lid of {_chest}")
@Example("close the lid of {_blocks::*}")
@Since("2.10")
public final class EffLidState extends Effect {

    private static boolean registered;

    private boolean setOpen;
    private Expression<FabricBlock> blocks;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffLidState.class,
                "(open|:close) [the] lid[s] (of|for) %blocks%",
                "(open|:close) %blocks%'[s] lid[s]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setOpen = !parseResult.hasTag("close");
        blocks = (Expression<FabricBlock>) exprs[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricBlock block : blocks.getArray(event)) {
            var state = block.state();
            block.level().blockEvent(block.position(), state.getBlock(), 1, setOpen ? 1 : 0);
            block.level().sendBlockUpdated(block.position(), state, state, 3);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (setOpen ? "open" : "close") + " lid of " + blocks.toString(event, debug);
    }
}
