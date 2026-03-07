package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootContextLocation extends SimpleExpression<FabricLocation> {

    private Expression<LootContextWrapper> contexts;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length == 0) {
            return getParser().isCurrentEvent(LootContextWrapper.class);
        }
        if (expressions.length != 1 || !expressions[0].canReturn(LootContextWrapper.class)) {
            return false;
        }
        contexts = (Expression<LootContextWrapper>) expressions[0];
        return true;
    }

    @Override
    protected FabricLocation @Nullable [] get(SkriptEvent event) {
        List<FabricLocation> values = new ArrayList<>();
        for (LootContextWrapper context : resolve(event)) {
            values.add(context.getLocation());
        }
        return values.toArray(FabricLocation[]::new);
    }

    @Override
    public boolean isSingle() {
        return contexts == null || contexts.isSingle();
    }

    @Override
    public Class<? extends FabricLocation> getReturnType() {
        return FabricLocation.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{FabricLocation.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof FabricLocation location)) {
            return;
        }
        for (LootContextWrapper context : resolve(event)) {
            context.setLocation(location);
        }
    }

    private LootContextWrapper[] resolve(SkriptEvent event) {
        if (contexts != null) {
            return contexts.getAll(event);
        }
        return event.handle() instanceof LootContextWrapper wrapper ? new LootContextWrapper[]{wrapper} : new LootContextWrapper[0];
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return contexts == null ? "loot location" : "loot location of " + contexts.toString(event, debug);
    }
}
