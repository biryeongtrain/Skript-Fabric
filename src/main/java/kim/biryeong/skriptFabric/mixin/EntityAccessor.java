package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    static int getFLAG_FALL_FLYING() {
        throw new UnsupportedOperationException();
    }
    @Invoker
    boolean callIsInRain();
    @Invoker
    void callSetSharedFlag(int flag, boolean value);
    @Invoker("getFireImmuneTicks")
    int skript$invokeGetFireImmuneTicks();
}
