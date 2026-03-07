package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprBrewingFuelLevel extends SimpleExpression<Integer> {

    private Expression<FabricBlock> blocks;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(FabricBlock.class)) {
            return false;
        }
        blocks = (Expression<FabricBlock>) expressions[0];
        return true;
    }

    @Override
    protected Integer @Nullable [] get(SkriptEvent event) {
        List<Integer> levels = new ArrayList<>();
        for (FabricBlock block : blocks.getAll(event)) {
            if (block.level().getBlockEntity(block.position()) instanceof BrewingStandBlockEntity brewingStand) {
                levels.add(PrivateBlockEntityAccess.brewingFuel(brewingStand));
            }
        }
        return levels.toArray(Integer[]::new);
    }

    @Override
    public boolean isSingle() {
        return blocks.isSingle();
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "brewing fuel amount of " + blocks.toString(event, debug);
    }
}
