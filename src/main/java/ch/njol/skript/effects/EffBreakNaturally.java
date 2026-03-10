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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Break Block")
@Description({"Breaks the block and spawns items as if a player had mined it",
        "\nYou can add a tool, which will spawn items based on how that tool would break the block ",
        "(ie: When using a hand to break stone, it drops nothing, whereas with a pickaxe it drops cobblestone)"})
@Example("""
        on right click:
            break clicked block naturally
        """)
@Example("""
        loop blocks in radius 10 around player:
            break loop-block using player's tool
        """)
@Example("""
        loop blocks in radius 10 around player:
            break loop-block naturally using diamond pickaxe
        """)
@Since("2.4")
public final class EffBreakNaturally extends Effect {

    private static boolean registered;

    private Expression<FabricBlock> blocks;
    private @Nullable Expression<FabricItemType> tool;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffBreakNaturally.class, "break %blocks% [naturally] [using %-itemtype%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        tool = (Expression<FabricItemType>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricItemType toolType = tool == null ? null : tool.getSingle(event);
        ItemStack toolStack = toolType == null ? ItemStack.EMPTY : toolType.toStack();
        for (FabricBlock block : blocks.getArray(event)) {
            var state = block.state();
            BlockEntity blockEntity = block.level().getBlockEntity(block.position());
            Block.dropResources(state, block.level(), block.position(), blockEntity, null, toolStack);
            block.level().destroyBlock(block.position(), false);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "break " + blocks.toString(event, debug) + " naturally"
                + (tool != null ? " using " + tool.toString(event, debug) : "");
    }
}
