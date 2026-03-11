package kim.biryeong.skriptFabric.mixin;

import com.mojang.brigadier.tree.CommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.skriptlang.skript.fabric.runtime.SkriptFabricEventBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
abstract class CommandsMixin {

    @Inject(method = "sendCommands", at = @At("HEAD"))
    private void skript$dispatchCommandSend(ServerPlayer player, CallbackInfo callbackInfo) {
        if (player == null) {
            return;
        }
        List<String> commands = ((Commands) (Object) this)
                .getDispatcher()
                .getRoot()
                .getChildren()
                .stream()
                .map(CommandNode<CommandSourceStack>::getName)
                .toList();
        SkriptFabricEventBridge.dispatchPlayerCommandSend(player, commands);
    }
}
