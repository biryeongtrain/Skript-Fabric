package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricLootGenerateEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLoot extends SimpleExpression<ItemStack> {

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricLootGenerateEventHandle.class)) {
            Skript.error("The 'loot' expression can only be used in a loot generate event.");
            return false;
        }
        return true;
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricLootGenerateEventHandle handle)) {
            return new ItemStack[0];
        }
        return handle.loot().stream().map(ItemStack::copy).toArray(ItemStack[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE -> new Class[]{ItemStack[].class, ItemStack.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricLootGenerateEventHandle handle)) {
            return;
        }
        List<ItemStack> updates = new ArrayList<>();
        if (delta != null) {
            for (Object value : delta) {
                if (value instanceof ItemStack stack) {
                    updates.add(stack.copy());
                }
            }
        }
        switch (mode) {
            case SET -> {
                handle.loot().clear();
                handle.loot().addAll(updates);
            }
            case ADD -> handle.loot().addAll(updates);
            case REMOVE -> handle.loot().removeIf(existing -> updates.stream().anyMatch(update -> ItemStack.isSameItemSameComponents(existing, update)));
            case DELETE -> handle.loot().clear();
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loot";
    }
}
