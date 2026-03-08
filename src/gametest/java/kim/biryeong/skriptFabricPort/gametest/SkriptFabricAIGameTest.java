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

public final class SkriptFabricAIGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void aiConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setNoAi(false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/has_ai_entity_names_entity.sk",
                cow,
                "has ai"
        );
    }

    @GameTest
    public void noAiConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setNoAi(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/no_ai_entity_names_entity.sk",
                cow,
                "no ai"
        );
    }

    @GameTest
    public void aiConditionParsesAndEvaluates(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.setNoAi(false);

            Condition hasAi = parseConditionInEvent("event-entity has artificial intelligence", FabricUseEntityHandle.class);
            helper.assertTrue(hasAi != null, Component.literal("Expected event-entity has artificial intelligence to parse."));
            if (hasAi == null) {
                throw new IllegalStateException("event-entity has artificial intelligence did not parse");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            SkriptEvent event = new SkriptEvent(
                    new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            );
            helper.assertTrue(
                    hasAi.check(event),
                    Component.literal("Expected event-entity has artificial intelligence to evaluate true for an AI-enabled cow.")
            );

            cow.setNoAi(true);
            Condition noAi = parseConditionInEvent("event-entity doesn't have ai", FabricUseEntityHandle.class);
            helper.assertTrue(noAi != null, Component.literal("Expected event-entity doesn't have ai to parse."));
            if (noAi == null) {
                throw new IllegalStateException("event-entity doesn't have ai did not parse");
            }
            helper.assertTrue(
                    noAi.check(event),
                    Component.literal("Expected event-entity doesn't have ai to evaluate true for a no-AI cow.")
            );
        });
    }
}
