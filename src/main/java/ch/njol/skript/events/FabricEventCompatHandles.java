package ch.njol.skript.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import java.util.Set;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.FabricBlockEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricItemEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricTimeAwareItemEventHandle;

public final class FabricEventCompatHandles {

    private FabricEventCompatHandles() {
    }

    public record GameMode(@Nullable GameType mode) {
    }

    public enum ResourcePackState {
        ACCEPTED, DECLINED, FAILED_DOWNLOAD, SUCCESSFULLY_LOADED,
        DOWNLOADED, INVALID_URL, FAILED_RELOAD, DISCARDED
    }

    public record ResourcePackResponse(@Nullable ResourcePackState status) {
    }

    public record Healing(Entity entity, @Nullable String reason, float amount) implements FabricEntityEventHandle {
    }

    public record Portal(Entity entity, boolean player) implements FabricEntityEventHandle {
    }

    public enum WeatherType { CLEAR, RAIN, THUNDER }

    public record WeatherChange(boolean rain, boolean thunder) {
    }

    public enum WorldAction {
        SAVE,
        INIT,
        UNLOAD,
        LOAD
    }

    public record World(ServerLevel world, WorldAction action) {
    }

    public record MoveOn(BlockState blockState) {
    }

    public record EntityTransform(Entity entity, @Nullable String reason) implements FabricEntityEventHandle {
    }

    public record EntityTarget(Entity entity, @Nullable Entity target) implements FabricEntityEventHandle {
    }

    public record EntityShootBow(LivingEntity entity, @Nullable ItemStack consumable, float force) {
    }

    public record AreaEffectCloudApply(@Nullable java.util.List<LivingEntity> affectedEntities) {
    }

    public record ExperienceSpawn(int amount) {
    }

    public record ExperienceCooldownChange(@Nullable String reason) {
    }

    public record Firework(@Nullable Set<Integer> colors) {
    }

    public enum LeashAction {
        LEASH,
        PLAYER_LEASH,
        UNLEASH,
        PLAYER_UNLEASH
    }

    public record Leash(Entity entity, LeashAction action) {
    }

    public record HarvestBlock(BlockState blockState) {
    }

    public enum ArmorSlot {
        HELMET, CHESTPLATE, LEGGINGS, BOOTS
    }

    public record PlayerArmorChange(@Nullable ArmorSlot slot) {
    }

    public enum ClickType {
        RIGHT,
        LEFT
    }

    public record Click(
            ServerLevel level,
            BlockPos position,
            ClickType clickType,
            @Nullable Entity entity,
            @Nullable BlockState blockState,
            @Nullable ItemStack tool
    ) implements FabricBlockEventHandle, FabricEntityEventHandle, FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return tool == null ? ItemStack.EMPTY : tool;
        }
    }

    public record BookEdit(ItemStack previous, ItemStack current, boolean signing) implements FabricTimeAwareItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return current;
        }

        @Override
        public ItemStack itemStack(int time) {
            return time == 1 ? previous : current;
        }
    }

    public record BeaconEffect(ServerLevel level, BlockPos position, boolean primary, @Nullable Object effectType) implements FabricBlockEventHandle {
    }

    public record BeaconToggle(ServerLevel level, BlockPos position, boolean activated) implements FabricBlockEventHandle {
    }

    public enum BlockAction {
        BREAK,
        BURN,
        PLACE,
        FADE,
        FORM,
        DROP
    }

    public record Block(
            ServerLevel level,
            BlockPos position,
            BlockAction action,
            @Nullable BlockState blockState,
            @Nullable ItemStack itemStack,
            boolean dropped
    ) implements FabricBlockEventHandle, FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return itemStack == null ? ItemStack.EMPTY : itemStack;
        }
    }

    public record EntityLifecycle(Entity entity, boolean spawn, @Nullable SpawnReason spawnReason) implements FabricEntityEventHandle {
    }

    public record EntityBlockChange(ServerLevel level, BlockPos position, Entity entity, @Nullable BlockState from, @Nullable BlockState to)
            implements FabricBlockEventHandle, FabricEntityEventHandle {
    }

    public record Grow(ServerLevel level, BlockPos position, @Nullable BlockState from, @Nullable BlockState to, @Nullable String structureType)
            implements FabricBlockEventHandle {
    }

    public enum ItemAction {
        DISPENSE,
        SPAWN,
        DROP,
        PREPARE_CRAFT,
        CRAFT,
        PICKUP,
        CONSUME,
        INVENTORY_CLICK,
        DESPAWN,
        MERGE,
        INVENTORY_MOVE,
        STONECUTTING
    }

    public record Item(ServerLevel level, BlockPos position, ItemAction action, @Nullable ItemStack itemStack, boolean entityEvent)
            implements FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return itemStack == null ? ItemStack.EMPTY : itemStack;
        }
    }

    public static final class PiglinBarter {

        private final @Nullable ItemStack input;
        private final java.util.List<ItemStack> outcome;

        public PiglinBarter(@Nullable ItemStack input) {
            this(input, new java.util.ArrayList<>());
        }

        public PiglinBarter(@Nullable ItemStack input, java.util.List<ItemStack> outcome) {
            this.input = input;
            this.outcome = outcome;
        }

        public @Nullable ItemStack input() {
            return input;
        }

        public java.util.List<ItemStack> outcome() {
            return outcome;
        }
    }

    public static final class Explosion {

        private final @Nullable java.util.List<FabricBlock> explodedBlocks;
        private float yield;

        public Explosion(@Nullable java.util.List<FabricBlock> explodedBlocks) {
            this(explodedBlocks, 1.0F);
        }

        public Explosion(@Nullable java.util.List<FabricBlock> explodedBlocks, float yield) {
            this.explodedBlocks = explodedBlocks;
            this.yield = yield;
        }

        public @Nullable java.util.List<FabricBlock> explodedBlocks() {
            return explodedBlocks;
        }

        public float yield() {
            return yield;
        }

        public void setYield(float yield) {
            this.yield = yield;
        }
    }

    public record BlockFertilize(@Nullable java.util.List<FabricBlock> blocks) {
    }

    public record PlayerRespawn(
            @Nullable FabricLocation respawnLocation,
            boolean bedSpawn,
            boolean anchorSpawn,
            @Nullable String reason
    ) {
    }

    public record PlantGrowth(ServerLevel level, BlockPos position, @Nullable BlockState from, @Nullable BlockState to)
            implements FabricBlockEventHandle {
    }

    public record PressurePlate(ServerLevel level, BlockPos position, boolean tripwire) implements FabricBlockEventHandle {
    }

    public record VehicleCollision(ServerLevel level, BlockPos position, Entity vehicle, @Nullable BlockState blockState, @Nullable Entity entity)
            implements FabricBlockEventHandle, FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return entity != null ? entity : vehicle;
        }
    }

    public record Jump() {
    }

    public record HandItemSwap() {
    }

    public static @Nullable String effectName(@Nullable net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect) {
        if (effect == null) {
            return null;
        }
        return effect.unwrapKey()
                .map(key -> key.location().getPath())
                .orElseGet(() -> BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).getPath());
    }

    public record EnchantPrepare(
            ItemStack item,
            int enchantmentBonus,
            @Nullable List<EnchantmentInstance> offers
    ) implements FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return item;
        }
    }

    public record EnchantApply(
            ItemStack item,
            List<EnchantmentInstance> enchantments,
            int cost
    ) implements FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return item;
        }
    }

    public record Mending(
            LivingEntity entity,
            ItemStack item,
            int repairAmount,
            @Nullable ExperienceOrb experienceOrb
    ) implements FabricEntityEventHandle, FabricItemEventHandle {
        @Override
        public ItemStack itemStack() {
            return item;
        }
    }

    public record Chat(
            String message,
            @Nullable List<ServerPlayer> recipients
    ) {
    }
}
