package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootContext extends SimpleExpression<LootContextWrapper> {

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 0) {
            return false;
        }
        if (!getParser().isCurrentEvent(LootContextWrapper.class)) {
            Skript.error("The 'loot context' expression can only be used inside a loot context creation section.");
            return false;
        }
        return true;
    }

    @Override
    protected LootContextWrapper @Nullable [] get(SkriptEvent event) {
        return event.handle() instanceof LootContextWrapper wrapper ? new LootContextWrapper[]{wrapper} : new LootContextWrapper[0];
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends LootContextWrapper> getReturnType() {
        return LootContextWrapper.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loot context";
    }
}
