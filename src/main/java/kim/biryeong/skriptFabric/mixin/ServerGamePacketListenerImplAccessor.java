package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {

    @Accessor("awaitingTeleport")
    int skript$getAwaitingTeleport();

    @Accessor("awaitingPositionFromClient")
    @Nullable Vec3 skript$getAwaitingPositionFromClient();
}
