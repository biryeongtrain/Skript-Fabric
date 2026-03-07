package org.skriptlang.skript.fabric.runtime;

import java.util.Locale;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public enum FabricPotionEffectCause {
    UNKNOWN("unknown"),
    ATTACK("attack", "entity attack", "attack affliction"),
    ARROW("arrow affliction", "arrow", "tipped arrow"),
    AREA_EFFECT_CLOUD("area effect cloud", "lingering cloud", "cloud affliction"),
    AXOLOTL("axolotl", "axolotl support"),
    BEACON("beacon", "beacon effect"),
    COMMAND("command", "effect command"),
    CONDUIT("conduit", "conduit power"),
    CONVERSION("conversion", "villager conversion", "zombie villager conversion"),
    DEATH("death"),
    DOLPHIN("dolphin", "dolphin grace", "dolphins grace"),
    EXPIRATION("expiration", "effect expiration", "effect expired"),
    FOOD("food", "food effect", "eating food", "eat food"),
    ILLUSION("illusion", "illusioner"),
    MILK("milk", "milk bucket", "drink milk"),
    PLUGIN("plugin"),
    POTION_SPLASH("potion splash", "splash potion"),
    POTION_DRINK("potion drink", "drink potion", "drinking potion"),
    SPIDER_SPAWN("spider spawn", "spider natural spawn"),
    TOTEM("totem", "totem of undying"),
    TURTLE_HELMET("turtle helmet"),
    VILLAGER_TRADE("villager trade"),
    WARDEN("warden"),
    WITHER_ROSE("wither rose");

    private final String skriptName;
    private final String[] aliases;

    FabricPotionEffectCause(String skriptName, String... aliases) {
        this.skriptName = skriptName;
        this.aliases = aliases;
    }

    public String skriptName() {
        return skriptName;
    }

    public static FabricPotionEffectCause resolve(@Nullable Entity source) {
        return resolve(source, null, null);
    }

    public static FabricPotionEffectCause resolve(
            @Nullable Entity source,
            @Nullable LivingEntity entity,
            @Nullable MobEffectInstance effect
    ) {
        FabricPotionEffectCause tracked = FabricPotionEffectCauseContext.current();
        if (tracked != null) {
            return tracked;
        }
        if (source instanceof AreaEffectCloud) {
            return AREA_EFFECT_CLOUD;
        }
        if (source instanceof AbstractArrow) {
            return ARROW;
        }
        if (source instanceof Dolphin) {
            return DOLPHIN;
        }
        if (source != null) {
            return ATTACK;
        }
        if (entity instanceof Illusioner && effect != null && effect.is(MobEffects.INVISIBILITY)) {
            return ILLUSION;
        }
        if (entity instanceof net.minecraft.world.entity.player.Player player
                && effect != null
                && effect.is(MobEffects.WATER_BREATHING)
                && player.getItemBySlot(EquipmentSlot.HEAD).is(Items.TURTLE_HELMET)) {
            return TURTLE_HELMET;
        }
        return UNKNOWN;
    }

    public static @Nullable FabricPotionEffectCause parse(@Nullable Object value) {
        if (value instanceof FabricPotionEffectCause cause) {
            return cause;
        }
        if (value == null) {
            return null;
        }
        String normalized = normalize(String.valueOf(value));
        for (FabricPotionEffectCause cause : values()) {
            if (cause.matches(normalized)) {
                return cause;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return skriptName;
    }

    private boolean matches(String normalized) {
        if (normalize(skriptName).equals(normalized)) {
            return true;
        }
        for (String alias : aliases) {
            if (normalize(alias).equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String raw) {
        return raw.trim()
                .toLowerCase(Locale.ENGLISH)
                .replace('_', ' ')
                .replace('-', ' ')
                .replaceAll("\\s+", " ");
    }
}
