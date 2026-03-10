package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Send Block Change")
@Description("Makes a player see a block as something else or as the original.")
@Example("make player see block at player as dirt")
@Example("""
        make all players see (blocks in radius 5 of location(0, 0, 0)) as bedrock
        make all players see (blocks in radius 5 of location(0, 0, 0)) as original
        """)
@Since("2.2-dev37c, 2.5.1 (block data support), 2.12 (as original)")
public final class EffSendBlockChange extends Effect {

    private static boolean registered;

    private Expression<ServerPlayer> players;
    private Expression<FabricLocation> locations;
    private @Nullable Expression<Object> type;
    private boolean asOriginal;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(
                EffSendBlockChange.class,
                "make %players% see %locations% as [the|its] (original|normal|actual) [block]",
                "make %players% see %locations% as %itemtype/objects%"
        );
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        players = (Expression<ServerPlayer>) exprs[0];
        locations = (Expression<FabricLocation>) exprs[1];
        asOriginal = matchedPattern == 0;
        if (!asOriginal) {
            type = (Expression<Object>) exprs[2];
        }
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        BlockState forcedState = null;
        if (!asOriginal) {
            assert type != null;
            Object value = type.getSingle(event);
            if (value instanceof FabricItemType itemType && itemType.item() instanceof BlockItem blockItem) {
                forcedState = blockItem.getBlock().defaultBlockState();
            } else if (value instanceof BlockState blockState) {
                forcedState = blockState;
            } else {
                return;
            }
        }
        for (FabricLocation location : locations.getArray(event)) {
            if (location.level() == null) {
                continue;
            }
            BlockPos pos = BlockPos.containing(location.position());
            BlockState state = asOriginal ? location.level().getBlockState(pos) : forcedState;
            ClientboundBlockUpdatePacket packet = new ClientboundBlockUpdatePacket(pos, state);
            for (ServerPlayer player : players.getArray(event)) {
                player.connection.send(packet);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("make", players, "see", locations, "as");
        if (asOriginal) {
            builder.append("original");
        } else if (type != null) {
            builder.append(type);
        }
        return builder.toString();
    }
}
