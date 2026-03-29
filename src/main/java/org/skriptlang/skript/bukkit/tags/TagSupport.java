package org.skriptlang.skript.bukkit.tags;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class TagSupport {

    private static final Map<Identifier, List<Item>> CUSTOM_ITEM_TAGS = new ConcurrentHashMap<>();
    private static final Map<Identifier, List<Block>> CUSTOM_BLOCK_TAGS = new ConcurrentHashMap<>();
    private static final Map<Identifier, List<EntityType<?>>> CUSTOM_ENTITY_TAGS = new ConcurrentHashMap<>();

    private TagSupport() {
    }

    public static boolean isTagged(@Nullable Object element, @Nullable Object rawTag) {
        MinecraftTag tag = asTag(rawTag, MinecraftTag.Target.ANY, "minecraft");
        Identifier tagId = tag == null ? null : tag.id();
        if (tagId == null || element == null) {
            return false;
        }
        if (element instanceof ItemStack itemStack) {
            return itemStack.is(TagKey.create(Registries.ITEM, tagId))
                    || CUSTOM_ITEM_TAGS.getOrDefault(tagId, List.of()).contains(itemStack.getItem());
        }
        if (element instanceof FabricItemType itemType) {
            Item item = itemType.item();
            return item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, tagId))
                    || CUSTOM_ITEM_TAGS.getOrDefault(tagId, List.of()).contains(item);
        }
        if (element instanceof Item item) {
            return item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, tagId))
                    || CUSTOM_ITEM_TAGS.getOrDefault(tagId, List.of()).contains(item);
        }
        if (element instanceof Entity entity) {
            return entity.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, tagId))
                    || CUSTOM_ENTITY_TAGS.getOrDefault(tagId, List.of()).contains(entity.getType());
        }
        if (element instanceof EntityType<?> entityType) {
            return entityType.builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, tagId))
                    || CUSTOM_ENTITY_TAGS.getOrDefault(tagId, List.of()).contains(entityType);
        }
        if (element instanceof FabricBlock fabricBlock) {
            return fabricBlock.state().is(TagKey.create(Registries.BLOCK, tagId))
                    || CUSTOM_BLOCK_TAGS.getOrDefault(tagId, List.of()).contains(fabricBlock.state().getBlock());
        }
        if (element instanceof BlockState blockState) {
            return blockState.is(TagKey.create(Registries.BLOCK, tagId))
                    || CUSTOM_BLOCK_TAGS.getOrDefault(tagId, List.of()).contains(blockState.getBlock());
        }
        if (element instanceof Block block) {
            return block.builtInRegistryHolder().is(TagKey.create(Registries.BLOCK, tagId))
                    || CUSTOM_BLOCK_TAGS.getOrDefault(tagId, List.of()).contains(block);
        }
        return false;
    }

    public static void registerCustomTag(MinecraftTag.Target target, Identifier id, Object[] contents) {
        switch (target) {
            case ITEM -> CUSTOM_ITEM_TAGS.put(id, normalizeItems(contents));
            case BLOCK -> CUSTOM_BLOCK_TAGS.put(id, normalizeBlocks(contents));
            case ENTITY -> CUSTOM_ENTITY_TAGS.put(id, normalizeEntities(contents));
            case ANY -> {
            }
        }
    }

    public static @Nullable Identifier parseTagId(@Nullable String rawTag, String defaultNamespace) {
        if (rawTag == null || rawTag.isBlank()) {
            return null;
        }
        String normalized = rawTag.trim();
        try {
            if (!normalized.contains(":")) {
                normalized = defaultNamespace + ":" + normalized;
            }
            return MinecraftResourceParser.parse(normalized);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    public static @Nullable MinecraftTag asTag(@Nullable Object rawTag, MinecraftTag.Target defaultTarget, String defaultNamespace) {
        if (rawTag instanceof MinecraftTag tag) {
            return tag;
        }
        if (rawTag instanceof Identifier resourceLocation) {
            return new MinecraftTag(resourceLocation, defaultTarget);
        }
        if (!(rawTag instanceof String string)) {
            return null;
        }
        Identifier id = parseTagId(string, defaultNamespace);
        return id == null ? null : new MinecraftTag(id, defaultTarget);
    }

    public static List<MinecraftTag> tagsOf(@Nullable Object value, MinecraftTag.Target requestedTarget) {
        List<MinecraftTag> tags = new ArrayList<>();
        if (value instanceof ItemStack itemStack) {
            addItemTags(tags, itemStack.getItem().builtInRegistryHolder(), requestedTarget);
            addCustomItemTags(tags, itemStack.getItem(), requestedTarget);
        } else if (value instanceof FabricItemType itemType) {
            addItemTags(tags, itemType.item().builtInRegistryHolder(), requestedTarget);
            addCustomItemTags(tags, itemType.item(), requestedTarget);
        } else if (value instanceof Item item) {
            addItemTags(tags, item.builtInRegistryHolder(), requestedTarget);
            addCustomItemTags(tags, item, requestedTarget);
        } else if (value instanceof FabricBlock block) {
            addBlockTags(tags, block.state().getBlock().builtInRegistryHolder(), requestedTarget);
            addCustomBlockTags(tags, block.state().getBlock(), requestedTarget);
        } else if (value instanceof BlockState state) {
            addBlockTags(tags, state.getBlock().builtInRegistryHolder(), requestedTarget);
            addCustomBlockTags(tags, state.getBlock(), requestedTarget);
        } else if (value instanceof Block block) {
            addBlockTags(tags, block.builtInRegistryHolder(), requestedTarget);
            addCustomBlockTags(tags, block, requestedTarget);
        } else if (value instanceof Entity entity) {
            addEntityTags(tags, entity.getType().builtInRegistryHolder(), requestedTarget);
            addCustomEntityTags(tags, entity.getType(), requestedTarget);
        } else if (value instanceof EntityType<?> entityType) {
            addEntityTags(tags, entityType.builtInRegistryHolder(), requestedTarget);
            addCustomEntityTags(tags, entityType, requestedTarget);
        }
        tags.sort(Comparator.comparing(MinecraftTag::toString));
        return tags;
    }

    public static List<Object> contents(MinecraftTag tag) {
        List<Object> values = new ArrayList<>();
        if (tag.target() == MinecraftTag.Target.ANY || tag.target() == MinecraftTag.Target.ITEM) {
            TagKey<Item> key = TagKey.create(Registries.ITEM, tag.id());
            for (Item item : BuiltInRegistries.ITEM) {
                if (item.builtInRegistryHolder().is(key)) {
                    values.add(new FabricItemType(item));
                }
            }
            CUSTOM_ITEM_TAGS.getOrDefault(tag.id(), List.of()).forEach(item -> values.add(new FabricItemType(item)));
        }
        if (tag.target() == MinecraftTag.Target.ANY || tag.target() == MinecraftTag.Target.BLOCK) {
            TagKey<Block> key = TagKey.create(Registries.BLOCK, tag.id());
            for (Block block : BuiltInRegistries.BLOCK) {
                if (!block.builtInRegistryHolder().is(key)) {
                    continue;
                }
                Item blockItem = block.asItem();
                if (blockItem != null) {
                    values.add(new FabricItemType(blockItem));
                }
            }
            for (Block block : CUSTOM_BLOCK_TAGS.getOrDefault(tag.id(), List.of())) {
                Item blockItem = block.asItem();
                if (blockItem != null) {
                    values.add(new FabricItemType(blockItem));
                }
            }
        }
        if (tag.target() == MinecraftTag.Target.ANY || tag.target() == MinecraftTag.Target.ENTITY) {
            TagKey<EntityType<?>> key = TagKey.create(Registries.ENTITY_TYPE, tag.id());
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (entityType.builtInRegistryHolder().is(key)) {
                    values.add(entityType);
                }
            }
            values.addAll(CUSTOM_ENTITY_TAGS.getOrDefault(tag.id(), List.of()));
        }
        return values;
    }

    public static List<MinecraftTag> allTags(MinecraftTag.Target target) {
        List<MinecraftTag> tags = new ArrayList<>();
        if (target == MinecraftTag.Target.ANY || target == MinecraftTag.Target.ITEM) {
            BuiltInRegistries.ITEM.getTags().forEach(named -> tags.add(new MinecraftTag(named.key().location(), MinecraftTag.Target.ITEM)));
            CUSTOM_ITEM_TAGS.keySet().forEach(id -> tags.add(new MinecraftTag(id, MinecraftTag.Target.ITEM)));
        }
        if (target == MinecraftTag.Target.ANY || target == MinecraftTag.Target.BLOCK) {
            BuiltInRegistries.BLOCK.getTags().forEach(named -> tags.add(new MinecraftTag(named.key().location(), MinecraftTag.Target.BLOCK)));
            CUSTOM_BLOCK_TAGS.keySet().forEach(id -> tags.add(new MinecraftTag(id, MinecraftTag.Target.BLOCK)));
        }
        if (target == MinecraftTag.Target.ANY || target == MinecraftTag.Target.ENTITY) {
            BuiltInRegistries.ENTITY_TYPE.getTags().forEach(named -> tags.add(new MinecraftTag(named.key().location(), MinecraftTag.Target.ENTITY)));
            CUSTOM_ENTITY_TAGS.keySet().forEach(id -> tags.add(new MinecraftTag(id, MinecraftTag.Target.ENTITY)));
        }
        tags.sort(Comparator.comparing(MinecraftTag::toString));
        return tags;
    }

    private static void addItemTags(List<MinecraftTag> tags, Holder<Item> holder, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.ITEM) {
            return;
        }
        holder.tags().forEach(tag -> tags.add(new MinecraftTag(tag.location(), MinecraftTag.Target.ITEM)));
    }

    private static void addBlockTags(List<MinecraftTag> tags, Holder<Block> holder, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.BLOCK) {
            return;
        }
        holder.tags().forEach(tag -> tags.add(new MinecraftTag(tag.location(), MinecraftTag.Target.BLOCK)));
    }

    private static void addEntityTags(List<MinecraftTag> tags, Holder<EntityType<?>> holder, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.ENTITY) {
            return;
        }
        holder.tags().forEach(tag -> tags.add(new MinecraftTag(tag.location(), MinecraftTag.Target.ENTITY)));
    }

    private static void addCustomItemTags(List<MinecraftTag> tags, Item item, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.ITEM) {
            return;
        }
        CUSTOM_ITEM_TAGS.forEach((id, values) -> {
            if (values.contains(item)) {
                tags.add(new MinecraftTag(id, MinecraftTag.Target.ITEM));
            }
        });
    }

    private static void addCustomBlockTags(List<MinecraftTag> tags, Block block, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.BLOCK) {
            return;
        }
        CUSTOM_BLOCK_TAGS.forEach((id, values) -> {
            if (values.contains(block)) {
                tags.add(new MinecraftTag(id, MinecraftTag.Target.BLOCK));
            }
        });
    }

    private static void addCustomEntityTags(List<MinecraftTag> tags, EntityType<?> entityType, MinecraftTag.Target requestedTarget) {
        if (requestedTarget != MinecraftTag.Target.ANY && requestedTarget != MinecraftTag.Target.ENTITY) {
            return;
        }
        CUSTOM_ENTITY_TAGS.forEach((id, values) -> {
            if (values.contains(entityType)) {
                tags.add(new MinecraftTag(id, MinecraftTag.Target.ENTITY));
            }
        });
    }

    private static List<Item> normalizeItems(Object[] contents) {
        List<Item> values = new ArrayList<>();
        for (Object value : contents) {
            if (value instanceof ItemStack itemStack) {
                values.add(itemStack.getItem());
            } else if (value instanceof FabricItemType itemType) {
                values.add(itemType.item());
            } else if (value instanceof Item item) {
                values.add(item);
            } else if (value instanceof Block block && block.asItem() != null) {
                values.add(block.asItem());
            } else if (value instanceof BlockState blockState && blockState.getBlock().asItem() != null) {
                values.add(blockState.getBlock().asItem());
            } else if (value instanceof FabricBlock block && block.state().getBlock().asItem() != null) {
                values.add(block.state().getBlock().asItem());
            }
        }
        return values;
    }

    private static List<Block> normalizeBlocks(Object[] contents) {
        List<Block> values = new ArrayList<>();
        for (Object value : contents) {
            if (value instanceof FabricBlock block) {
                values.add(block.state().getBlock());
            } else if (value instanceof BlockState blockState) {
                values.add(blockState.getBlock());
            } else if (value instanceof Block block) {
                values.add(block);
            } else if (value instanceof FabricItemType itemType) {
                Block block = Block.byItem(itemType.item());
                if (block != Blocks.AIR) {
                    values.add(block);
                }
            } else if (value instanceof ItemStack itemStack) {
                Block block = Block.byItem(itemStack.getItem());
                if (block != Blocks.AIR) {
                    values.add(block);
                }
            } else if (value instanceof Item item) {
                Block block = Block.byItem(item);
                if (block != Blocks.AIR) {
                    values.add(block);
                }
            }
        }
        return values;
    }

    private static List<EntityType<?>> normalizeEntities(Object[] contents) {
        List<EntityType<?>> values = new ArrayList<>();
        for (Object value : contents) {
            if (value instanceof Entity entity) {
                values.add(entity.getType());
            } else if (value instanceof EntityType<?> entityType) {
                values.add(entityType);
            } else if (value instanceof String raw) {
                Identifier id = parseTagId(raw, "minecraft");
                EntityType<?> entityType = id == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(id);
                if (entityType != null) {
                    values.add(entityType);
                }
            }
        }
        return values;
    }
}
