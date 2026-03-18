package ch.njol.skript.effects;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricEntityEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricExplosionPrimeEventHandle;

final class FabricEffectEventHandles {

    private FabricEffectEventHandles() {
    }

    static final class PlayerRespawn {

        private @Nullable FabricLocation respawnLocation;
        private final boolean bedSpawn;
        private final boolean anchorSpawn;
        private final @Nullable String reason;

        PlayerRespawn(@Nullable FabricLocation respawnLocation, boolean bedSpawn, boolean anchorSpawn, @Nullable String reason) {
            this.respawnLocation = respawnLocation;
            this.bedSpawn = bedSpawn;
            this.anchorSpawn = anchorSpawn;
            this.reason = reason;
        }

        public @Nullable FabricLocation respawnLocation() {
            return respawnLocation;
        }

        public void setRespawnLocation(@Nullable FabricLocation respawnLocation) {
            this.respawnLocation = respawnLocation;
        }

        public boolean isBedSpawn() {
            return bedSpawn;
        }

        public boolean isAnchorSpawn() {
            return anchorSpawn;
        }

        public @Nullable String reason() {
            return reason;
        }
    }

    static final class EntityDeath implements FabricEntityEventHandle {

        private final LivingEntity entity;
        private final List<ItemStack> drops;
        private int droppedExp;
        private @Nullable net.minecraft.network.chat.Component deathMessage;

        EntityDeath(LivingEntity entity) {
            this(entity, new ArrayList<>(), 0);
        }

        EntityDeath(LivingEntity entity, List<ItemStack> drops, int droppedExp) {
            this.entity = entity;
            this.drops = drops;
            this.droppedExp = droppedExp;
            this.deathMessage = entity.getCombatTracker().getDeathMessage();
        }

        @Override
        public LivingEntity entity() {
            return entity;
        }

        public List<ItemStack> drops() {
            return drops;
        }

        public int droppedExp() {
            return droppedExp;
        }

        public void setDroppedExp(int droppedExp) {
            this.droppedExp = droppedExp;
        }

        public @Nullable net.minecraft.network.chat.Component deathMessage() {
            return deathMessage;
        }

        public void setDeathMessage(@Nullable net.minecraft.network.chat.Component deathMessage) {
            this.deathMessage = deathMessage;
        }
    }

    static final class ExplosionPrime implements FabricExplosionPrimeEventHandle {

        private float radius;
        private boolean causesFire;

        ExplosionPrime(float radius, boolean causesFire) {
            this.radius = radius;
            this.causesFire = causesFire;
        }

        public float radius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public boolean causesFire() {
            return causesFire;
        }

        public void setCausesFire(boolean causesFire) {
            this.causesFire = causesFire;
        }
    }

    static final class PlayerEggThrow {
    }

    static final class PlayerElytraBoost {

        private boolean shouldConsume;

        PlayerElytraBoost(boolean shouldConsume) {
            this.shouldConsume = shouldConsume;
        }

        boolean shouldConsume() {
            return shouldConsume;
        }

        void setShouldConsume(boolean shouldConsume) {
            this.shouldConsume = shouldConsume;
        }
    }

    static final class EntityUnleash {
    }

    static final class HangingBreak {

        private final Entity entity;
        private final @Nullable Entity remover;

        HangingBreak(Entity entity, @Nullable Entity remover) {
            this.entity = entity;
            this.remover = remover;
        }

        public Entity entity() {
            return entity;
        }

        public @Nullable Entity remover() {
            return remover;
        }
    }

    static final class HangingPlace {

        private final Entity entity;

        HangingPlace(Entity entity) {
            this.entity = entity;
        }

        public Entity entity() {
            return entity;
        }
    }
}
