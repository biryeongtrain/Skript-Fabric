package kim.biryeong.skriptFabric.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {
    @Accessor
    ServerEntity getServerEntity();

    @Invoker
    void callUpdatePlayer(ServerPlayer player);

    @Invoker
    void callRemovePlayer(ServerPlayer player);
}
