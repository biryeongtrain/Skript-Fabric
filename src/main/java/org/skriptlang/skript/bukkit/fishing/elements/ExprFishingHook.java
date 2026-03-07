package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFishingHook extends SimpleExpression<Entity> {

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFishingEventHandle handle)) {
            return null;
        }
        return new Entity[]{handle.hook()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFishingEventHandle.class)) {
            Skript.error("The 'fishing hook' expression can only be used in a fishing event.");
            return false;
        }
        return true;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "fishing hook";
    }
}
