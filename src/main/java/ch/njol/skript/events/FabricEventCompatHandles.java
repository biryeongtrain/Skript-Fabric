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
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
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

    public static final class ExperienceSpawn {
        private int amount;

        public ExperienceSpawn(int amount) {
            this.amount = amount;
        }

        public int amount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
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

    public record SneakToggle(boolean sneaking) {
    }

    public record SprintToggle(boolean sprinting) {
    }

    public record FlightToggle(boolean flying) {
    }

    public record GlideToggle() {
    }

    public record SwimToggle(Entity entity, boolean swimming) implements FabricEntityEventHandle {
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

    public static final class Mending implements FabricEntityEventHandle, FabricItemEventHandle {

        private final LivingEntity entity;
        private final ItemStack item;
        private int repairAmount;
        private final @Nullable ExperienceOrb experienceOrb;

        public Mending(LivingEntity entity, ItemStack item, int repairAmount, @Nullable ExperienceOrb experienceOrb) {
            this.entity = entity;
            this.item = item;
            this.repairAmount = repairAmount;
            this.experienceOrb = experienceOrb;
        }

        @Override
        public LivingEntity entity() {
            return entity;
        }

        @Override
        public ItemStack itemStack() {
            return item;
        }

        public ItemStack item() {
            return item;
        }

        public int repairAmount() {
            return repairAmount;
        }

        public void setRepairAmount(int repairAmount) {
            this.repairAmount = repairAmount;
        }

        public @Nullable ExperienceOrb experienceOrb() {
            return experienceOrb;
        }
    }

    public record Chat(
            String message,
            @Nullable List<ServerPlayer> recipients
    ) {
    }

    public record ChunkLoad(net.minecraft.world.level.chunk.LevelChunk chunk) {
    }

    public record ChunkUnload(net.minecraft.world.level.chunk.LevelChunk chunk) {
    }

    public record VehicleCreate(Entity vehicle) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return vehicle;
        }
    }

    public record VehicleDamage(Entity vehicle, @Nullable Entity attacker) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return vehicle;
        }
    }

    public record VehicleDestroy(Entity vehicle, @Nullable Entity attacker) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return vehicle;
        }
    }

    public record VehicleEnter(Entity vehicle, Entity passenger) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return passenger;
        }
    }

    public record VehicleExit(Entity vehicle, Entity passenger) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return passenger;
        }
    }

    public record EntityMount(Entity entity, Entity vehicle) implements FabricEntityEventHandle {
    }

    public record EntityDismount(Entity entity, Entity vehicle) implements FabricEntityEventHandle {
    }

    public record ResurrectAttempt(LivingEntity entity) implements FabricEntityEventHandle {
    }

    public record PlayerWorldChange(ServerPlayer player) {
    }

    public record SheepRegrowWool(Entity sheep) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return sheep;
        }
    }

    public record SlimeSplit(Entity slime) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return slime;
        }
    }

    public record BellRing(ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }

    public record BellResonate(ServerLevel level, BlockPos position) implements FabricBlockEventHandle {
    }

    public record BatToggleSleep(Entity bat, boolean resting) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return bat;
        }
    }

    public record ToolChange(ServerPlayer player, int previousSlot, int newSlot) {
    }

    public record LanguageChange(ServerPlayer player, String language) {
    }

    public record Tame(TamableAnimal entity, net.minecraft.world.entity.player.Player player) implements FabricEntityEventHandle {
    }

    public record Combust(Entity entity, int duration) implements FabricEntityEventHandle {
    }

    public record ProjectileHit(Projectile projectile, @Nullable Entity hitEntity) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return projectile;
        }
    }

    public record ProjectileLaunch(Projectile projectile) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return projectile;
        }
    }

    public record BedEnter(ServerPlayer player) {
    }

    public record BedLeave(ServerPlayer player) {
    }

    public record LightningStrike(LightningBolt lightning) implements FabricEntityEventHandle {
        @Override
        public Entity entity() {
            return lightning;
        }
    }

    public record FoodLevelChange(ServerPlayer player, int oldLevel, int newLevel) {
    }

    public record SignChange(ServerPlayer player, BlockPos pos, String[] lines, boolean front) implements FabricBlockEventHandle {
        @Override
        public ServerLevel level() {
            return (ServerLevel) player.level();
        }

        @Override
        public BlockPos position() {
            return pos;
        }
    }

    public record BlockDamage(ServerPlayer player, BlockPos pos) implements FabricBlockEventHandle {
        @Override
        public ServerLevel level() {
            return (ServerLevel) player.level();
        }

        @Override
        public BlockPos position() {
            return pos;
        }
    }

    public record BucketUse(ServerPlayer player, boolean fill) {
    }

    public record InventoryOpen(ServerPlayer player) {
    }

    public record InventoryClose(ServerPlayer player) {
    }

    public record InventoryDrag(ServerPlayer player) {
    }

    public record LeavesDecay(ServerLevel level, BlockPos pos) implements FabricBlockEventHandle {
        @Override
        public BlockPos position() {
            return pos;
        }
    }

    public record SpongeAbsorb(ServerLevel level, BlockPos pos) implements FabricBlockEventHandle {
        @Override
        public BlockPos position() {
            return pos;
        }
    }

    public record SpawnChange(ServerLevel level, BlockPos pos) implements FabricBlockEventHandle {
        @Override
        public BlockPos position() {
            return pos;
        }
    }
}
