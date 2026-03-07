package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableSupport;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEquippableComponent extends SimpleExpression<EquippableWrapper> {

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
    protected EquippableWrapper @Nullable [] get(SkriptEvent event) {
        return values.stream(event)
                .map(EquippableSupport::getWrapper)
                .filter(java.util.Objects::nonNull)
                .toArray(EquippableWrapper[]::new);
    }

    @Override
    public boolean isSingle() {
        return values.isSingle();
    }

    @Override
    public Class<? extends EquippableWrapper> getReturnType() {
        return EquippableWrapper.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{EquippableWrapper.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        Equippable replacement = delta != null && delta.length > 0 && delta[0] instanceof EquippableWrapper wrapper ? wrapper.getComponent() : null;
        for (Object value : values.getAll(event)) {
            EquippableWrapper wrapper = EquippableSupport.getWrapper(value);
            if (wrapper == null) {
                continue;
            }
            if (mode == ChangeMode.SET && replacement != null) {
                wrapper.applyComponent(replacement);
            } else if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
                wrapper.clearComponent();
            }
            if (value instanceof ItemStack stack) {
                stack.set(net.minecraft.core.component.DataComponents.EQUIPPABLE, wrapper.getComponent());
            } else if (value instanceof Slot slot) {
                ItemStack stack = slot.getItem().copy();
                if (!stack.isEmpty()) {
                    stack.set(net.minecraft.core.component.DataComponents.EQUIPPABLE, wrapper.getComponent());
                    slot.set(stack);
                }
            } else if (value instanceof FabricItemType itemType) {
                itemType.equippable(wrapper.getComponent());
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "equippable component of " + values.toString(event, debug);
    }
}
