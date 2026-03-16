package kim.biryeong.skriptFabric.mixin;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.skriptlang.skript.fabric.runtime.FabricChatHandle;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ChatMessageMixin {

    @Shadow public ServerPlayer player;

    @Inject(
            method = "broadcastChatMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void skript$dispatchChat(PlayerChatMessage chatMessage, CallbackInfo ci) {
        Component messageContent = chatMessage.decoratedContent();
        Set<ServerPlayer> recipients = new LinkedHashSet<>(player.getServer().getPlayerList().getPlayers());

        FabricChatHandle handle = SkriptFabricEventBridge.dispatchChat(player, messageContent, recipients);

        if (handle.isCancelled()) {
            ci.cancel();
            return;
        }

        // Format and recipients may have been modified by the script.
        // The format is stored on the handle for use by expressions;
        // recipient filtering and formatted message dispatch are handled here
        // if the recipients set was modified.
        Set<ServerPlayer> modifiedRecipients = handle.recipients();
        if (!modifiedRecipients.equals(new LinkedHashSet<>(player.getServer().getPlayerList().getPlayers()))) {
            // Recipients were modified; send to the subset and cancel the default broadcast
            String format = handle.format();
            String formattedMessage = String.format(format, player.getDisplayName().getString(), messageContent.getString());
            Component formattedComponent = Component.literal(formattedMessage);
            for (ServerPlayer recipient : modifiedRecipients) {
                recipient.sendSystemMessage(formattedComponent);
            }
            ci.cancel();
        }
    }
}
