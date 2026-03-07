package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.DirectionalEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleWithOffset extends SimpleExpression<ParticleEffect> {

    private Expression<ParticleEffect> particles;
    private Expression<?> offset;
    private int pattern;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        particles = (Expression<ParticleEffect>) expressions[0];
        offset = expressions[1];
        pattern = matchedPattern;
        return true;
    }

    @Override
    protected ParticleEffect @Nullable [] get(SkriptEvent event) {
        Vec3 vec3 = offset.getSingle(event) instanceof Vec3 provided ? provided : Classes.parse(String.valueOf(offset.getSingle(event)), Vec3.class, ParseContext.DEFAULT);
        if (vec3 == null) {
            return new ParticleEffect[0];
        }
        return particles.stream(event).map(effect -> {
            ParticleEffect copy = effect.copy();
            if (pattern == 1) {
                copy.distribution(vec3);
            } else if (pattern == 2 && copy instanceof DirectionalEffect directional) {
                directional.velocity(vec3);
                copy = directional;
            } else {
                copy.offset(vec3);
            }
            return copy;
        }).toArray(ParticleEffect[]::new);
    }

    @Override public boolean isSingle() { return particles.isSingle(); }
    @Override public Class<? extends ParticleEffect> getReturnType() { return ParticleEffect.class; }
    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "particle with offset"; }
}
