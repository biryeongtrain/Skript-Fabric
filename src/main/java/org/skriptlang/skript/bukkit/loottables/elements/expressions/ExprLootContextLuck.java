package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootContextLuck extends SimpleExpression<Float> {

    private Expression<LootContextWrapper> contexts;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length == 0) {
            return getParser().isCurrentEvent(LootContextWrapper.class);
        }
        if (expressions.length != 1 || !expressions[0].canReturn(LootContextWrapper.class)) {
            return false;
        }
        contexts = (Expression<LootContextWrapper>) expressions[0];
        return true;
    }

    @Override
    protected Float @Nullable [] get(SkriptEvent event) {
        List<Float> values = new ArrayList<>();
        for (LootContextWrapper context : resolve(event)) {
            values.add(context.getLuck());
        }
        return values.toArray(Float[]::new);
    }

    @Override
    public boolean isSingle() {
        return contexts == null || contexts.isSingle();
    }

    @Override
    public Class<? extends Float> getReturnType() {
        return Float.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Float.class, Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        float amount = delta != null && delta.length > 0 && delta[0] instanceof Number number ? number.floatValue() : 0.0F;
        for (LootContextWrapper context : resolve(event)) {
            float next = switch (mode) {
                case SET -> amount;
                case ADD -> context.getLuck() + amount;
                case REMOVE -> context.getLuck() - amount;
                case DELETE, RESET -> 0.0F;
                default -> context.getLuck();
            };
            context.setLuck(next);
        }
    }

    private LootContextWrapper[] resolve(SkriptEvent event) {
        if (contexts != null) {
            return contexts.getAll(event);
        }
        return event.handle() instanceof LootContextWrapper wrapper ? new LootContextWrapper[]{wrapper} : new LootContextWrapper[0];
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return contexts == null ? "loot luck" : "loot luck of " + contexts.toString(event, debug);
    }
}
