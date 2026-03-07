package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInteractionState;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLastInteractionDate extends SimpleExpression<Date> {

    private static final int ATTACK = 0;
    private static final int INTERACT = 1;
    private static final int BOTH = 2;

    private Expression<Entity> entities;
    private int mode;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        mode = switch (matchedPattern) {
            case 0 -> ATTACK;
            case 1 -> INTERACT;
            default -> BOTH;
        };
        return true;
    }

    @Override
    protected Date @Nullable [] get(SkriptEvent event) {
        List<Date> values = new ArrayList<>();
        for (Entity entity : entities.getAll(event)) {
            if (!(entity instanceof Interaction interaction)) {
                continue;
            }
            FabricInteractionState.Action action = switch (mode) {
                case ATTACK -> FabricInteractionState.lastAttack(interaction);
                case INTERACT -> FabricInteractionState.lastInteract(interaction);
                default -> FabricInteractionState.lastClick(interaction);
            };
            if (action != null) {
                values.add(new Date(action.timestamp()));
            }
        }
        return values.toArray(Date[]::new);
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
    }

    @Override
    public Class<? extends Date> getReturnType() {
        return Date.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String action = switch (mode) {
            case ATTACK -> "attacked";
            case INTERACT -> "interacted with";
            default -> "clicked on";
        };
        return "last date that " + entities.toString(event, debug) + " was " + action;
    }
}
