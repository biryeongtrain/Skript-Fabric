package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetTestBlockAtBlock extends Effect {

    private Expression<FabricBlock> blockExpression;
    private Expression<String> blockId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        blockExpression = (Expression<FabricBlock>) expressions[0];
        blockId = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricBlock target = blockExpression.getSingle(event);
        String blockKey = blockId.getSingle(event);
        if (target == null || blockKey == null || blockKey.isBlank()) {
            throw new IllegalStateException("set test block at block effect received incomplete block target or block id.");
        }

        var resourceLocation = MinecraftResourceParser.parse(blockKey);
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        target.level().setBlockAndUpdate(target.position(), block.defaultBlockState());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return "set test block at block";
    }
}
