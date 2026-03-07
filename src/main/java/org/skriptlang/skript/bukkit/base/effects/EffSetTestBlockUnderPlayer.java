package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetTestBlockUnderPlayer extends Effect {

    private Expression<ServerPlayer> playerExpression;
    private Expression<String> blockId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        playerExpression = (Expression<ServerPlayer>) expressions[0];
        blockId = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        ServerPlayer player = playerExpression.getSingle(event);
        String blockKey = blockId.getSingle(event);
        if (player == null || blockKey == null || blockKey.isBlank()) {
            throw new IllegalStateException("set test block under player effect received incomplete player or block id.");
        }

        var resourceLocation = MinecraftResourceParser.parse(blockKey);
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        BlockPos target = player.blockPosition().below();
        ((ServerLevel) player.level()).setBlockAndUpdate(target, block.defaultBlockState());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return "set test block under player";
    }
}
