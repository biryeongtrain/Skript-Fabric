package org.skriptlang.skript.bukkit.base.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.FabricItemEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprEventItem extends SimpleExpression<ItemStack> {

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        if (!(event.handle() instanceof FabricItemEventHandle handle)) {
            return null;
        }
        return new ItemStack[]{handle.itemStack()};
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
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "event-item";
    }
}
