package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEquipCompEntities extends SimpleExpression<EntityType<?>> {

    private Expression<?> values;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1) {
            return false;
        }
        values = expressions[0];
        return true;
    }

    @Override
    protected EntityType<?> @Nullable [] get(SkriptEvent event) {
        List<EntityType<?>> results = new ArrayList<>();
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper != null) {
                results.addAll(wrapper.allowedEntities());
            }
        }
        return results.toArray(EntityType[]::new);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends EntityType<?>> getReturnType() {
        return (Class<? extends EntityType<?>>) (Class<?>) EntityType.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{String.class, EntityType.class};
            case DELETE -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        List<EntityType<?>> converted = new ArrayList<>();
        if (delta != null) {
            for (Object value : delta) {
                if (value instanceof EntityType<?> entityType) {
                    converted.add(entityType);
                } else if (value != null) {
                    EntityType<?> parsed = BuiltInRegistries.ENTITY_TYPE.getValue(MinecraftResourceParser.parse(String.valueOf(value)));
                    if (parsed != null) {
                        converted.add(parsed);
                    }
                }
            }
        }
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper == null) {
                continue;
            }
            List<EntityType<?>> current = new ArrayList<>(wrapper.allowedEntities());
            switch (mode) {
                case SET -> current = new ArrayList<>(converted);
                case ADD -> current.addAll(converted);
                case REMOVE -> current.removeAll(converted);
                case DELETE -> current.clear();
                default -> {
                }
            }
            wrapper.allowedEntities(current);
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "allowed entities of " + values.toString(event, debug);
    }
}
