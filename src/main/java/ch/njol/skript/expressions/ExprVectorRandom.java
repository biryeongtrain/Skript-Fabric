package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.Random;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprVectorRandom extends SimpleExpression<Vec3> {

    private static final Random RANDOM = new Random();

    static {
        Skript.registerExpression(ExprVectorRandom.class, Vec3.class, "[a] random vector");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        return new Vec3[]{
                Vec3ExpressionSupport.normalize(new Vec3(
                        RANDOM.nextGaussian(),
                        RANDOM.nextGaussian(),
                        RANDOM.nextGaussian()
                ))
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Vec3> getReturnType() {
        return Vec3.class;
    }
}
