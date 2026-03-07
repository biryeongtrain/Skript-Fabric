package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffSetTestBlockAtLocation extends Effect {

    private Expression<FabricLocation> location;
    private Expression<String> blockId;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2) {
            return false;
        }
        location = (Expression<FabricLocation>) expressions[0];
        blockId = (Expression<String>) expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        FabricLocation target = location.getSingle(event);
        String blockKey = blockId.getSingle(event);
        if (target == null || blockKey == null || blockKey.isBlank()) {
            throw new IllegalStateException("set test block at location effect received incomplete location or block id.");
        }

        var resourceLocation = MinecraftResourceParser.parse(blockKey);
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        BlockPos position = BlockPos.containing(target.position());
        if (event.handle() instanceof GameTestHelper helper && target.level() == null) {
            helper.setBlock(position, block.defaultBlockState());
            return;
        }

        ServerLevel level = target.level() != null ? target.level() : event.level();
        if (level == null) {
            throw new IllegalStateException("No target level available for set test block at location effect.");
        }
        level.setBlockAndUpdate(position, block.defaultBlockState());
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return "set test block at location";
    }
}
