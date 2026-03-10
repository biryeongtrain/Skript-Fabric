package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public class ExprGlowing extends SimpleExpression<Boolean> {

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
            result[i] = isEntityGlowing(targets[i]);
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
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        boolean value = delta != null && delta.length > 0 && delta[0] instanceof Boolean b && b;
        for (Entity entity : entities.getAll(event)) {
            setEntityGlowing(entity, value);
        }
    }

    private static boolean isEntityGlowing(Entity entity) {
        try {
            return (Boolean) Entity.class.getMethod("isCurrentlyGlowing").invoke(entity);
        } catch (ReflectiveOperationException ignored) {
        }
        try {
            return (Boolean) Entity.class.getMethod("hasGlowingTag").invoke(entity);
        } catch (ReflectiveOperationException ignored) {
        }
        return false;
    }

    private static void setEntityGlowing(Entity entity, boolean value) {
        try {
            try {
                Entity.class.getMethod("setGlowing", boolean.class).invoke(entity, value);
                return;
            } catch (NoSuchMethodException ignored) {
            }
            Entity.class.getMethod("setGlowingTag", boolean.class).invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
