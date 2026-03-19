package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodDataAccessor {
    @Accessor("exhaustionLevel")
    float skript$getExhaustionLevel();

    @Accessor("exhaustionLevel")
    void skript$setExhaustionLevel(float value);
}
