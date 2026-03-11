package kim.biryeong.skriptFabric.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.TextDisplay.class)
public interface TextDisplayAccessor {

    @Invoker("getFlags")
    byte skript$invokeGetFlags();

    @Invoker("setFlags")
    void skript$invokeSetFlags(byte flags);

    @Invoker("getLineWidth")
    int skript$invokeGetLineWidth();

    @Invoker("setLineWidth")
    void skript$invokeSetLineWidth(int lineWidth);

    @Invoker("getTextOpacity")
    byte skript$invokeGetTextOpacity();

    @Invoker("setTextOpacity")
    void skript$invokeSetTextOpacity(byte opacity);

    @Invoker("getText")
    Component skript$invokeGetText();

    @Invoker("setText")
    void skript$invokeSetText(Component text);
}
