package org.skriptlang.skript.bukkit.potion.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.MinecraftRegistryLookup;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;

public final class PotionEffectSupport {

    private static final Pattern AMPLIFIER_SUFFIX = Pattern.compile("^(.*?)(?:\\s+(\\d+))?$");

    private PotionEffectSupport() {
    }

    public static @Nullable LivingEntity asLivingEntity(@Nullable Object value) {
        return value instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    public static @Nullable Holder<MobEffect> parsePotionType(@Nullable Object value) {
        if (value instanceof Holder<?> holder && holder.value() instanceof MobEffect) {
            @SuppressWarnings("unchecked") Holder<MobEffect> mobEffectHolder = (Holder<MobEffect>) holder;
            return mobEffectHolder;
        }
        if (value instanceof Identifier id) {
            return holderFromId(id);
        }
        if (value instanceof MobEffect mobEffect) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect);
        }
        if (value instanceof SkriptPotionEffect potionEffect) {
            return potionEffect.type();
        }
        if (!(value instanceof String raw)) {
            String fallback = fallbackPotionToken(value);
            return fallback == null ? null : parsePotionType(fallback);
        }
        String normalized = normalizeEffectName(raw);
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = stripPotionSuffix(normalized);
        Matcher matcher = AMPLIFIER_SUFFIX.matcher(normalized);
        if (!matcher.matches()) {
            return null;
        }
        MobEffect mobEffect = MinecraftRegistryLookup.lookup(matcher.group(1), PotionEffectSupport::effectFromId);
        return mobEffect == null ? null : BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect);
    }

    public static @Nullable SkriptPotionEffect parsePotionEffect(@Nullable Object value) {
        if (value instanceof SkriptPotionEffect potionEffect) {
            return potionEffect;
        }
        if (value instanceof MobEffectInstance instance) {
            return SkriptPotionEffect.fromInstance(instance);
        }
        if (!(value instanceof String raw)) {
            Holder<MobEffect> type = parsePotionType(value);
            return type != null ? SkriptPotionEffect.fromType(type) : null;
        }

        String normalized = raw.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.endsWith(" active")) {
            normalized = normalized.substring(0, normalized.length() - 7).trim();
        }

        Boolean ambient = null;
        Boolean particles = null;
        Boolean icon = null;

        if (normalized.startsWith("ambient ")) {
            ambient = Boolean.TRUE;
            normalized = normalized.substring("ambient ".length()).trim();
        }
        if (normalized.contains(" without particles")) {
            particles = Boolean.FALSE;
            normalized = normalized.replace(" without particles", "").trim();
        } else if (normalized.contains(" with particles")) {
            particles = Boolean.TRUE;
            normalized = normalized.replace(" with particles", "").trim();
        }
        if (normalized.contains(" without icon")) {
            icon = Boolean.FALSE;
            normalized = normalized.replace(" without icon", "").trim();
        } else if (normalized.contains(" with icon")) {
            icon = Boolean.TRUE;
            normalized = normalized.replace(" with icon", "").trim();
        }

        Matcher matcher = AMPLIFIER_SUFFIX.matcher(normalized);
        if (!matcher.matches()) {
            return null;
        }

        Holder<MobEffect> type = parsePotionType(matcher.group(1));
        if (type == null) {
            return null;
        }

        Integer amplifier = null;
        if (matcher.group(2) != null) {
            amplifier = Integer.parseInt(matcher.group(2));
        }
        return new SkriptPotionEffect(type, amplifier, ambient, particles, icon);
    }

    public static String effectId(Holder<MobEffect> effect) {
        Identifier key = BuiltInRegistries.MOB_EFFECT.getKey(effect.value());
        return key != null ? MinecraftResourceParser.display(key) : effect.toString();
    }

    public static boolean sameType(@Nullable Holder<MobEffect> left, @Nullable Holder<MobEffect> right) {
        if (left == null || right == null) {
            return false;
        }
        if (left == right || left.value() == right.value()) {
            return true;
        }
        Identifier leftId = BuiltInRegistries.MOB_EFFECT.getKey(left.value());
        Identifier rightId = BuiltInRegistries.MOB_EFFECT.getKey(right.value());
        return leftId != null && leftId.equals(rightId);
    }

    private static String normalizeEffectName(String raw) {
        String normalized = raw.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.startsWith("a ")) {
            normalized = normalized.substring(2).trim();
        } else if (normalized.startsWith("an ")) {
            normalized = normalized.substring(3).trim();
        }
        normalized = normalized.replace("potion effect of ", "");
        normalized = normalized.replace("effect of ", "");
        return normalized.trim();
    }

    private static String stripPotionSuffix(String raw) {
        String normalized = raw.trim();
        if (normalized.endsWith(" potion")) {
            normalized = normalized.substring(0, normalized.length() - 7).trim();
        }
        return normalized;
    }

    private static @Nullable String fallbackPotionToken(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof GameProfile profile) {
            return profile.name();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        String rendered = value.toString();
        return rendered == null || rendered.isBlank() ? null : rendered;
    }

    private static @Nullable Holder<MobEffect> holderFromId(Identifier id) {
        MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.getValue(id);
        return mobEffect != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(mobEffect) : null;
    }

    private static @Nullable MobEffect effectFromId(Identifier id) {
        MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.getValue(id);
        Identifier key = mobEffect == null ? null : BuiltInRegistries.MOB_EFFECT.getKey(mobEffect);
        return id.equals(key) ? mobEffect : null;
    }
}
