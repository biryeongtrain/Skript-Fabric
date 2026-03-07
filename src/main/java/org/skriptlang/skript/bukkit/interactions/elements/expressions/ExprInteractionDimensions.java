package org.skriptlang.skript.bukkit.interactions.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprInteractionDimensions extends SimpleExpression<Float> {

    private Expression<Entity> entities;
    private boolean width;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(Entity.class)) {
            return false;
        }
        entities = (Expression<Entity>) expressions[0];
        width = matchedPattern < 2;
        return true;
    }

    @Override
    protected Float @Nullable [] get(SkriptEvent event) {
        List<Float> values = new ArrayList<>();
        for (Entity entity : entities.getAll(event)) {
            if (!(entity instanceof Interaction interaction)) {
                continue;
            }
            values.add(width ? PrivateEntityAccess.interactionWidth(interaction) : PrivateEntityAccess.interactionHeight(interaction));
        }
        return values.toArray(Float[]::new);
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET -> new Class[]{Float.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        float amount = mode == ChangeMode.RESET
                ? 1.0F
                : delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.floatValue() : 0.0F;
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            return;
        }
        for (Entity entity : entities.getAll(event)) {
            if (!(entity instanceof Interaction interaction)) {
                continue;
            }
            float current = width ? PrivateEntityAccess.interactionWidth(interaction) : PrivateEntityAccess.interactionHeight(interaction);
            float next = switch (mode) {
                case SET, RESET -> amount;
                case ADD -> current + amount;
                case REMOVE -> current - amount;
                default -> current;
            };
            next = Math.max(0.0F, next);
            if (width) {
                PrivateEntityAccess.setInteractionWidth(interaction, next);
            } else {
                PrivateEntityAccess.setInteractionHeight(interaction, next);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (width ? "interaction width of " : "interaction height of ") + entities.toString(event, debug);
    }
}
