package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public final class ParticleClassInfo {

    private static final Pattern COUNT_PREFIX = Pattern.compile("^(\\d+)\\s+(.+)$");

    private ParticleClassInfo() {
    }

    public static void register() {
        ClassInfo<ParticleEffect> info = new ClassInfo<>(ParticleEffect.class, "particle");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<ParticleEffect> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable ParticleEffect parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String trimmed = input.trim().toLowerCase(Locale.ENGLISH);

            int count = 1;
            Matcher countMatcher = COUNT_PREFIX.matcher(trimmed);
            if (countMatcher.matches()) {
                try {
                    count = Integer.parseInt(countMatcher.group(1));
                } catch (NumberFormatException ex) {
                    return null;
                }
                trimmed = countMatcher.group(2).trim();
            }

            ParticleOptions options = resolveParticle(trimmed);
            if (options == null) {
                return null;
            }
            ParticleEffect effect = ParticleEffect.of(options);
            effect.count(count);
            return effect;
        }
    }

    private static @Nullable ParticleOptions resolveParticle(String name) {
        // Handle dust/color particles: "white dust", "red dust", "dust", etc.
        if (name.endsWith(" dust") || name.equals("dust")) {
            String colorName = name.endsWith(" dust") && !name.equals("dust")
                    ? name.substring(0, name.length() - 5).trim()
                    : "";
            ColorRGB color = parseColor(colorName);
            return new DustParticleOptions(color.rgb(), 1.0F);
        }

        // Try direct particle registry lookup
        String normalized = name.replace(' ', '_');
        Identifier id = Identifier.tryParse("minecraft:" + normalized);
        if (id != null) {
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(id);
            if (type != null && type instanceof ParticleOptions options) {
                return options;
            }
        }

        // Try common aliases
        return resolveAlias(name);
    }

    private static ColorRGB parseColor(String colorName) {
        if (colorName.isEmpty() || "white".equals(colorName)) {
            return new ColorRGB(255, 255, 255);
        }
        return switch (colorName) {
            case "red" -> new ColorRGB(255, 0, 0);
            case "green" -> new ColorRGB(0, 255, 0);
            case "blue" -> new ColorRGB(0, 0, 255);
            case "yellow" -> new ColorRGB(255, 255, 0);
            case "cyan" -> new ColorRGB(0, 255, 255);
            case "magenta", "pink" -> new ColorRGB(255, 0, 255);
            case "orange" -> new ColorRGB(255, 165, 0);
            case "purple" -> new ColorRGB(128, 0, 128);
            case "black" -> new ColorRGB(0, 0, 0);
            case "gray", "grey" -> new ColorRGB(128, 128, 128);
            case "light gray", "light grey" -> new ColorRGB(192, 192, 192);
            default -> new ColorRGB(255, 255, 255);
        };
    }

    @SuppressWarnings("deprecation")
    private static @Nullable ParticleOptions resolveAlias(String name) {
        return switch (name) {
            case "flame", "fire" -> ParticleTypes.FLAME;
            case "smoke" -> ParticleTypes.SMOKE;
            case "large smoke" -> ParticleTypes.LARGE_SMOKE;
            case "cloud" -> ParticleTypes.CLOUD;
            case "heart" -> ParticleTypes.HEART;
            case "villager happy", "happy villager" -> ParticleTypes.HAPPY_VILLAGER;
            case "angry villager" -> ParticleTypes.ANGRY_VILLAGER;
            case "crit", "critical hit" -> ParticleTypes.CRIT;
            case "enchanted hit", "magic crit" -> ParticleTypes.ENCHANTED_HIT;
            case "bubble" -> ParticleTypes.BUBBLE;
            case "splash" -> ParticleTypes.SPLASH;
            case "dripping water", "drip water" -> ParticleTypes.DRIPPING_WATER;
            case "dripping lava", "drip lava" -> ParticleTypes.DRIPPING_LAVA;
            case "lava" -> ParticleTypes.LAVA;
            case "note" -> ParticleTypes.NOTE;
            case "portal" -> ParticleTypes.PORTAL;
            case "enchant", "enchantment table" -> ParticleTypes.ENCHANT;
            case "explosion" -> ParticleTypes.EXPLOSION;
            case "snowflake" -> ParticleTypes.SNOWFLAKE;
            case "electric spark" -> ParticleTypes.ELECTRIC_SPARK;
            case "witch", "witch magic" -> ParticleTypes.WITCH;
            case "water wake", "fishing" -> ParticleTypes.FISHING;
            case "end rod" -> ParticleTypes.END_ROD;
            case "totem", "totem of undying" -> ParticleTypes.TOTEM_OF_UNDYING;
            case "campfire smoke", "campfire cosy smoke" -> ParticleTypes.CAMPFIRE_COSY_SMOKE;
            case "soul fire flame", "soul flame" -> ParticleTypes.SOUL_FIRE_FLAME;
            case "soul" -> ParticleTypes.SOUL;
            case "wax on" -> ParticleTypes.WAX_ON;
            case "wax off" -> ParticleTypes.WAX_OFF;
            case "damage indicator" -> ParticleTypes.DAMAGE_INDICATOR;
            default -> null;
        };
    }
}
