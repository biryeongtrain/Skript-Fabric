package org.skriptlang.skript.bukkit.particles.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.skript.util.ColorRGB;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffPlayEffect extends Effect {

    private Expression<?> drawables;
    private @Nullable Expression<FabricLocation> locations;
    private @Nullable Expression<ServerPlayer> players;
    private @Nullable Expression<Number> radius;
    private @Nullable Expression<Entity> entities;
    private boolean force;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        force = matchedPattern < 4;
        drawables = expressions[0];
        switch (matchedPattern % 4) {
            case 0 -> locations = (Expression<FabricLocation>) expressions[1];
            case 1 -> {
                locations = (Expression<FabricLocation>) expressions[1];
                players = (Expression<ServerPlayer>) expressions[2];
            }
            case 2 -> {
                locations = (Expression<FabricLocation>) expressions[1];
                radius = (Expression<Number>) expressions[2];
            }
            case 3 -> entities = (Expression<Entity>) expressions[1];
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        if (entities != null) {
            return;
        }
        if (locations == null) {
            return;
        }
        for (FabricLocation location : locations.getAll(event)) {
            ServerLevel level = location.level();
            if (level == null) {
                continue;
            }
            for (Object drawable : drawables.getAll(event)) {
                if (drawable instanceof ParticleEffect particleEffect) {
                    playParticle(level, location.position(), particleEffect, players != null ? players.getAll(event) : null);
                } else if (drawable instanceof GameEffect gameEffect) {
                    playGameEffect(level, location.position(), gameEffect, radius != null ? radius.getSingle(event) : null);
                }
            }
        }
    }

    private void playParticle(ServerLevel level, Vec3 position, ParticleEffect effect, @Nullable ServerPlayer[] targets) {
        if (targets == null || targets.length == 0) {
            level.sendParticles(
                    effect.particle(),
                    force,
                    false,
                    position.x,
                    position.y,
                    position.z,
                    effect.count(),
                    effect.offset().x,
                    effect.offset().y,
                    effect.offset().z,
                    effect.extra()
            );
            return;
        }
        for (ServerPlayer player : targets) {
            level.sendParticles(
                    player,
                    effect.particle(),
                    force,
                    false,
                    position.x,
                    position.y,
                    position.z,
                    effect.count(),
                    effect.offset().x,
                    effect.offset().y,
                    effect.offset().z,
                    effect.extra()
            );
        }
    }

    private void playGameEffect(ServerLevel level, Vec3 position, GameEffect effect, @Nullable Number radius) {
        BlockPos blockPos = BlockPos.containing(position);
        String id = effect.id().getPath();
        if ("potion_break".equals(id)) {
            ColorRGB color = effect.data() instanceof ColorRGB rgb ? rgb : new ColorRGB(255, 255, 255);
            level.levelEvent(null, 2002, blockPos, color.rgb());
        } else if ("bone_meal_use".equals(id)) {
            int particles = effect.data() instanceof Number number ? Math.max(0, number.intValue()) : 15;
            level.levelEvent(null, 2005, blockPos, particles);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "play " + drawables.toString(event, debug);
    }
}
