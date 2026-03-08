package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffKill extends Effect {

    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getAll(event)) {
            ServerLevel level = event.level();
            if (entity.level() instanceof ServerLevel entityLevel) {
                level = entityLevel;
            }
            if (level != null) {
                entity.kill(level);
            }
            if (entity.isAlive()) {
                entity.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "kill " + entities.toString(event, debug);
    }
}
