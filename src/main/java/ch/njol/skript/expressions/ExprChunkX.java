package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.chunk.LevelChunk;

public class ExprChunkX extends SimplePropertyExpression<LevelChunk, Number> {

    static {
        register(ExprChunkX.class, Number.class, "chunk x(-| )coord[inate][s]", "chunks");
    }

    @Override
    public Number convert(LevelChunk chunk) {
        return chunk.getPos().x;
    }

    @Override
    protected String getPropertyName() {
        return "chunk x-coordinate";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
