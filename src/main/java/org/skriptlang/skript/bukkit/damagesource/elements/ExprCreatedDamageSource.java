package org.skriptlang.skript.bukkit.damagesource.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprCreatedDamageSource extends SimpleExpression<DamageSource> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(DamageSourceSectionContext.class)) {
            Skript.error("The 'created damage source' expression can only be used inside a custom damage source section.");
            return false;
        }
        return true;
    }

    @Override
    protected DamageSource @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof DamageSourceSectionContext context)) {
            return new DamageSource[0];
        }
        return new DamageSource[]{context.build()};
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
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "created damage source";
    }
}
