package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

/**
 * Exact upstream property expression: "glowing" of entities.
 * Allows reading and changing the glowing state of entities.
 */
public final class ExprGlowing extends SimpleExpression<Boolean> {

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
    protected Boolean @Nullable [] get(SkriptEvent event) {
        Entity[] targets = entities.getArray(event);
        Boolean[] result = new Boolean[targets.length];
        for (int i = 0; i < targets.length; i++) {
            Entity e = targets[i];
            result[i] = isEntityGlowing(e);
        }
        return result;
    }

    @Override
    public boolean isSingle() {
        return entities.isSingle();
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "glowing of " + entities.toString(event, debug);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET -> new Class[]{Boolean.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) throws UnsupportedOperationException {
        boolean value = delta != null && delta.length > 0 && delta[0] instanceof Boolean b ? b : false;
        for (Entity entity : entities.getAll(event)) {
            setEntityGlowing(entity, value);
        }
    }

    private static boolean isEntityGlowing(Entity entity) {
        // Yarn names vary; try both common mappings.
        try {
            // 1.21+: isCurrentlyGlowing() exists on Entity
            return (Boolean) Entity.class.getMethod("isCurrentlyGlowing").invoke(entity);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            // Fallback: hasGlowingTag() existed in older mappings
            return (Boolean) Entity.class.getMethod("hasGlowingTag").invoke(entity);
        } catch (ReflectiveOperationException ignored) {
        }
        // Last resort: assume not glowing
        return false;
    }

    private static void setEntityGlowing(Entity entity, boolean value) {
        try {
            // 1.21+: setGlowingTag(boolean) may exist, but prefer setGlowing(boolean) if available
            try {
                Entity.class.getMethod("setGlowing", boolean.class).invoke(entity, value);
                return;
            } catch (NoSuchMethodException ignored) {
                // fall through
            }
            Entity.class.getMethod("setGlowingTag", boolean.class).invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
            // No-op if API not present; expression remains best-effort in current runtime.
        }
    }
}
