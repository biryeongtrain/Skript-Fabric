package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Lid Is Open/Closed")
@Description("Check to see whether lidded blocks (chests, shulkers, etc.) are open or closed.")
@Example("""
        if the lid of {_chest} is closed:
            open the lid of {_block}
        """)
@Since("2.10")
public class CondLidState extends PropertyCondition<FabricBlock> {

    static {
        Skript.registerCondition(CondLidState.class,
                "[the] lid[s] of %blocks% (is|are) (open[ed]|:close[d])",
                "[the] lid[s] of %blocks% (isn't|is not|aren't|are not) (open[ed]|:close[d])",
                "%blocks%'[s] lid[s] (is|are) (open[ed]|:close[d])",
                "%blocks%'[s] lid[s] (isn't|is not|aren't|are not) (open[ed]|:close[d])"
        );
    }

    private boolean checkOpen;
    private Expression<FabricBlock> blocks;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        checkOpen = !parseResult.hasTag("close");
        blocks = (Expression<FabricBlock>) exprs[0];
        setExpr(blocks);
        setNegated(matchedPattern == 1 || matchedPattern == 3);
        return true;
    }

    @Override
    public boolean check(FabricBlock block) {
        return block.state().hasProperty(BlockStateProperties.OPEN)
                && block.state().getValue(BlockStateProperties.OPEN) == checkOpen;
    }

    @Override
    protected String getPropertyName() {
        return (checkOpen ? "opened" : "closed") + " lid state";
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the lids of " + blocks.toString(event, debug) + (isNegated() ? " are not " : " are ") + (checkOpen ? "opened" : "closed");
    }
}
