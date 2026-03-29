package kim.biryeong.skriptFabric.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.server.players.NameAndId;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.server.network.ServerStatusPacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.FabricServerListPingHandle;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mixin(ServerStatusPacketListenerImpl.class)
abstract class ServerStatusMixin {

    @Shadow
    @Final
    private ServerStatus status;

    @Shadow
    @Final
    private Connection connection;

    @Inject(
            method = "handleStatusRequest",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$dispatchServerListPing(ServerboundStatusRequestPacket packet, CallbackInfo ci) {
        List<String> currentSample = new ArrayList<>();
        Optional<ServerStatus.Players> playersOptional = this.status.players();
        if (playersOptional.isPresent()) {
            ServerStatus.Players players = playersOptional.get();
            for (NameAndId entry : players.sample()) {
                currentSample.add(entry.name());
            }
        }

        int currentProtocol = SharedConstants.getProtocolVersion();
        FabricServerListPingHandle handle = SkriptFabricEventBridge.dispatchServerListPing(currentSample, currentProtocol);

        // Build description (MOTD)
        Component description;
        String motd = handle.motd();
        if (motd != null) {
            description = Component.literal(motd);
        } else {
            description = this.status.description();
        }

        // Build players section
        Optional<ServerStatus.Players> newPlayersOpt;
        if (handle.hidePlayerInfo()) {
            newPlayersOpt = Optional.empty();
        } else {
            List<String> modifiedSample = handle.playerSample();
            Set<UUID> hiddenPlayerIds = handle.hiddenPlayers();

            int online = 0;
            int max = 0;
            if (playersOptional.isPresent()) {
                ServerStatus.Players players = playersOptional.get();
                online = players.online();
                max = players.max();
            }

            List<NameAndId> newProfiles = new ArrayList<>();
            for (String name : modifiedSample) {
                UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
                if (!hiddenPlayerIds.contains(uuid)) {
                    newProfiles.add(new NameAndId(uuid, name));
                }
            }

            // Also hide by matching real player UUIDs — reduce online count
            if (!hiddenPlayerIds.isEmpty()) {
                online = Math.max(0, online - hiddenPlayerIds.size());
            }

            ServerStatus.Players newPlayers = new ServerStatus.Players(max, online, newProfiles);
            newPlayersOpt = Optional.of(newPlayers);
        }

        // Build version
        Optional<ServerStatus.Version> version;
        if (handle.protocolVersion() != currentProtocol) {
            version = Optional.of(new ServerStatus.Version(
                    SharedConstants.getCurrentVersion().name(),
                    handle.protocolVersion()
            ));
        } else {
            version = this.status.version();
        }

        // Build favicon
        Optional<ServerStatus.Favicon> favicon;
        byte[] faviconBytes = handle.faviconBytes();
        if (faviconBytes != null) {
            favicon = Optional.of(new ServerStatus.Favicon(faviconBytes));
        } else {
            favicon = this.status.favicon();
        }

        ServerStatus newStatus = new ServerStatus(
                description,
                newPlayersOpt,
                version,
                favicon,
                this.status.enforcesSecureChat()
        );

        this.connection.send(new ClientboundStatusResponsePacket(newStatus));
        ci.cancel();
    }
}
