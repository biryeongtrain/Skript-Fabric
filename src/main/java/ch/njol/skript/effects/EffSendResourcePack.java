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
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Send Resource Pack")
@Description({
        "Request that the player's client download and switch resource packs.",
        "The URL must be a direct download link."
})
@Example("send the resource pack from \"https://example.com/pack.zip\" with hash \"...\" to the player")
@Since("2.4")
public class EffSendResourcePack extends Effect {

    private static boolean registered;

    private Expression<String> url;
    private @Nullable Expression<String> hash;
    private Expression<ServerPlayer> recipients;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffSendResourcePack.class,
                "send [the] resource pack [from [[the] URL]] %string% to %players%",
                "send [the] resource pack [from [[the] URL]] %string% with hash %string% to %players%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        url = (Expression<String>) expressions[0];
        if (matchedPattern == 0) {
            recipients = (Expression<ServerPlayer>) expressions[1];
        } else {
            hash = (Expression<String>) expressions[1];
            recipients = (Expression<ServerPlayer>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        String address = url.getSingle(event);
        if (address == null) {
            return;
        }
        String resolvedHash = hash == null ? "" : EffectRuntimeSupport.stringOf(hash.getSingle(event));
        for (ServerPlayer player : recipients.getArray(event)) {
            player.connection.send(new ClientboundResourcePackPushPacket(
                    UUID.randomUUID(),
                    address,
                    resolvedHash,
                    false,
                    Optional.empty()
            ));
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return "send the resource pack from " + url.toString(event, debug)
                + (hash == null ? "" : " with hash " + hash.toString(event, debug))
                + " to " + recipients.toString(event, debug);
    }
}
