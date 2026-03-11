package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BrewingStandBlockEntity.class)
public interface BrewingStandBlockEntityAccessor {

    @Accessor("fuel")
    int skript$getFuel();

    @Accessor("fuel")
    void skript$setFuel(int value);

    @Accessor("brewTime")
    int skript$getBrewTime();

    @Accessor("brewTime")
    void skript$setBrewTime(int value);
}
