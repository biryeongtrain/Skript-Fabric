package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BellBlockEntity.class)
public interface BellBlockEntityAccessor {

    @Accessor("ticks")
    int skript$getTicks();

    @Accessor("ticks")
    void skript$setTicks(int ticks);

    @Accessor("resonating")
    boolean skript$isResonating();

    @Accessor("resonating")
    void skript$setResonating(boolean resonating);

    @Accessor("resonationTicks")
    int skript$getResonationTicks();

    @Accessor("resonationTicks")
    void skript$setResonationTicks(int resonationTicks);

    @Accessor("nearbyEntities")
    List<LivingEntity> skript$getNearbyEntities();

    @Accessor("nearbyEntities")
    void skript$setNearbyEntities(List<LivingEntity> nearbyEntities);
}
