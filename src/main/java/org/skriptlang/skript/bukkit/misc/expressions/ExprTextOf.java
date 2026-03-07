package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprTextOf extends SimpleExpression<String> {

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
    protected String @Nullable [] get(SkriptEvent event) {
        return entities.stream(event)
                .filter(Display.TextDisplay.class::isInstance)
                .map(Display.TextDisplay.class::cast)
                .map(PrivateEntityAccess::textDisplayText)
                .map(Component::getString)
                .toArray(String[]::new);
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{String.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Component text = mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] != null
                ? Component.literal(String.valueOf(delta[0]))
                : Component.empty();
        for (Entity entity : entities.getAll(event)) {
            if (entity instanceof Display.TextDisplay textDisplay) {
                PrivateEntityAccess.setTextDisplayText(textDisplay, text);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "text of " + entities.toString(event, debug);
    }
}
