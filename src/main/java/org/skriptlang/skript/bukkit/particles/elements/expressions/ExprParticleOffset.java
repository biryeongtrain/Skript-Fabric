package org.skriptlang.skript.bukkit.particles.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprParticleOffset extends SimpleExpression<Vec3> {

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
    protected Vec3 @Nullable [] get(SkriptEvent event) {
        return particles.stream(event).map(ParticleEffect::offset).toArray(Vec3[]::new);
    }

    @Override public boolean isSingle() { return particles.isSingle(); }
    @Override public Class<? extends Vec3> getReturnType() { return Vec3.class; }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Vec3.class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Vec3 change = mode == ChangeMode.RESET ? Vec3.ZERO : parse(delta);
        if (change == null) {
            return;
        }
        for (ParticleEffect effect : particles.getAll(event)) {
            Vec3 next = switch (mode) {
                case SET, RESET -> change;
                case ADD -> effect.offset().add(change);
                case REMOVE -> effect.offset().subtract(change);
                default -> effect.offset();
            };
            effect.offset(next);
        }
    }

    private @Nullable Vec3 parse(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) return null;
        if (delta[0] instanceof Vec3 vec3) return vec3;
        return Classes.parse(String.valueOf(delta[0]), Vec3.class, ParseContext.DEFAULT);
    }

    @Override public String toString(@Nullable SkriptEvent event, boolean debug) { return "particle offset of " + particles.toString(event, debug); }
}
