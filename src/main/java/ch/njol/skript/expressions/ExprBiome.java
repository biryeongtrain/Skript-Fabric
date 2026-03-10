package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprBiome extends PropertyExpression<FabricLocation, Biome> {

    static {
        Skript.registerExpression(ExprBiome.class, Biome.class,
                "[the] biome [(of|%direction%) %locations%]",
                "%locations%'[s] biome");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr(matchedPattern == 1
                ? (Expression<? extends FabricLocation>) exprs[0]
                : Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends FabricLocation>) exprs[1]));
        return true;
    }

    @Override
    protected Biome[] get(SkriptEvent event, FabricLocation[] source) {
        return get(source, location -> location.level() == null
                ? null
                : location.level().getBiome(net.minecraft.core.BlockPos.containing(location.position())).value());
    }

    @Override
    public Class<? extends Biome> getReturnType() {
        return Biome.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "biome at " + getExpr().toString(event, debug);
    }
}
