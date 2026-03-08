package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.lang.Condition;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricInvisibleGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void invisibleConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setInvisible(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/invisible_entity_names_entity.sk",
                cow,
                "invisible entity"
        );
    }

    @GameTest
    public void visibleConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/visible_entity_names_entity.sk",
                cow,
                "visible entity"
        );
    }

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

    @GameTest
    public void invisibleConditionParsesAndEvaluates(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.setInvisible(true);

            Condition invisible = parseConditionInEvent("event-entity is invisible", FabricUseEntityHandle.class);
            helper.assertTrue(invisible != null, Component.literal("Expected event-entity invisible condition to parse."));
            if (invisible == null) {
                throw new IllegalStateException("event-entity is invisible did not parse");
            }

            Condition visible = parseConditionInEvent("event-entity is visible", FabricUseEntityHandle.class);
            helper.assertTrue(visible != null, Component.literal("Expected event-entity visible condition to parse."));
            if (visible == null) {
                throw new IllegalStateException("event-entity is visible did not parse");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            SkriptEvent event = new SkriptEvent(
                    new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );

            helper.assertTrue(
                    invisible.check(event),
                    Component.literal("Expected event-entity invisible condition to evaluate true for an invisible cow.")
            );
            helper.assertTrue(
                    !visible.check(event),
                    Component.literal("Expected event-entity visible condition to evaluate false for an invisible cow.")
            );
        });
    }
}
