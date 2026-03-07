package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleWithSpeed extends SimpleExpression<ParticleEffect> {

    private Expression<ParticleEffect> particles;
    private Expression<Number> speed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        particles = (Expression<ParticleEffect>) expressions[0];
        speed = (Expression<Number>) expressions[1];
        return true;
    }

    @Override
    protected ParticleEffect @Nullable [] get(SkriptEvent event) {
        Number amount = speed.getSingle(event);
        if (amount == null) {
            return new ParticleEffect[0];
        }
        return particles.stream(event)
                .map(ParticleEffect::copy)
                .peek(effect -> effect.extra(amount.doubleValue()))
                .toArray(ParticleEffect[]::new);
    }

    @Override public boolean isSingle() { return particles.isSingle(); }
    @Override public Class<? extends ParticleEffect> getReturnType() { return ParticleEffect.class; }
    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "particle with speed"; }
}
