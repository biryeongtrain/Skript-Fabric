package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricItemEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricTimeAwareItemEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventItem extends SimpleExpression<ItemStack> {

    private int time;

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricItemEventHandle handle)) {
            return null;
        }
        return new ItemStack[]{handle.itemStack(time)};
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
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return expressions.length == 0;
    }

    @Override
    public boolean setTime(int time) {
        if (time == 0) {
            this.time = 0;
            return true;
        }
        if (time == 1 && getParser().isCurrentEvent(FabricTimeAwareItemEventHandle.class)) {
            this.time = 1;
            return true;
        }
        return false;
    }

    @Override
    public int getTime() {
        return time;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-item";
    }
}
