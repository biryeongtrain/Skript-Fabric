package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Inventory")
@Description("The inventory of a player or inventory object.")
@Example("clear the player's inventory")
@Example("remove 5 wool from the inventory of player")
@Since("1.0")
public final class ExprInventory extends SimplePropertyExpression<Object, FabricInventory> {

    @Override
    public @Nullable FabricInventory convert(Object from) {
        if (from instanceof FabricInventory inventory) {
            return inventory;
        }
        if (from instanceof ServerPlayer player) {
            return new FabricInventory(player.getInventory(), MenuType.GENERIC_9x5, player.getName(), player);
        }
        return null;
    }

    @Override
    public Class<? extends FabricInventory> getReturnType() {
        return FabricInventory.class;
    }

    @Override
    protected String getPropertyName() {
        return "inventory";
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD -> new Class[]{FabricItemType.class, ItemStack.class};
            case REMOVE -> new Class[]{FabricItemType.class, ItemStack.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        for (Object obj : getExpr().getAll(event)) {
            if (!(obj instanceof ServerPlayer player)) continue;
            switch (mode) {
                case ADD -> {
                    if (delta == null) break;
                    for (Object item : delta) {
                        ItemStack stack = toStack(item);
                        if (stack == null || stack.isEmpty()) continue;
                        if (!player.getInventory().add(stack.copy())) {
                            player.drop(stack.copy(), false);
                        }
                    }
                }
                case REMOVE -> {
                    if (delta == null) break;
                    for (Object item : delta) {
                        ItemStack stack = toStack(item);
                        if (stack == null || stack.isEmpty()) continue;
                        removeFromInventory(player, stack);
                    }
                }
                case DELETE, RESET -> player.getInventory().clearContent();
                default -> {}
            }
        }
    }

    private static @Nullable ItemStack toStack(Object item) {
        if (item instanceof FabricItemType fabricItemType) {
            return fabricItemType.toStack();
        }
        if (item instanceof ItemStack itemStack) {
            return itemStack;
        }
        return null;
    }

    private static void removeFromInventory(ServerPlayer player, ItemStack toRemove) {
        int remaining = toRemove.getCount();
        for (int i = 0; i < player.getInventory().getContainerSize() && remaining > 0; i++) {
            ItemStack slot = player.getInventory().getItem(i);
            if (!slot.isEmpty() && slot.getItem() == toRemove.getItem()) {
                int take = Math.min(remaining, slot.getCount());
                slot.shrink(take);
                remaining -= take;
            }
        }
    }
}
