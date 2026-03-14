package kim.biryeong.skriptFabric.mixin;

import ch.njol.skript.events.TeleportCause;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.LookAt;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import org.skriptlang.skript.fabric.runtime.TeleportCauseCapture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(TeleportCommand.class)
abstract class TeleportCommandMixin {

    @Inject(
            method = "performTeleport(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFLnet/minecraft/server/commands/LookAt;)V",
            at = @At("HEAD")
    )
    private static void skript$setCommandCause(CommandSourceStack source, Entity entity, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float yRot, float xRot, LookAt lookAt, CallbackInfo ci) {
        TeleportCauseCapture.set(TeleportCause.COMMAND);
    }

    @Inject(
            method = "performTeleport(Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFLnet/minecraft/server/commands/LookAt;)V",
            at = @At("TAIL")
    )
    private static void skript$clearCommandCause(CommandSourceStack source, Entity entity, ServerLevel level, double x, double y, double z, Set<Relative> relatives, float yRot, float xRot, LookAt lookAt, CallbackInfo ci) {
        TeleportCauseCapture.consume();
    }
}
