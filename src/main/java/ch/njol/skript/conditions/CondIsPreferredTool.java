package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Is Preferred Tool")
@Description(
        "Checks whether an item is the preferred tool for a block. A preferred tool is one that will drop the block's item "
                + "when used."
)
@Example("""
    on left click:
        event-block is set
        if player's tool is the preferred tool for event-block:
            break event-block naturally using player's tool
        else:
            cancel event
    """)
@Since("2.7")
public class CondIsPreferredTool extends Condition {

    static {
        Skript.registerCondition(CondIsPreferredTool.class,
                "%itemtypes% (is|are) %blocks%'s preferred tool[s]",
                "%itemtypes% (is|are) [the|a] preferred tool[s] (for|of) %blocks%",
                "%itemtypes% (is|are)(n't| not) %blocks%'s preferred tool[s]",
                "%itemtypes% (is|are)(n't| not) [the|a] preferred tool[s] (for|of) %blocks%"
        );
    }

    private Expression<FabricItemType> items;
    private Expression<?> blocks;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern >= 2);
        items = (Expression<FabricItemType>) exprs[0];
        blocks = exprs[1];
        return true;
    }

    @Override
    public boolean check(SkriptEvent event) {
        return blocks.check(event, block ->
                items.check(event, item -> {
                    ItemStack stack = item.toStack();
                    BlockState state = stateOf(block);
                    return state != null && stack.isCorrectToolForDrops(state);
                }), isNegated());
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return items.toString(event, debug) + " is the preferred tool for " + blocks.toString(event, debug);
    }

    private static @Nullable BlockState stateOf(Object block) {
        if (block instanceof FabricBlock fabricBlock) {
            return fabricBlock.state();
        }
        return block instanceof BlockState state ? state : null;
    }
}
