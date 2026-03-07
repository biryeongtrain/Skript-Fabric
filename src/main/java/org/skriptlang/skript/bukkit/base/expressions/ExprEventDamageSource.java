package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricDamageSourceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventDamageSource extends SimpleExpression<DamageSource> {

    @Override
    protected DamageSource @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricDamageSourceEventHandle handle)) {
            return null;
        }
        return new DamageSource[]{handle.damageSource()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends DamageSource> getReturnType() {
        return DamageSource.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return expressions.length == 0;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-damage source";
    }
}
