package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventEntity extends SimpleExpression<Entity> {

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricEntityEventHandle handle)) {
            return null;
        }
        return new Entity[]{handle.entity()};
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
        return expressions.length == 0;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-entity";
    }
}
