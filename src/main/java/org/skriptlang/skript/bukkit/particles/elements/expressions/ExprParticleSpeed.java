package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleSpeed extends SimpleExpression<Double> {

    private Expression<ParticleEffect> particles;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ParticleEffect.class)) return false;
        particles = (Expression<ParticleEffect>) expressions[0];
        return true;
    }

    @Override
    protected Double @Nullable [] get(SkriptEvent event) {
        return particles.stream(event).map(ParticleEffect::extra).toArray(Double[]::new);
    }

    @Override public boolean isSingle() { return particles.isSingle(); }
    @Override public Class<? extends Double> getReturnType() { return Double.class; }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        double amount = mode == ChangeMode.RESET ? 0.0D : delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.doubleValue() : 0.0D;
        for (ParticleEffect effect : particles.getAll(event)) {
            double next = switch (mode) {
                case SET, RESET -> amount;
                case ADD -> effect.extra() + amount;
                case REMOVE -> effect.extra() - amount;
                default -> effect.extra();
            };
            effect.extra(next);
        }
    }

    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "particle speed of " + particles.toString(event, debug); }
}
