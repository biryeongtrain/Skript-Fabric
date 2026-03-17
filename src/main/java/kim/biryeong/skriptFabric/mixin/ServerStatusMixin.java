package kim.biryeong.skriptFabric.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.network.Connection;
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
            for (GameProfile profile : players.sample()) {
                currentSample.add(profile.getName());
            }
        }

        int currentProtocol = SharedConstants.getProtocolVersion();
        FabricServerListPingHandle handle = SkriptFabricEventBridge.dispatchServerListPing(currentSample, currentProtocol);

        List<String> modifiedSample = handle.playerSample();
        List<GameProfile> newProfiles = new ArrayList<>();
        for (String name : modifiedSample) {
            newProfiles.add(new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()), name));
        }

        int online = 0;
        int max = 0;
        if (playersOptional.isPresent()) {
            ServerStatus.Players players = playersOptional.get();
            online = players.online();
            max = players.max();
        }

        ServerStatus.Players newPlayers = new ServerStatus.Players(max, online, newProfiles);

        // Use the handle's protocol version (may have been modified by scripts)
        Optional<ServerStatus.Version> version;
        if (handle.protocolVersion() != currentProtocol) {
            version = Optional.of(new ServerStatus.Version(
                    SharedConstants.getCurrentVersion().name(),
                    handle.protocolVersion()
            ));
        } else {
            version = this.status.version();
        }

        ServerStatus newStatus = new ServerStatus(
                this.status.description(),
                Optional.of(newPlayers),
                version,
                this.status.favicon(),
                this.status.enforcesSecureChat()
        );

        this.connection.send(new ClientboundStatusResponsePacket(newStatus));
        ci.cancel();
    }
}
