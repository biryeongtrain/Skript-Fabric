package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventPlayer extends SimpleExpression<ServerPlayer> {

    @Override
    protected ServerPlayer @Nullable [] get(SkriptEvent event) {
        if (event.player() == null) {
            return null;
        }
        return new ServerPlayer[]{event.player()};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ServerPlayer> getReturnType() {
        return ServerPlayer.class;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return expressions.length == 0;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD -> new Class[]{FabricItemType.class, ItemStack.class};
            case REMOVE -> new Class[]{FabricItemType.class, ItemStack.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0) {
            return;
        }
        ServerPlayer player = event.player();
        if (player == null) {
            return;
        }
        for (Object item : delta) {
            ItemStack stack = toStack(item);
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            switch (mode) {
                case ADD -> {
                    if (!player.getInventory().add(stack.copy())) {
                        player.drop(stack.copy(), false);
                    }
                }
                case REMOVE -> {
                    removeFromInventory(player, stack);
                }
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

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-player";
    }
}
