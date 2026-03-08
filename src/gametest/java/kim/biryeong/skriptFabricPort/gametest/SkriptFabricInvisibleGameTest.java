package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Cow;

public final class SkriptFabricInvisibleGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void makeInvisibleEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/make_invisible_names_entity.sk",
                cow,
                "made invisible",
                () -> helper.assertTrue(cow.isInvisible(), Component.literal("Expected make-invisible effect to set the Mojang invisible flag."))
        );
    }

    @GameTest
    public void makeVisibleEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setInvisible(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/make_visible_names_entity.sk",
                cow,
                "made visible",
                () -> helper.assertTrue(!cow.isInvisible(), Component.literal("Expected make-visible effect to clear the Mojang invisible flag."))
        );
    }
}
