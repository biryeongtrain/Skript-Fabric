package org.skriptlang.skript.fabric.compat;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;

public final class StructureType {

    private static final Map<String, StructureType> BY_NAME = new LinkedHashMap<>();

    static {
        register("tree", key("oak"));
        register("oak", key("oak"));
        register("big tree", key("fancy_oak"));
        register("fancy oak", key("fancy_oak"));
        register("birch", key("birch"));
        register("tall birch", key("super_birch_bees_0002"));
        register("redwood", key("spruce"));
        register("spruce", key("spruce"));
        register("tall redwood", key("pine"));
        register("pine", key("pine"));
        register("mega redwood", key("mega_spruce"));
        register("mega spruce", key("mega_spruce"));
        register("jungle tree", key("jungle_tree_no_vine"));
        register("small jungle tree", key("jungle_tree_no_vine"));
        register("jungle bush", key("jungle_bush"));
        register("cocoa tree", key("jungle_tree_no_vine"));
        register("big jungle tree", key("mega_jungle_tree"));
        register("mega jungle tree", key("mega_jungle_tree"));
        register("acacia", key("acacia"));
        register("dark oak", key("dark_oak"));
        register("cherry", key("cherry"));
        register("mangrove", key("mangrove"));
        register("tall mangrove", key("tall_mangrove"));
        register("azalea", key("azalea_tree"));
        register("mushroom", key("huge_brown_mushroom"));
        register("brown mushroom", key("huge_brown_mushroom"));
        register("red mushroom", key("huge_red_mushroom"));
        register("crimson fungus", key("crimson_fungus"));
        register("warped fungus", key("warped_fungus"));
        register("chorus plant", key("chorus_plant"));
    }

    private final ResourceKey<ConfiguredFeature<?, ?>> featureKey;
    private final String name;

    private StructureType(String name, ResourceKey<ConfiguredFeature<?, ?>> featureKey) {
        this.name = name;
        this.featureKey = featureKey;
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.withDefaultNamespace(path));
    }

    private static void register(String name, ResourceKey<ConfiguredFeature<?, ?>> featureKey) {
        BY_NAME.put(name.toLowerCase(Locale.ENGLISH), new StructureType(name, featureKey));
    }

    public static @Nullable StructureType fromName(String name) {
        return BY_NAME.get(name.toLowerCase(Locale.ENGLISH).trim());
    }

    public static Map<String, StructureType> all() {
        return BY_NAME;
    }

    public String name() {
        return name;
    }

    public ResourceKey<ConfiguredFeature<?, ?>> key() {
        return featureKey;
    }

    public boolean place(ServerLevel level, BlockPos pos) {
        return level.registryAccess()
                .lookup(Registries.CONFIGURED_FEATURE)
                .flatMap(reg -> reg.get(featureKey))
                .map(holder -> holder.value().place(level, level.getChunkSource().getGenerator(), RandomSource.create(), pos))
                .orElse(false);
    }

    @Override
    public String toString() {
        return name;
    }
}
