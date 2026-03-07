package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AgeableMob;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffAllowAging extends Effect {

    private boolean unlock;
    private Expression<Entity> entities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        unlock = matchedPattern > 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (Entity entity : entities.getAll(event)) {
            if (entity instanceof AgeableMob ageableMob) {
                FabricBreedingState.setAgeLocked(ageableMob, !unlock);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (unlock ? "allow" : "prevent") + " aging of " + entities.toString(event, debug);
    }
}
