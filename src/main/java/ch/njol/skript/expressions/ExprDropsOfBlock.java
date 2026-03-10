package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprDropsOfBlock extends SimpleExpression<FabricItemType> {

    static {
        Skript.registerExpression(
                ExprDropsOfBlock.class,
                FabricItemType.class,
                "[(all|the|all [of] the)] drops of %blocks% [(using|with) %-itemtype% [(1¦as %-entity%)]]",
                "%blocks%'s drops [(using|with) %-itemtype% [(1¦as %-entity%)]]"
        );
    }

    private Expression<FabricBlock> blocks;
    private @Nullable Expression<FabricItemType> tool;
    private @Nullable Expression<Entity> entity;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        blocks = (Expression<FabricBlock>) exprs[0];
        tool = (Expression<FabricItemType>) exprs[1];
        entity = (Expression<Entity>) exprs[2];
        return true;
    }

    @Override
    protected FabricItemType @Nullable [] get(SkriptEvent event) {
        List<FabricItemType> values = new ArrayList<>();
        for (FabricBlock block : blocks.getArray(event)) {
            if (block.level() == null) {
                continue;
            }
            ServerLevel level = block.level();
            ItemStack stack = tool == null || tool.getSingle(event) == null ? ItemStack.EMPTY : tool.getSingle(event).toStack();
            Entity breaker = entity == null ? null : entity.getSingle(event);
            BlockEntity blockEntity = level.getBlockEntity(block.position());
            List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                    block.state(),
                    level,
                    block.position(),
                    blockEntity,
                    breaker,
                    stack
            );
            for (ItemStack drop : drops) {
                values.add(new FabricItemType(drop));
            }
        }
        return values.toArray(FabricItemType[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "drops of " + blocks.toString(event, debug);
    }
}
