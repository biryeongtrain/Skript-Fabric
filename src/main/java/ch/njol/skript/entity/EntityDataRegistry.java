package ch.njol.skript.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;

final class EntityDataRegistry {

    private static final Map<String, EntityData<?>> BY_NAME = new LinkedHashMap<>();
    private static final Map<net.minecraft.world.entity.EntityType<?>, EntityData<?>> BY_TYPE = new LinkedHashMap<>();
    private static final List<EntityData<?>> ALL = new ArrayList<>();
    private static boolean initialized;

    private EntityDataRegistry() {
    }

    static synchronized void ensureInitialized() {
        if (initialized) {
            return;
        }

        registerSpecificExacts();
        for (net.minecraft.world.entity.EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (key == null) {
                continue;
            }
            register(SimpleEntityData.exact(humanize(key.getPath()), entityType));
        }

        alias("xp orb", net.minecraft.world.entity.EntityType.EXPERIENCE_ORB);
        alias("experience orb", net.minecraft.world.entity.EntityType.EXPERIENCE_ORB);
        alias("fish hook", net.minecraft.world.entity.EntityType.FISHING_BOBBER);
        alias("firework", net.minecraft.world.entity.EntityType.FIREWORK_ROCKET);
        alias("zombie pigman", net.minecraft.world.entity.EntityType.ZOMBIFIED_PIGLIN);

        register(SimpleEntityData.supertype("entity", Entity.class));
        register(SimpleEntityData.supertype("living entity", LivingEntity.class));
        register(SimpleEntityData.supertype("mob", Mob.class));
        register(SimpleEntityData.supertype("monster", Monster.class));
        register(SimpleEntityData.supertype("animal", Animal.class));
        register(SimpleEntityData.supertype("horse", AbstractHorse.class));
        register(SimpleEntityData.supertype("projectile", Projectile.class));
        register(SimpleEntityData.supertype("player", Player.class));
        register(SimpleEntityData.supertype("boat", Boat.class));
        register(SimpleEntityData.supertype("vehicle", VehicleEntity.class));
        register(SimpleEntityData.supertype("minecart", AbstractMinecart.class));
        register(SimpleEntityData.supertype("display", Display.class));
        register(SimpleEntityData.supertype("interaction", Interaction.class));
        register(SimpleEntityData.supertype("item", ItemEntity.class));

        initialized = true;
    }

    static @Nullable EntityData<?> parse(String input) {
        ensureInitialized();
        if (input == null || input.isBlank()) {
            return null;
        }
        String normalized = normalize(input);
        if (normalized.isEmpty()) {
            return null;
        }
        EntityData<?> exact = BY_NAME.get(normalized);
        if (exact != null) {
            return exact;
        }
        for (String candidate : singularCandidates(normalized)) {
            if (candidate.equals(normalized)) {
                continue;
            }
            EntityData<?> singularMatch = BY_NAME.get(candidate);
            if (singularMatch != null) {
                return singularMatch;
            }
        }
        for (EntityData<?> data : ALL) {
            String codeName = normalize(data.getCodeName());
            if (normalized.equals(codeName)) {
                return data;
            }
            for (String candidate : singularCandidates(normalized)) {
                if (candidate.equals(codeName)) {
                    return data;
                }
            }
        }
        return null;
    }

    static @Nullable EntityData<?> fromType(net.minecraft.world.entity.EntityType<?> entityType) {
        ensureInitialized();
        return BY_TYPE.get(entityType);
    }

    static @Nullable EntityData<?> fromClass(Class<? extends Entity> entityClass) {
        ensureInitialized();
        for (EntityData<?> data : ALL) {
            if (data instanceof SimpleEntityData simple && simple.isExactType() && data.getType() == entityClass) {
                return data;
            }
            if (data instanceof ExactEntityData<?> && data.getType() == entityClass) {
                return data;
            }
        }
        for (EntityData<?> data : ALL) {
            if (data.getType().isAssignableFrom(entityClass)) {
                return data;
            }
        }
        return null;
    }

    static Collection<EntityData<?>> all() {
        ensureInitialized();
        return List.copyOf(ALL);
    }

    private static void alias(String alias, net.minecraft.world.entity.EntityType<?> entityType) {
        EntityData<?> existing = BY_TYPE.get(entityType);
        if (existing == null) {
            register(SimpleEntityData.exact(alias, entityType));
            return;
        }
        BY_NAME.putIfAbsent(normalize(alias), existing);
    }

    private static void register(EntityData<?> data) {
        if (data instanceof SimpleEntityData simple && simple.isExactType() && BY_TYPE.containsKey(simple.getMinecraftType())) {
            return;
        }
        if (data instanceof ExactEntityData<?> exact && BY_TYPE.containsKey(exact.getMinecraftType())) {
            return;
        }
        ALL.add(data);
        BY_NAME.putIfAbsent(normalize(data.getCodeName()), data);
        if (data instanceof SimpleEntityData simple && simple.isExactType()) {
            BY_TYPE.putIfAbsent(simple.getMinecraftType(), data);
        }
        if (data instanceof ExactEntityData<?> exact) {
            BY_TYPE.putIfAbsent(exact.getMinecraftType(), data);
        }
    }

    private static void registerSpecificExacts() {
        register(new AxolotlData());
        register(new BeeData());
        register(new CatData());
        register(new ChickenData());
        register(new CowData());
        register(new CreeperData());
        register(new EndermanData());
        register(new FoxData());
        register(new FrogData());
        register(new GoatData());
        register(new LlamaData());
        register(new PandaData());
        register(new ParrotData());
        register(new PigData());
        register(new RabbitData());
        register(new SalmonData());
        register(new SheepData());
        register(new TropicalFishData());
        register(new VillagerData());
        register(new WolfData());
        register(new ZombieVillagerData());
    }

    private static String humanize(String path) {
        return normalize(path.replace('_', ' '));
    }

    private static String normalize(String input) {
        String lowered = input.trim().toLowerCase(Locale.ENGLISH).replace('_', ' ').replace('-', ' ');
        lowered = lowered.replaceAll("\\s+", " ");
        if (lowered.startsWith("a ")) {
            return lowered.substring(2);
        }
        if (lowered.startsWith("an ")) {
            return lowered.substring(3);
        }
        if (lowered.startsWith("the ")) {
            return lowered.substring(4);
        }
        return lowered;
    }

    private static List<String> singularCandidates(String value) {
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(value);
        if (value.endsWith("s") && !value.endsWith("ss")) {
            candidates.add(value.substring(0, value.length() - 1));
        }
        if (value.endsWith("ies") && value.length() > 3) {
            candidates.add(value.substring(0, value.length() - 3) + "y");
        }
        if (value.endsWith("ches") || value.endsWith("shes") || value.endsWith("xes")
                || value.endsWith("oes")) {
            candidates.add(value.substring(0, value.length() - 2));
        }
        return List.copyOf(candidates);
    }
}
