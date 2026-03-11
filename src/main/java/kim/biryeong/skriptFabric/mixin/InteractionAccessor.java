package kim.biryeong.skriptFabric.mixin;

import net.minecraft.world.entity.Interaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Interaction.class)
public interface InteractionAccessor {

    @Invoker("getWidth")
    float skript$invokeGetWidth();

    @Invoker("setWidth")
    void skript$invokeSetWidth(float width);

    @Invoker("getHeight")
    float skript$invokeGetHeight();

    @Invoker("setHeight")
    void skript$invokeSetHeight(float height);

    @Invoker("getResponse")
    boolean skript$invokeGetResponse();

    @Invoker("setResponse")
    void skript$invokeSetResponse(boolean response);
}
