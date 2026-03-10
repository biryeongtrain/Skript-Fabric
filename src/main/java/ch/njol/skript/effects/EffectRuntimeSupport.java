package ch.njol.skript.effects;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.StringMode;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class EffectRuntimeSupport {

    private EffectRuntimeSupport() {
    }

    static ServerPlayer[] playersOrEvent(@Nullable ServerPlayer[] players, SkriptEvent event) {
        if (players != null && players.length > 0) {
            return players;
        }
        return event.player() == null ? new ServerPlayer[0] : new ServerPlayer[]{event.player()};
    }

    static FabricLocation locationOf(ServerPlayer player) {
        return new FabricLocation(player.level(), player.position());
    }

    static Component componentOf(@Nullable Object value, @Nullable SkriptEvent event) {
        if (value == null) {
            return Component.empty();
        }
        if (value instanceof String string) {
            return SkriptTextPlaceholders.resolveComponent(string, event);
        }
        return Component.literal(stringOf(value));
    }

    static String stringOf(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        return value instanceof String string ? string : Classes.toString(value, StringMode.MESSAGE);
    }

    static @Nullable ResourceLocation parseResourceLocation(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            return input.contains(":")
                    ? ResourceLocation.parse(input)
                    : ResourceLocation.withDefaultNamespace(input);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static Holder<SoundEvent> soundOf(ResourceLocation id) {
        return BuiltInRegistries.SOUND_EVENT.get(id)
                .<Holder<SoundEvent>>map(reference -> reference)
                .orElseGet(() -> Holder.direct(SoundEvent.createVariableRangeEvent(id)));
    }

    static SoundSource soundSource(@Nullable String input) {
        if (input == null || input.isBlank()) {
            return SoundSource.MASTER;
        }
        String normalized = input.trim().toLowerCase(Locale.ENGLISH);
        for (SoundSource value : SoundSource.values()) {
            if (value.getName().equalsIgnoreCase(normalized) || value.name().equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return SoundSource.MASTER;
    }

    static ServerPlayer[] worldPlayers(@Nullable ServerLevel level) {
        if (level == null) {
            return new ServerPlayer[0];
        }
        return level.players().toArray(ServerPlayer[]::new);
    }
}
