package org.skriptlang.skript.fabric.syntax.effect;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.skriptlang.skript.fabric.compat.FabricBlock;
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

        ResourceLocation resourceLocation = ResourceLocation.parse(normalizeBlockId(blockKey));
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        target.level().setBlockAndUpdate(target.position(), block.defaultBlockState());
    }

    private String normalizeBlockId(String blockKey) {
        String normalized = blockKey.trim();
        while (normalized.length() >= 2) {
            if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                    || (normalized.startsWith("'") && normalized.endsWith("'"))) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
                continue;
            }
            if ((normalized.startsWith("\\\"") && normalized.endsWith("\\\""))
                    || (normalized.startsWith("\\'") && normalized.endsWith("\\'"))) {
                normalized = normalized.substring(2, normalized.length() - 2).trim();
                continue;
            }
            break;
        }
        return normalized
                .replace("\\\"", "")
                .replace("\\'", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^A-Za-z0-9_:/.-]", "");
    }

    @Override
    public String toString(SkriptEvent event, boolean debug) {
        return "set test block at block";
    }
}
