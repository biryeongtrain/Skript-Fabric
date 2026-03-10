package ch.njol.skript.events;

import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class FabricEventCompatHandles {

    private FabricEventCompatHandles() {
    }

    public record GameMode(@Nullable GameType mode) {
    }

    public record ResourcePackResponse(@Nullable String status) {
    }

    public record Healing(Entity entity, @Nullable String reason) {
    }

    public record Portal(Entity entity, boolean player) {
    }

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

    public record EntityTransform(Entity entity, @Nullable String reason) {
    }

    public record EntityTarget(@Nullable Entity target) {
    }

    public record EntityShootBow(LivingEntity entity) {
    }

    public record ExperienceSpawn(int amount) {
    }

    public record Firework(@Nullable Set<Integer> colors) {
    }

    public enum LeashAction {
        LEASH,
        UNLEASH,
        PLAYER_UNLEASH
    }

    public record Leash(Entity entity, LeashAction action) {
    }

    public record HarvestBlock(BlockState blockState) {
    }

    public enum ArmorSlot {
        HEAD("helmet"),
        CHEST("chestplate"),
        LEGS("leggings"),
        FEET("boots");

        private final String text;

        ArmorSlot(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public record PlayerArmorChange(@Nullable ArmorSlot slot) {
    }

    public enum ClickType {
        RIGHT,
        LEFT
    }

    public record Click(
            ClickType clickType,
            @Nullable Entity entity,
            @Nullable BlockState blockState,
            @Nullable ItemStack tool
    ) {
    }

    public record BookEdit(boolean signing) {
    }
}
