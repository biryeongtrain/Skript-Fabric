package org.skriptlang.skript.bukkit.loottables.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.world.phys.Vec3;

public final class ExprLootItems extends SimpleExpression<ItemStack> {

    private Expression<LootTable> lootTables;
    private @Nullable Expression<LootContextWrapper> contexts;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (exprs.length == 0) {
            return false;
        }
        lootTables = (Expression<LootTable>) exprs[0];
        if (exprs.length > 1) {
            contexts = (Expression<LootContextWrapper>) exprs[1];
        }
        return true;
    }

    @Override
    protected ItemStack @Nullable [] get(SkriptEvent event) {
        LootContextWrapper context = contexts != null ? contexts.getSingle(event) : defaultContext(event);
        if (context == null) {
            return new ItemStack[0];
        }
        List<ItemStack> items = new ArrayList<>();
        for (LootTable lootTable : lootTables.getAll(event)) {
            items.addAll(context.generate(lootTable, event.server()));
        }
        return items.toArray(ItemStack[]::new);
    }

    private @Nullable LootContextWrapper defaultContext(SkriptEvent event) {
        if (event.level() == null) {
            return null;
        }
        return new LootContextWrapper(new FabricLocation(event.level(), new Vec3(0.0D, 0.0D, 0.0D)));
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "loot items of " + lootTables.toString(event, debug);
    }
}
