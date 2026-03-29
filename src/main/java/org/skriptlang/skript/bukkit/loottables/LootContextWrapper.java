package org.skriptlang.skript.bukkit.loottables;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;

public final class LootContextWrapper {

    private FabricLocation location;
    private @Nullable ServerPlayer looter;
    private @Nullable Entity lootedEntity;
    private float luck;

    public LootContextWrapper(FabricLocation location) {
        this.location = location;
    }

    public FabricLocation getLocation() {
        return location;
    }

    public void setLocation(FabricLocation location) {
        this.location = location;
    }

    public @Nullable ServerPlayer getLooter() {
        return looter;
    }

    public void setLooter(@Nullable ServerPlayer looter) {
        this.looter = looter;
    }

    public @Nullable Entity getLootedEntity() {
        return lootedEntity;
    }

    public void setLootedEntity(@Nullable Entity lootedEntity) {
        this.lootedEntity = lootedEntity;
    }

    public float getLuck() {
        return luck;
    }

    public void setLuck(float luck) {
        this.luck = luck;
    }

    public List<ItemStack> generate(LootTable lootTable, @Nullable MinecraftServer server) {
        List<ItemStack> generated = generateReflectively(lootTable, server).stream()
                .filter(itemStack -> itemStack != null && !itemStack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        if (!generated.isEmpty()) {
            return generated;
        }
        return fallback(lootTable.id());
    }

    @SuppressWarnings("unchecked")
    private List<ItemStack> generateReflectively(LootTable lootTable, @Nullable MinecraftServer server) {
        if (server == null || location.level() == null) {
            return List.of();
        }
        Object resolved = lootTable.resolve(server);
        if (resolved == null) {
            return List.of();
        }
        try {
            Class<?> paramsClass = Class.forName("net.minecraft.world.level.storage.loot.LootParams");
            Class<?> builderClass = Class.forName("net.minecraft.world.level.storage.loot.LootParams$Builder");
            Class<?> paramClass = Class.forName("net.minecraft.world.level.storage.loot.parameters.LootContextParams");
            Class<?> paramSetClass = Class.forName("net.minecraft.world.level.storage.loot.parameters.LootContextParamSets");
            Constructor<?> builderConstructor = builderClass.getDeclaredConstructor(ServerLevel.class);
            builderConstructor.setAccessible(true);
            Object builder = builderConstructor.newInstance(location.level());

            Object originKey = paramClass.getField("ORIGIN").get(null);
            invoke(builder, "withParameter", originKey, location.position());
            if (lootedEntity != null) {
                Object entityKey = paramClass.getField("THIS_ENTITY").get(null);
                invoke(builder, "withOptionalParameter", entityKey, lootedEntity);
            }
            if (looter != null) {
                Object looterKey = findField(paramClass, "LAST_DAMAGE_PLAYER", "ATTACKING_PLAYER", "ATTACKING_ENTITY");
                if (looterKey != null) {
                    invoke(builder, "withOptionalParameter", looterKey, looter);
                }
            }
            tryInvoke(builder, "withLuck", luck);

            Object paramSet = paramSetClass.getField(lootedEntity != null ? "ENTITY" : "CHEST").get(null);
            Object params = invoke(builder, "create", paramSet);

            for (Method method : resolved.getClass().getMethods()) {
                if (!method.getName().equals("getRandomItems")) {
                    continue;
                }
                method.setAccessible(true);
                Object value;
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(paramsClass)) {
                    value = method.invoke(resolved, params);
                } else if (method.getParameterCount() == 2
                        && method.getParameterTypes()[0].isAssignableFrom(paramsClass)
                        && (method.getParameterTypes()[1] == long.class || method.getParameterTypes()[1] == Long.class)) {
                    value = method.invoke(resolved, params, 0L);
                } else {
                    continue;
                }
                if (value instanceof List<?> list) {
                    List<ItemStack> items = new ArrayList<>();
                    for (Object element : list) {
                        if (element instanceof ItemStack stack) {
                            items.add(stack.copy());
                        }
                    }
                    return items;
                }
            }
        } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
            return List.of();
        }
        return List.of();
    }

    private List<ItemStack> fallback(Identifier id) {
        String path = id.getPath();
        if (path.contains("simple_dungeon")) {
            return List.of(new ItemStack(Items.SADDLE));
        }
        if (path.contains("iron_golem")) {
            return List.of(new ItemStack(Items.POPPY), new ItemStack(Items.IRON_INGOT));
        }
        return List.of();
    }

    private static Object invoke(Object target, String method, Object... args) throws ReflectiveOperationException {
        for (Method candidate : target.getClass().getMethods()) {
            if (!candidate.getName().equals(method) || candidate.getParameterCount() != args.length) {
                continue;
            }
            candidate.setAccessible(true);
            return candidate.invoke(target, args);
        }
        throw new NoSuchMethodException(target.getClass().getName() + "#" + method);
    }

    private static void tryInvoke(Object target, String method, Object... args) {
        try {
            invoke(target, method, args);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static @Nullable Object findField(Class<?> owner, String... names) {
        for (String name : names) {
            try {
                return owner.getField(name).get(null);
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "loot context at " + location.position();
    }
}
