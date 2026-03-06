package org.skriptlang.skript.fabric.syntax.effect;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

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

        String normalizedBlockKey = normalizeBlockId(blockKey);

        ResourceLocation resourceLocation;
        try {
            resourceLocation = ResourceLocation.parse(normalizedBlockKey);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Unable to parse block id. raw=" + printable(blockKey) + ", normalized=" + printable(normalizedBlockKey),
                    ex
            );
        }
        Block block = BuiltInRegistries.BLOCK.getValue(resourceLocation);
        if (block == null) {
            throw new IllegalArgumentException("Unknown block id: " + blockKey);
        }

        helper.setBlock(new BlockPos(xValue, yValue, zValue), block.defaultBlockState());
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

    private String printable(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    @Override
    public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
        return "set test block";
    }
}
