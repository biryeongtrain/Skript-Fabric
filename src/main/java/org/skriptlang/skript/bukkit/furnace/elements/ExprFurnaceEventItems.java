package org.skriptlang.skript.bukkit.furnace.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceHandle;
import org.skriptlang.skript.fabric.runtime.FabricFurnaceEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprFurnaceEventItems extends SimpleExpression<ItemStack> {

    private enum Type {
        SMELTED,
        EXTRACTED,
        SMELTING,
        BURNED
    }

    private Type type;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(FabricFurnaceEventHandle.class)) {
            Skript.error("The furnace event item expressions can only be used in furnace events.");
            return false;
        }
        type = switch (matchedPattern) {
            case 0 -> Type.SMELTED;
            case 1 -> Type.EXTRACTED;
            case 2 -> Type.SMELTING;
            case 3 -> Type.BURNED;
            default -> Type.SMELTED;
        };
        return true;
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricFurnaceEventHandle handle)) {
            return new ItemStack[0];
        }
        ItemStack value = switch (type) {
            case SMELTED -> handle.result().copy();
            case EXTRACTED -> handle.result().copyWithCount(Math.max(1, handle.itemAmount()));
            case SMELTING -> handle.source().copy();
            case BURNED -> handle.fuel().copy();
        };
        return new ItemStack[]{value};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (type != Type.SMELTED) {
            return null;
        }
        return switch (mode) {
            case SET -> new Class[]{ItemStack.class};
            case DELETE, RESET -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (!(event.handle() instanceof FabricFurnaceHandle handle) || type != Type.SMELTED) {
            return;
        }
        ItemStack next = ItemStack.EMPTY;
        if (mode == ChangeMode.SET && delta != null && delta.length > 0 && delta[0] instanceof ItemStack stack) {
            next = stack.copy();
        }
        handle.furnace().setItem(2, next);
        handle.furnace().setChanged();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return switch (type) {
            case SMELTED -> "smelted item";
            case EXTRACTED -> "extracted item";
            case SMELTING -> "smelting item";
            case BURNED -> "burned fuel";
        };
    }
}
