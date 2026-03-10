package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ConvertedExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.converter.ConverterInfo;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprLocationOf extends WrapperExpression<FabricLocation> {

    static {
        Skript.registerExpression(
                ExprLocationOf.class,
                FabricLocation.class,
                "(location|position) of %locations/blocks/entities/chunks%",
                "%locations/blocks/entities/chunks%'[s] (location|position)"
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<?> source = exprs[0];
        if (source.canReturn(FabricLocation.class)) {
            setExpr((Expression<? extends FabricLocation>) source);
            return true;
        }
        if (source.canReturn(FabricBlock.class)) {
            setExpr(new ConvertedExpression<>(
                    (Expression<? extends FabricBlock>) source,
                    FabricLocation.class,
                    new ConverterInfo<>(FabricBlock.class, FabricLocation.class, FabricLocationExpressionSupport::locationOf, 0)
            ));
            return true;
        }
        if (source.canReturn(Entity.class)) {
            setExpr(new ConvertedExpression<>(
                    (Expression<? extends Entity>) source,
                    FabricLocation.class,
                    new ConverterInfo<>(Entity.class, FabricLocation.class, FabricLocationExpressionSupport::locationOf, 0)
            ));
            return true;
        }
        if (source.canReturn(LevelChunk.class)) {
            setExpr(new ConvertedExpression<>(
                    (Expression<? extends LevelChunk>) source,
                    FabricLocation.class,
                    new ConverterInfo<>(LevelChunk.class, FabricLocation.class, FabricLocationExpressionSupport::locationOf, 0)
            ));
            return true;
        }
        return false;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "the location of " + getExpr().toString(event, debug);
    }
}
