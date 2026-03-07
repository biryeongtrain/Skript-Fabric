package org.skriptlang.skript.bukkit.loottables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;

public final class LootTableUtils {

    private LootTableUtils() {
    }

    public static boolean isLootable(@Nullable Object object) {
        Object target = resolveTarget(object);
        return target instanceof ContainerEntity
                || target instanceof RandomizableContainer
                || findGenericLootTableMethod(target) != null;
    }

    public static @Nullable LootTable getLootTable(@Nullable Object object) {
        Object target = resolveTarget(object);
        if (target == null) {
            return null;
        }
        if (target instanceof ContainerEntity containerEntity) {
            return wrap(containerEntity.getContainerLootTable());
        }
        if (target instanceof RandomizableContainer randomizableContainer) {
            return wrap(randomizableContainer.getLootTable());
        }
        Method method = findGenericLootTableMethod(target);
        if (method == null) {
            return null;
        }
        try {
            return wrap(extractOptional(method.invoke(target)));
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to read Minecraft loot table state.", exception);
        }
    }

    public static @Nullable Long getLootTableSeed(@Nullable Object object) {
        Object target = resolveTarget(object);
        if (target == null) {
            return null;
        }
        if (target instanceof ContainerEntity containerEntity) {
            return containerEntity.getContainerLootTableSeed();
        }
        if (target instanceof RandomizableContainer randomizableContainer) {
            return randomizableContainer.getLootTableSeed();
        }
        Method method = findMethod(target, 0, "getLootTableSeed", "getContainerLootTableSeed");
        if (method == null) {
            return null;
        }
        try {
            Object value = method.invoke(target);
            return value instanceof Number number ? number.longValue() : null;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to read Minecraft loot table seed state.", exception);
        }
    }

    public static void setLootTable(@Nullable Object object, @Nullable LootTable lootTable) {
        Object target = resolveTarget(object);
        if (target == null || !isLootable(target)) {
            return;
        }

        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key = lootTable == null ? null : lootTable.key();
        if (key != null) {
            Long currentSeed = getLootTableSeed(target);
            if (invokeCombinedLootTableSetter(target, key, currentSeed != null ? currentSeed : 0L)) {
                updateState(target);
                return;
            }
        }
        if (target instanceof ContainerEntity containerEntity) {
            containerEntity.setContainerLootTable(key);
            updateState(target);
            return;
        }
        if (target instanceof RandomizableContainer randomizableContainer) {
            randomizableContainer.setLootTable(key);
            updateState(target);
            return;
        }

        Method method = findMethod(target, 1, "setLootTable", "setContainerLootTable");
        if (method == null) {
            return;
        }
        try {
            method.invoke(target, key);
            updateState(target);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to write Minecraft loot table state.", exception);
        }
    }

    public static void setLootTableSeed(@Nullable Object object, long seed) {
        Object target = resolveTarget(object);
        if (target == null) {
            return;
        }
        LootTable currentLootTable = getLootTable(target);
        if (currentLootTable != null && invokeCombinedLootTableSetter(target, currentLootTable.key(), seed)) {
            updateState(target);
            return;
        }
        if (target instanceof ContainerEntity containerEntity) {
            containerEntity.setContainerLootTableSeed(seed);
            updateState(target);
            return;
        }
        if (target instanceof RandomizableContainer randomizableContainer) {
            randomizableContainer.setLootTableSeed(seed);
            updateState(target);
            return;
        }

        Method method = findMethod(target, 1, "setLootTableSeed", "setContainerLootTableSeed");
        if (method == null) {
            return;
        }
        try {
            method.invoke(target, seed);
            updateState(target);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to write Minecraft loot table seed state.", exception);
        }
    }

    private static @Nullable Object resolveTarget(@Nullable Object object) {
        if (object instanceof FabricBlock block) {
            return block.level().getBlockEntity(block.position());
        }
        return object;
    }

    private static @Nullable Method findGenericLootTableMethod(@Nullable Object target) {
        return findMethod(target, 0, "getLootTable", "getContainerLootTable");
    }

    private static @Nullable Object extractOptional(@Nullable Object value) {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private static @Nullable LootTable wrap(@Nullable Object value) {
        if (value instanceof LootTable lootTable) {
            return lootTable;
        }
        if (value instanceof ResourceKey<?> resourceKey) {
            return new LootTable((ResourceKey<net.minecraft.world.level.storage.loot.LootTable>) resourceKey);
        }
        return null;
    }

    private static boolean invokeCombinedLootTableSetter(
            Object target,
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key,
            long seed
    ) {
        Method method = findMethod(target, 2, "setLootTable", "setContainerLootTable");
        if (method == null) {
            return false;
        }
        try {
            method.invoke(target, key, seed);
            return true;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to write combined Minecraft loot table state.", exception);
        }
    }

    private static @Nullable Method findMethod(@Nullable Object target, int parameterCount, String... names) {
        if (target == null) {
            return null;
        }
        for (String name : names) {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(name) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        return null;
    }

    private static void updateState(@Nullable Object target) {
        if (!(target instanceof BlockEntity blockEntity)) {
            return;
        }
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, 3);
    }
}
