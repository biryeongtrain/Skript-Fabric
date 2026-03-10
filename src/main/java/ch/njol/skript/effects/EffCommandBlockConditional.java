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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Conditional / Unconditional")
@Description(
        "Sets whether the provided command blocks are conditional or not."
)
@Example("make command block {_block} conditional")
@Example("make command block {_block} unconditional if {_block} is conditional")
@Since("2.10")
public class EffCommandBlockConditional extends Effect {

    private static boolean registered;

    private Expression<FabricBlock> blocks;
    private boolean conditional;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffCommandBlockConditional.class, "make command block[s] %blocks% [not:(un|not )]conditional");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        conditional = !parseResult.hasTag("not");
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (FabricBlock block : blocks.getArray(event)) {
            BlockState state = block.state();
            if (state.hasProperty(BlockStateProperties.CONDITIONAL)) {
                block.level().setBlock(block.position(), state.setValue(BlockStateProperties.CONDITIONAL, conditional), 3);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make command block " + blocks.toString(event, debug) + (conditional ? " " : " un") + "conditional";
    }
}
