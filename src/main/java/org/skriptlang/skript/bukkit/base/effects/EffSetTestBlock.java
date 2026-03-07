package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class EffSetTestBlock extends Effect {

    private Expression<Integer> x;
    private Expression<Integer> y;
    private Expression<Integer> z;
    private Expression<String> blockId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 4) {
            return false;
        }
        x = (Expression<Integer>) expressions[0];
        y = (Expression<Integer>) expressions[1];
        z = (Expression<Integer>) expressions[2];
        blockId = (Expression<String>) expressions[3];
        return true;
    }

    @Override
    protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        if (!(event.handle() instanceof GameTestHelper helper)) {
            throw new IllegalStateException("set test block effect requires a GameTestHelper event handle.");
        }

        Integer xValue = x.getSingle(event);
        Integer yValue = y.getSingle(event);
        Integer zValue = z.getSingle(event);
        String blockKey = blockId.getSingle(event);
        if (xValue == null || yValue == null || zValue == null || blockKey == null || blockKey.isBlank()) {
            throw new IllegalStateException("set test block effect received incomplete coordinates or block id.");
        }

        var resourceLocation = MinecraftResourceParser.parse(blockKey);
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        helper.setBlock(new BlockPos(xValue, yValue, zValue), block.defaultBlockState());
    }

    @Override
    public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "set test block";
    }
}
