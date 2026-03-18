package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import kim.biryeong.skriptFabric.EntityVisibilityManager;
import kim.biryeong.skriptFabric.mixin.ChunkMapAccessor;
import kim.biryeong.skriptFabric.mixin.TrackedEntityAccessor;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Entity Visibility")
@Description({
        "Change visibility of the given entities for the given players.",
        "If no players are given, will hide the entities from all online players.",
        "",
        "Note: all previously hidden entities (including players) will be visible when a player leaves and rejoins."
})
@Example("""
        on spawn:
            if event-entity is a chicken:
                hide event-entity
        """)
@Example("reveal hidden players of players")
@Since("2.3, 2.10 (entities)")
@RequiredPlugins("Minecraft 1.19+ (entities)")
public final class EffEntityVisibility extends Effect {

    private static boolean registered;

    private boolean reveal;
    private Expression<Entity> hidden;
    private @Nullable Expression<ServerPlayer> viewers;

    public static synchronized void register() {
        if (registered) {
            return;
        }
        Skript.registerEffect(EffEntityVisibility.class,
                "hide %entities% [(from|for) %-players%]",
                "reveal %entities% [(to|for|from) %-players%]");
        registered = true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        reveal = matchedPattern == 1;
        hidden = (Expression<Entity>) exprs[0];
        viewers = exprs.length < 2 ? null : (Expression<ServerPlayer>) exprs[1];
        return true;
    }

    @Override
    protected void execute(SkriptEvent event) {
        EntityVisibilityManager manager = EntityVisibilityManager.instance();

        for (Entity entity : hidden.getArray(event)) {
            if (!(entity.level() instanceof ServerLevel level)) {
                continue;
            }

            if (viewers == null) {
                if (reveal) {
                    manager.revealGlobally(entity.getUUID());
                    forceUpdateTracking(level, entity, null);
                } else {
                    manager.hideGlobally(entity.getUUID());
                    sendRemovePacket(level, entity, null);
                }
            } else {
                for (ServerPlayer viewer : viewers.getArray(event)) {
                    if (reveal) {
                        manager.revealFor(entity.getUUID(), viewer.getUUID());
                        forceUpdateTracking(level, entity, viewer);
                    } else {
                        manager.hideFor(entity.getUUID(), viewer.getUUID());
                        sendRemovePacket(level, entity, viewer);
                    }
                }
            }
        }
    }

    private static void sendRemovePacket(ServerLevel level, Entity entity, @Nullable ServerPlayer specificViewer) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(entity.getId());
        if (specificViewer != null) {
            specificViewer.connection.send(packet);
        } else {
            for (ServerPlayer player : level.players()) {
                player.connection.send(packet);
            }
        }
    }

    private static void forceUpdateTracking(ServerLevel level, Entity entity, @Nullable ServerPlayer specificViewer) {
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        if (!(chunkMap instanceof ChunkMapAccessor accessor)) {
            return;
        }
        Object tracked = accessor.getEntityMap().get(entity.getId());
        if (!(tracked instanceof TrackedEntityAccessor trackedAccessor)) {
            return;
        }
        if (specificViewer != null) {
            trackedAccessor.callUpdatePlayer(specificViewer);
        } else {
            for (ServerPlayer player : level.players()) {
                trackedAccessor.callUpdatePlayer(player);
            }
        }
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return (reveal ? "reveal " : "hide ")
                + hidden.toString(event, debug)
                + (viewers == null ? "" : (reveal ? " to " : " from ") + viewers.toString(event, debug));
    }
}
