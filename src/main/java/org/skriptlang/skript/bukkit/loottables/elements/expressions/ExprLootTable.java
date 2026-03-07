package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootTable extends SimpleExpression<LootTable> {

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
    protected LootTable @Nullable [] get(SkriptEvent event) {
        List<LootTable> lootTables = new ArrayList<>();
        for (Object value : resolveValues(event)) {
            LootTable lootTable = LootTableUtils.getLootTable(value);
            if (lootTable != null) {
                lootTables.add(lootTable);
            }
        }
        return lootTables.toArray(LootTable[]::new);
    }

    @Override
    public boolean isSingle() {
        return values == null || values.isSingle();
    }

    @Override
    public Class<? extends LootTable> getReturnType() {
        return LootTable.class;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{LootTable.class, String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        LootTable lootTable = mode == ChangeMode.SET ? resolve(delta) : null;
        if (mode == ChangeMode.SET && lootTable == null) {
            return;
        }
        for (Object value : resolveValues(event)) {
            LootTableUtils.setLootTable(value, lootTable);
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

    private @Nullable LootTable resolve(Object @Nullable [] delta) {
        if (delta == null || delta.length == 0 || delta[0] == null) {
            return null;
        }
        Object value = delta[0];
        if (value instanceof LootTable lootTable) {
            return lootTable;
        }
        if (value instanceof String string) {
            return Classes.parse(string, LootTable.class, ParseContext.DEFAULT);
        }
        return null;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        if (values != null) {
            return "loot table of " + values.toString(event, debug);
        }
        return matchedPattern == EVENT_BLOCK_PATTERN ? "loot table of event-block" : "loot table of event-entity";
    }
}
