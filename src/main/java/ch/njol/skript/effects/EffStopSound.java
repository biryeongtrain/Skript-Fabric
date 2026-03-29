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
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Stop Sound")
@Description("Stops specific or all sounds from playing to a group of players.")
@Example("stop sound \"block.chest.open\" for the player")
@Example("stop all sounds for all players")
@Since("2.4, 2.7 (stop all sounds)")
public class EffStopSound extends Effect {

    private static boolean registered;

    private @Nullable Expression<String> sounds;
    private @Nullable Expression<String> category;
    private @Nullable Expression<ServerPlayer> players;
    private boolean allSounds;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffStopSound.class,
                "stop (all:all sound[s]|sound[s] %-strings%) [(in [the]|from) %-string%] [(from playing to|for) %-players%]",
                "stop playing sound[s] %strings% [(in [the]|from) %-string%] [(to|for) %-players%]"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        allSounds = parseResult.hasTag("all");
        sounds = (Expression<String>) expressions[0];
        category = (Expression<String>) expressions[1];
        players = (Expression<ServerPlayer>) expressions[2];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        SoundSource source = category == null ? null : EffectRuntimeSupport.soundSource(category.getSingle(event));
        ServerPlayer[] targets = EffectRuntimeSupport.playersOrEvent(players == null ? null : players.getArray(event), event);
        if (allSounds) {
            ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(null, source);
            for (ServerPlayer player : targets) {
                player.connection.send(packet);
            }
            return;
        }
        if (sounds == null) {
            return;
        }
        for (String sound : sounds.getArray(event)) {
            Identifier id = EffectRuntimeSupport.parseResourceLocation(sound);
            if (id == null) {
                continue;
            }
            ClientboundStopSoundPacket packet = new ClientboundStopSoundPacket(id, source);
            for (ServerPlayer player : targets) {
                player.connection.send(packet);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (allSounds ? "stop all sounds" : "stop sound " + (sounds == null ? "" : sounds.toString(event, debug)))
                + (category == null ? "" : " in " + category.toString(event, debug))
                + (players == null ? "" : " for " + players.toString(event, debug));
    }
}
