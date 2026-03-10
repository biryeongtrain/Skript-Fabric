package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Play Sound")
@Description("Plays a sound at given locations for everyone nearby or directly to the given players.")
@Example("play sound \"block.note_block.pling\" to the player")
@Example("play sound \"entity.experience_orb.pickup\" at event-location")
@Since("2.2-dev28")
public class EffPlaySound extends Effect {

    private static boolean registered;

    private Expression<String> sounds;
    private @Nullable Expression<String> category;
    private @Nullable Expression<Number> volume;
    private @Nullable Expression<Number> pitch;
    private @Nullable Expression<Number> seed;
    private @Nullable Expression<ServerPlayer> players;
    private @Nullable Expression<FabricLocation> locations;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffPlaySound.class,
                "play sound[s] %strings% [[with] seed %-number%] [(in|from) %-string%] [(at|with) volume %-number%] [(and|at|with) pitch %-number%] [(to|for) %-players%] [(at|from) %-locations%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        sounds = (Expression<String>) expressions[0];
        seed = (Expression<Number>) expressions[1];
        category = (Expression<String>) expressions[2];
        volume = (Expression<Number>) expressions[3];
        pitch = (Expression<Number>) expressions[4];
        players = (Expression<ServerPlayer>) expressions[5];
        locations = (Expression<FabricLocation>) expressions[6];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        SoundSource source = EffectRuntimeSupport.soundSource(category == null ? null : category.getSingle(event));
        float resolvedVolume = volume == null || volume.getSingle(event) == null ? 1.0F : volume.getSingle(event).floatValue();
        float resolvedPitch = pitch == null || pitch.getSingle(event) == null ? 1.0F : pitch.getSingle(event).floatValue();
        long resolvedSeed = seed == null || seed.getSingle(event) == null
                ? ThreadLocalRandom.current().nextLong()
                : seed.getSingle(event).longValue();

        List<ResourceLocation> soundIds = new ArrayList<>();
        for (String sound : sounds.getArray(event)) {
            ResourceLocation id = EffectRuntimeSupport.parseResourceLocation(sound);
            if (id != null) {
                soundIds.add(id);
            }
        }
        if (soundIds.isEmpty()) {
            return;
        }

        ServerPlayer[] recipients = EffectRuntimeSupport.playersOrEvent(players == null ? null : players.getArray(event), event);
        FabricLocation[] emitters = locations == null ? new FabricLocation[0] : locations.getArray(event);

        if (emitters.length == 0 && recipients.length > 0) {
            emitters = new FabricLocation[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                emitters[i] = EffectRuntimeSupport.locationOf(recipients[i]);
            }
        }

        if (recipients.length == 0) {
            for (FabricLocation emitter : emitters) {
                if (emitter.level() == null) {
                    continue;
                }
                for (ServerPlayer player : EffectRuntimeSupport.worldPlayers(emitter.level())) {
                    sendSounds(player, emitter, soundIds, source, resolvedVolume, resolvedPitch, resolvedSeed);
                }
            }
            return;
        }

        for (ServerPlayer player : recipients) {
            if (emitters.length == 0) {
                sendSounds(player, EffectRuntimeSupport.locationOf(player), soundIds, source, resolvedVolume, resolvedPitch, resolvedSeed);
                continue;
            }
            for (FabricLocation emitter : emitters) {
                sendSounds(player, emitter, soundIds, source, resolvedVolume, resolvedPitch, resolvedSeed);
            }
        }
    }

    private void sendSounds(
            ServerPlayer player,
            FabricLocation emitter,
            List<ResourceLocation> soundIds,
            SoundSource source,
            float volume,
            float pitch,
            long seed
    ) {
        for (ResourceLocation soundId : soundIds) {
            player.connection.send(new ClientboundSoundPacket(
                    EffectRuntimeSupport.soundOf(soundId),
                    source,
                    emitter.position().x,
                    emitter.position().y,
                    emitter.position().z,
                    volume,
                    pitch,
                    seed
            ));
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        StringBuilder builder = new StringBuilder("play sound ").append(sounds.toString(event, debug));
        if (seed != null) {
            builder.append(" with seed ").append(seed.toString(event, debug));
        }
        if (category != null) {
            builder.append(" in ").append(category.toString(event, debug));
        }
        if (volume != null) {
            builder.append(" with volume ").append(volume.toString(event, debug));
        }
        if (pitch != null) {
            builder.append(" with pitch ").append(pitch.toString(event, debug));
        }
        if (players != null) {
            builder.append(" to ").append(players.toString(event, debug));
        }
        if (locations != null) {
            builder.append(" at ").append(locations.toString(event, debug));
        }
        return builder.toString();
    }
}
