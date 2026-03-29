package ch.njol.skript.expressions;

import ch.njol.skript.expressions.base.PropertyExpression;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprAttachedBlock extends PropertyExpression<Projectile, FabricBlock> {

    static {
        register(ExprAttachedBlock.class, FabricBlock.class, "(attached|hit) block[s]", "projectiles");
    }

    @Override
    protected FabricBlock[] get(SkriptEvent event, Projectile[] source) {
        Set<FabricBlock> blocks = new LinkedHashSet<>();
        for (Projectile projectile : source) {
            if (!(projectile instanceof AbstractArrow arrow)) {
                continue;
            }
            if (arrow.level() instanceof ServerLevel level) {
                blocks.add(new FabricBlock(level, arrow.blockPosition()));
            }
        }
        return blocks.toArray(FabricBlock[]::new);
    }

    @Override
    public Class<? extends FabricBlock> getReturnType() {
        return FabricBlock.class;
    }

    @Override
    public boolean isSingle() {
        return getExpr().isSingle();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "attached blocks of " + getExpr().toString(event, debug);
    }
}
