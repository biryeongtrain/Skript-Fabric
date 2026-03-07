package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEquipCompSlot extends SimpleExpression<EquipmentSlot> {

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
    protected EquipmentSlot @Nullable [] get(SkriptEvent event) {
        return values.stream(event)
                .map(EquippableSupport::getWrapper)
                .filter(java.util.Objects::nonNull)
                .map(EquippableWrapper::slot)
                .toArray(EquipmentSlot[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends EquipmentSlot> getReturnType() {
        return EquipmentSlot.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{EquipmentSlot.class, String.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        EquipmentSlot slot = parseSlot(delta);
        if (slot == null) {
            return;
        }
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper != null) {
                wrapper.slot(slot);
            }
        }
    }

    private @Nullable EquipmentSlot parseSlot(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return null;
        }
        Object value = delta[0];
        if (value instanceof EquipmentSlot slot) {
            return slot;
        }
        String normalized = String.valueOf(value).trim().toLowerCase(java.util.Locale.ENGLISH).replace('_', ' ');
        return switch (normalized) {
            case "head", "helmet", "helmet slot", "head slot" -> EquipmentSlot.HEAD;
            case "chest", "chestplate", "chest slot", "chestplate slot" -> EquipmentSlot.CHEST;
            case "legs", "leggings", "leggings slot", "legs slot" -> EquipmentSlot.LEGS;
            case "feet", "boots", "boots slot", "feet slot" -> EquipmentSlot.FEET;
            case "mainhand", "main hand", "main hand slot" -> EquipmentSlot.MAINHAND;
            case "offhand", "off hand", "off hand slot" -> EquipmentSlot.OFFHAND;
            case "body", "body slot" -> EquipmentSlot.BODY;
            default -> null;
        };
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "equipment slot of " + values.toString(event, debug);
    }
}
