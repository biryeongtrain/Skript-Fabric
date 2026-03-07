package org.skriptlang.skript.bukkit.brewing.elements;

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
import org.skriptlang.skript.fabric.runtime.FabricBrewingCompleteEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprBrewingResults extends SimpleExpression<ItemStack> {

    private boolean delayed;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(FabricBrewingCompleteEventHandle.class)) {
            Skript.error("The 'brewing results' expression can only be used in a brewing complete event.");
            return false;
        }
        delayed = isDelayed.isTrue();
        return exprs.length == 0;
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricBrewingCompleteEventHandle handle)) {
            return new ItemStack[0];
        }
        return handle.results().stream().map(ItemStack::copy).toArray(ItemStack[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (delayed) {
            Skript.error("The 'brewing results' cannot be changed after the brewing complete event has passed.");
            return null;
        }
        return switch (mode) {
            case SET, DELETE, ADD, REMOVE -> new Class[]{ItemStack[].class, ItemStack.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricBrewingCompleteEventHandle handle)) {
            return;
        }
        List<ItemStack> results = handle.results();
        List<ItemStack> updates = new ArrayList<>();
        if (delta != null) {
            for (Object value : delta) {
                if (value instanceof ItemStack itemStack) {
                    updates.add(itemStack.copy());
                }
            }
        }
        switch (mode) {
            case SET -> {
                results.clear();
                results.addAll(updates);
            }
            case DELETE -> results.clear();
            case ADD -> results.addAll(updates);
            case REMOVE -> results.removeIf(existing -> updates.stream().anyMatch(update -> ItemStack.isSameItemSameComponents(existing, update)));
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
        return "brewing results";
    }
}
