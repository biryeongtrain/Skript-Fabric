package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLocationVectorOffset extends SimpleExpression<FabricLocation> {

    static {
        Skript.registerExpression(
                ExprLocationVectorOffset.class,
                FabricLocation.class,
                "%location% offset by [[the] vector[s]] %vectors%",
                "%location%[ ]~[~][ ]%vectors%"
        );
    }

    private Expression<FabricLocation> location;
    private Expression<Vec3> vectors;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        location = (Expression<FabricLocation>) exprs[0];
        vectors = (Expression<Vec3>) exprs[1];
        return true;
    }

    @Override
    protected FabricLocation @Nullable [] get(SkriptEvent event) {
        FabricLocation source = location.getSingle(event);
        if (source == null) {
            return new FabricLocation[0];
        }
        Vec3 updated = source.position();
        for (Vec3 offset : vectors.getArray(event)) {
            updated = Vec3ExpressionSupport.add(updated, offset);
        }
        return new FabricLocation[]{new FabricLocation(source.level(), updated)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }
}
