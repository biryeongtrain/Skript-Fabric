package org.skriptlang.skript.bukkit.loottables.elements.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.loottables.LootContextWrapper;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class EffGenerateLoot extends Effect {

    private Expression<LootTable> lootTables;
    private @Nullable Expression<?> contexts;
    private Expression<?> targets;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (expressions.length != 2 && expressions.length != 3) {
            return false;
        }
        lootTables = (Expression<LootTable>) expressions[0];
        contexts = expressions.length == 3 ? expressions[1] : null;
        targets = expressions.length == 3 ? expressions[2] : expressions[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        LootContextWrapper context = resolveContext(event);
        if (context == null) {
            return;
        }
        for (Object target : targets.getAll(event)) {
            Container container = resolveContainer(target);
            if (container == null) {
                continue;
            }
            for (LootTable lootTable : lootTables.getAll(event)) {
                insertGeneratedItems(container, context.generate(lootTable, event.server()));
            }
        }
    }

    private @Nullable LootContextWrapper resolveContext(SkriptEvent event) {
        if (contexts != null) {
            Object value = contexts.getSingle(event);
            if (value instanceof LootContextWrapper wrapper) {
                return wrapper;
            }
            if (value instanceof FabricLocation location) {
                return new LootContextWrapper(location);
            }
        }
        if (event.level() == null) {
            return null;
        }
        return new LootContextWrapper(new FabricLocation(event.level(), new Vec3(0.0D, 0.0D, 0.0D)));
    }

    private @Nullable Container resolveContainer(@Nullable Object target) {
        if (target instanceof FabricInventory inventory) {
            return inventory.container();
        }
        if (target instanceof Container container) {
            return container;
        }
        if (target instanceof FabricBlock block) {
            BlockEntity blockEntity = block.level().getBlockEntity(block.position());
            return blockEntity instanceof Container container ? container : null;
        }
        if (target instanceof BlockEntity blockEntity) {
            return blockEntity instanceof Container container ? container : null;
        }
        if (target instanceof Entity entity) {
            return entity instanceof Container container ? container : null;
        }
        return null;
    }

    private void insertGeneratedItems(Container container, List<ItemStack> generated) {
        for (ItemStack stack : generated) {
            ItemStack remaining = stack.copy();
            for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
                ItemStack existing = container.getItem(slot);
                if (existing.isEmpty()) {
                    container.setItem(slot, remaining.copy());
                    remaining = ItemStack.EMPTY;
                    break;
                }
                if (!ItemStack.isSameItemSameComponents(existing, remaining)) {
                    continue;
                }
                int room = Math.min(existing.getMaxStackSize(), container.getMaxStackSize()) - existing.getCount();
                if (room <= 0) {
                    continue;
                }
                int moved = Math.min(room, remaining.getCount());
                existing.grow(moved);
                remaining.shrink(moved);
                container.setItem(slot, existing);
            }
        }
        container.setChanged();
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "generate loot using " + lootTables.toString(event, debug);
    }
}
