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

public final class SkriptFabricSilentGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void silentConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setSilent(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/silent_entity_names_entity.sk",
                cow,
                "silent entity"
        );
    }

    @GameTest
    public void makeSilentEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/make_silent_names_entity.sk",
                cow,
                "made silent",
                () -> helper.assertTrue(cow.isSilent(), Component.literal("Expected make-silent effect to set the Mojang silent flag."))
        );
    }

    @GameTest
    public void silentConditionParsesAndEvaluates(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.setSilent(true);

            Condition condition = parseConditionInEvent("event-entity is silent", FabricUseEntityHandle.class);
            helper.assertTrue(condition != null, Component.literal("Expected event-entity silent condition to parse."));
            if (condition == null) {
                throw new IllegalStateException("event-entity is silent did not parse");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            helper.assertTrue(
                    condition.check(new SkriptEvent(
                            new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                            helper.getLevel().getServer(),
                            helper.getLevel(),
                            player
                    )),
                    Component.literal("Expected event-entity silent condition to evaluate true for a silent cow.")
            );
        });
    }
}
