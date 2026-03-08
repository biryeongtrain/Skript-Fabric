package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootContextEntity extends SimpleExpression<Entity> {

    private @Nullable Expression<?> contexts;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length == 0) {
            if (!getParser().isCurrentEvent(LootContextWrapper.class)) {
                return false;
            }
            return true;
        }
        if (expressions.length != 1 || !expressions[0].canReturn(LootContextWrapper.class)) {
            return false;
        }
        contexts = expressions[0];
        return true;
    }

    @Override
    protected Entity @Nullable [] get(SkriptEvent event) {
        List<Entity> values = new ArrayList<>();
        for (LootContextWrapper context : resolve(event)) {
            if (context.getLootedEntity() != null) {
                values.add(context.getLootedEntity());
            }
        }
        return values.toArray(Entity[]::new);
    }

    @Override
    public boolean isSingle() {
        return contexts == null || contexts.isSingle();
    }

    @Override
    public Class<? extends Entity> getReturnType() {
        return Entity.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{Entity.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Entity value = delta != null && delta.length > 0 && delta[0] instanceof Entity entity ? entity : null;
        for (LootContextWrapper context : resolve(event)) {
            context.setLootedEntity(value);
        }
    }

    private LootContextWrapper[] resolve(SkriptEvent event) {
        if (contexts != null) {
            List<LootContextWrapper> resolved = new ArrayList<>();
            for (Object value : contexts.getAll(event)) {
                if (value instanceof LootContextWrapper context) {
                    resolved.add(context);
                }
            }
            return resolved.toArray(LootContextWrapper[]::new);
        }
        return event.handle() instanceof LootContextWrapper wrapper ? new LootContextWrapper[]{wrapper} : new LootContextWrapper[0];
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return contexts == null ? "looted entity" : "looted entity of " + contexts.toString(event, debug);
    }
}
