package kim.biryeong.skriptFabric.mixin;

import com.mojang.math.Transformation;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Brightness;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.class)
public interface DisplayAccessor {

    @Invoker("getBillboardConstraints")
    Display.BillboardConstraints skript$invokeGetBillboardConstraints();

    @Invoker("setBillboardConstraints")
    void skript$invokeSetBillboardConstraints(Display.BillboardConstraints constraints);

    @Invoker("getBrightnessOverride")
    Brightness skript$invokeGetBrightnessOverride();

    @Invoker("setBrightnessOverride")
    void skript$invokeSetBrightnessOverride(Brightness brightness);

    @Invoker("getViewRange")
    float skript$invokeGetViewRange();

    @Invoker("setViewRange")
    void skript$invokeSetViewRange(float viewRange);

    @Invoker("getTransformationInterpolationDuration")
    int skript$invokeGetTransformationInterpolationDuration();

    @Invoker("setTransformationInterpolationDuration")
    void skript$invokeSetTransformationInterpolationDuration(int ticks);

    @Invoker("getTransformationInterpolationDelay")
    int skript$invokeGetTransformationInterpolationDelay();

    @Invoker("setTransformationInterpolationDelay")
    void skript$invokeSetTransformationInterpolationDelay(int ticks);

    @Invoker("getPosRotInterpolationDuration")
    int skript$invokeGetPosRotInterpolationDuration();

    @Invoker("setPosRotInterpolationDuration")
    void skript$invokeSetPosRotInterpolationDuration(int ticks);

    @Invoker("getShadowRadius")
    float skript$invokeGetShadowRadius();

    @Invoker("setShadowRadius")
    void skript$invokeSetShadowRadius(float radius);

    @Invoker("getShadowStrength")
    float skript$invokeGetShadowStrength();

    @Invoker("setShadowStrength")
    void skript$invokeSetShadowStrength(float strength);

    @Invoker("getWidth")
    float skript$invokeGetWidth();

    @Invoker("setWidth")
    void skript$invokeSetWidth(float width);

    @Invoker("getHeight")
    float skript$invokeGetHeight();

    @Invoker("setHeight")
    void skript$invokeSetHeight(float height);

    @Invoker("getGlowColorOverride")
    int skript$invokeGetGlowColorOverride();

    @Invoker("setGlowColorOverride")
    void skript$invokeSetGlowColorOverride(int color);

    @Invoker("setTransformation")
    void skript$invokeSetTransformation(Transformation transformation);

    @Invoker("createTransformation")
    static Transformation skript$invokeCreateTransformation(SynchedEntityData data) {
        throw new AssertionError();
    }
}
