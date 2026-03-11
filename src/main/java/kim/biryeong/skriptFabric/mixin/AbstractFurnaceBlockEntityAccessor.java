package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {

    @Accessor("litTimeRemaining")
    int skript$getLitTimeRemaining();

    @Accessor("litTimeRemaining")
    void skript$setLitTimeRemaining(int value);

    @Accessor("litTotalTime")
    int skript$getLitTotalTime();

    @Accessor("litTotalTime")
    void skript$setLitTotalTime(int value);

    @Accessor("cookingTimer")
    int skript$getCookingTimer();

    @Accessor("cookingTimer")
    void skript$setCookingTimer(int value);

    @Accessor("cookingTotalTime")
    int skript$getCookingTotalTime();

    @Accessor("cookingTotalTime")
    void skript$setCookingTotalTime(int value);
}
