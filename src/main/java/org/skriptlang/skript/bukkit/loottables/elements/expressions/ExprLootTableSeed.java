package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootTableSeed extends SimpleExpression<Long> {

    private static final int EVENT_ENTITY_PATTERN = 0;
    private static final int EVENT_BLOCK_PATTERN = 1;

    private Expression<?> values;
    private int matchedPattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        if (expressions.length == 0) {
            return matchedPattern == EVENT_ENTITY_PATTERN || matchedPattern == EVENT_BLOCK_PATTERN;
        }
        if (expressions.length == 1) {
            values = expressions[0];
            return true;
        }
        return false;
    }

    @Override
    protected Long @Nullable [] get(SkriptEvent event) {
        List<Long> seeds = new ArrayList<>();
        for (Object value : resolveValues(event)) {
            Long seed = LootTableUtils.getLootTableSeed(value);
            if (seed != null) {
                seeds.add(seed);
            }
        }
        return seeds.toArray(Long[]::new);
    }

    @Override
    public boolean isSingle() {
        return values == null || values.isSingle();
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Long.class, Integer.class, Number.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (mode != ChangeMode.SET || delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
            return;
        }
        long seed = number.longValue();
        for (Object value : resolveValues(event)) {
            if (LootTableUtils.getLootTable(value) == null) {
                continue;
            }
            LootTableUtils.setLootTableSeed(value, seed);
        }
    }

    private Object[] resolveValues(SkriptEvent event) {
        if (values != null) {
            return values.getAll(event);
        }
        if (matchedPattern == EVENT_ENTITY_PATTERN && event.handle() instanceof FabricEntityEventHandle handle) {
            return new Object[]{handle.entity()};
        }
        if (matchedPattern == EVENT_BLOCK_PATTERN && event.handle() instanceof FabricBlockEventHandle handle) {
            return new Object[]{new FabricBlock(handle.level(), handle.position())};
        }
        return new Object[0];
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (values != null) {
            return "loot table seed of " + values.toString(event, debug);
        }
        return matchedPattern == EVENT_BLOCK_PATTERN
                ? "loot table seed of event-block"
                : "loot table seed of event-entity";
    }
}
