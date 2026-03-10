package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.world.level.chunk.LevelChunk;

public class ExprChunkZ extends SimplePropertyExpression<LevelChunk, Number> {

    static {
        register(ExprChunkZ.class, Number.class, "chunk z(-| )coord[inate][s]", "chunks");
    }

    @Override
    public Number convert(LevelChunk chunk) {
        return chunk.getPos().z;
    }

    @Override
    protected String getPropertyName() {
        return "chunk z-coordinate";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
