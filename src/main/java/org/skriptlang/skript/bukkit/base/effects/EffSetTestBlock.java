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
import org.skriptlang.skript.fabric.runtime.GameTestRuntimeContext;

public final class EffSetTestBlock extends Effect {

    private Expression<Number> x;
    private Expression<Number> y;
    private Expression<Number> z;
    private Expression<String> blockId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 4) {
            return false;
        }
        x = (Expression<Number>) expressions[0];
        y = (Expression<Number>) expressions[1];
        z = (Expression<Number>) expressions[2];
        blockId = (Expression<String>) expressions[3];
        return true;
    }

    @Override
    protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        GameTestHelper helper = GameTestRuntimeContext.resolve(event);
        if (helper == null) {
            throw new IllegalStateException("set test block effect requires a GameTestHelper event handle.");
        }

        Number xValue = x.getSingle(event);
        Number yValue = y.getSingle(event);
        Number zValue = z.getSingle(event);
        String blockKey = blockId.getSingle(event);
        if (xValue == null || yValue == null || zValue == null || blockKey == null || blockKey.isBlank()) {
            throw new IllegalStateException(
                    "set test block effect received incomplete coordinates or block id: x=" + xValue
                            + ", y=" + yValue
                            + ", z=" + zValue
                            + ", blockId=" + blockKey
                            + ", blockExpr=" + (blockId == null ? "null" : blockId.getClass().getName())
                            + ", blockReturnType=" + (blockId == null ? "null" : blockId.getReturnType().getName())
            );
        }

        var resourceLocation = MinecraftResourceParser.parse(blockKey);
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        helper.setBlock(new BlockPos(xValue.intValue(), yValue.intValue(), zValue.intValue()), block.defaultBlockState());
    }

    @Override
    public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "set test block";
    }
}
