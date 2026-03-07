package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class ExprLootTableFromString extends SimpleExpression<LootTable> {

    private Expression<String> keys;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 1 || !expressions[0].canReturn(String.class)) {
            return false;
        }
        if (expressions[0] instanceof Literal<?> literal) {
            Object literalValue = literal.getSingle(null);
            if (literalValue instanceof String string && looksLikePropertyContinuation(string)) {
                return false;
            }
        }
        keys = (Expression<String>) expressions[0];
        return true;
    }

    private static boolean looksLikePropertyContinuation(String value) {
        String normalized = value.trim().toLowerCase();
        return normalized.startsWith("of ")
                || normalized.equals("of")
                || normalized.startsWith("seed ")
                || normalized.equals("seed")
                || normalized.startsWith("seeds ")
                || normalized.equals("seeds");
    }

    @Override
    protected LootTable @Nullable [] get(SkriptEvent event) {
        List<LootTable> lootTables = new ArrayList<>();
        for (String key : keys.getAll(event)) {
            LootTable lootTable = Classes.parse(key, LootTable.class, ParseContext.DEFAULT);
            if (lootTable != null) {
                lootTables.add(lootTable);
            }
        }
        return lootTables.toArray(LootTable[]::new);
    }

    @Override
    public boolean isSingle() {
        return keys.isSingle();
    }

    @Override
    public Class<? extends LootTable> getReturnType() {
        return LootTable.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loot table " + keys.toString(event, debug);
    }
}
