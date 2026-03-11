package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;
    @Unique
    private Vec3 skript$moveFromPosition;
    @Unique
    private float skript$moveFromYaw;
    @Unique
    private float skript$moveFromPitch;

    @Inject(
            method = "handlePlayerInput",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setLastClientInput(Lnet/minecraft/world/entity/player/Input;)V"
            )
    )
    private void skript$dispatchPlayerInput(ServerboundPlayerInputPacket packet, CallbackInfo callbackInfo) {
        Input previousInput = player.getLastClientInput();
        SkriptFabricEventBridge.dispatchPlayerInput(player, packet.input(), previousInput);
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void skript$captureMoveStart(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo) {
        skript$moveFromPosition = player.position();
        skript$moveFromYaw = player.getYRot();
        skript$moveFromPitch = player.getXRot();
    }

    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void skript$dispatchMoveAfterAcceptedStep(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo) {
        if (skript$moveFromPosition == null) {
            return;
        }
        Vec3 fromPosition = skript$moveFromPosition;
        float fromYaw = skript$moveFromYaw;
        float fromPitch = skript$moveFromPitch;
        Vec3 toPosition = new Vec3(
                packet.getX(fromPosition.x),
                packet.getY(fromPosition.y),
                packet.getZ(fromPosition.z)
        );
        float toYaw = packet.getYRot(fromYaw);
        float toPitch = packet.getXRot(fromPitch);
        if (fromPosition.equals(toPosition) && fromYaw == toYaw && fromPitch == toPitch) {
            return;
        }
        SkriptFabricEventBridge.dispatchPlayerMove(player, fromPosition, toPosition, fromYaw, fromPitch, toYaw, toPitch);
    }

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void skript$clearCapturedMove(ServerboundMovePlayerPacket packet, CallbackInfo callbackInfo) {
        skript$moveFromPosition = null;
    }
}
