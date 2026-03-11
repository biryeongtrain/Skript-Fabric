package kim.biryeong.skriptFabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {

    @Accessor("levels")
    int skript$getLevels();

    @Accessor("levels")
    void skript$setLevels(int levels);

    @Accessor("primaryPower")
    @Nullable Holder<MobEffect> skript$getPrimaryPower();

    @Accessor("primaryPower")
    void skript$setPrimaryPower(@Nullable Holder<MobEffect> effect);

    @Accessor("secondaryPower")
    @Nullable Holder<MobEffect> skript$getSecondaryPower();

    @Accessor("secondaryPower")
    void skript$setSecondaryPower(@Nullable Holder<MobEffect> effect);
}
