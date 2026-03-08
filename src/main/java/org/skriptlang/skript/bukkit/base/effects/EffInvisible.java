package org.skriptlang.skript.bukkit.base.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class EffInvisible extends Effect {

    private boolean invisible;
    private Expression<LivingEntity> livingEntities;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        livingEntities = (Expression<LivingEntity>) expressions[0];
        invisible = matchedPattern == 0 || matchedPattern == 2;
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        for (LivingEntity livingEntity : livingEntities.getAll(event)) {
            livingEntity.setInvisible(invisible);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "make " + livingEntities.toString(event, debug) + " " + (invisible ? "invisible" : "visible");
    }
}
