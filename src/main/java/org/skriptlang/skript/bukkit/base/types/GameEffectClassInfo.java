package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.GameEffect;

public final class GameEffectClassInfo {

    private GameEffectClassInfo() {
    }

    public static void register() {
        ClassInfo<GameEffect> info = new ClassInfo<>(GameEffect.class, "gameeffect");
        info.setParser(new Parser());
        Classes.registerClassInfo(info);
    }

    private static final class Parser implements ClassInfo.Parser<GameEffect> {

        @Override
        public boolean canParse(ParseContext context) {
            return context == ParseContext.DEFAULT || context == ParseContext.CONFIG;
        }

        @Override
        public @Nullable GameEffect parse(String input, ParseContext context) {
            if (input == null || input.isBlank()) {
                return null;
            }
            String trimmed = input.trim().toLowerCase(Locale.ENGLISH);

            // Try direct "potion break" / "bone meal" style effects
            GameEffect effect = resolveGameEffect(trimmed);
            if (effect != null) {
                return effect;
            }

            // Try registry lookup with normalized name
            String normalized = trimmed.replace(' ', '_');
            ResourceLocation id = ResourceLocation.tryParse("minecraft:" + normalized);
            if (id != null && BuiltInRegistries.PARTICLE_TYPE.containsKey(id)) {
                return new GameEffect(id);
            }

            return null;
        }
    }

    private static @Nullable GameEffect resolveGameEffect(String name) {
        return switch (name) {
            case "potion break", "potion break effect" ->
                    new GameEffect(ResourceLocation.withDefaultNamespace("effect"));
            case "bone meal", "bone meal effect" ->
                    new GameEffect(ResourceLocation.withDefaultNamespace("happy_villager"));
            case "ender signal", "eye of ender signal" ->
                    new GameEffect(ResourceLocation.withDefaultNamespace("portal"));
            case "mobspawner flames", "mob spawner flames" ->
                    new GameEffect(ResourceLocation.withDefaultNamespace("flame"));
            default -> null;
        };
    }
}
