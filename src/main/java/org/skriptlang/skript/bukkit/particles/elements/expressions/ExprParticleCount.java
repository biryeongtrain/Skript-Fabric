package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleCount extends SimpleExpression<Integer> {

    private Expression<ParticleEffect> particles;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(ParticleEffect.class)) {
            return false;
        }
        particles = (Expression<ParticleEffect>) expressions[0];
        return true;
    }

    @Override
    protected Integer @Nullable [] get(SkriptEvent event) {
        return particles.stream(event).map(ParticleEffect::count).toArray(Integer[]::new);
    }

    @Override
    public boolean isSingle() { return particles.isSingle(); }

    @Override
    public Class<? extends Integer> getReturnType() { return Integer.class; }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int amount = mode == ChangeMode.RESET ? 0 : delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.intValue() : 0;
        for (ParticleEffect effect : particles.getAll(event)) {
            int next = switch (mode) {
                case SET, RESET -> amount;
                case ADD -> effect.count() + amount;
                case REMOVE -> effect.count() - amount;
                default -> effect.count();
            };
            effect.count(Math.max(0, Math.min(16384, next)));
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) { return "particle count of " + particles.toString(event, debug); }
}
