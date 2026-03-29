package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.lang.Condition;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricBurningGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void burningConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setRemainingFireTicks(40);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/burning_entity_names_entity.sk",
                cow,
                "burning entity"
        );
    }

    @GameTest
    public void ignitedConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setRemainingFireTicks(40);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/ignited_entity_names_entity.sk",
                cow,
                "ignited entity"
        );
    }

    @GameTest
    public void onFireConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setRemainingFireTicks(40);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/on_fire_entity_names_entity.sk",
                cow,
                "on fire entity"
        );
    }

    @GameTest
    public void burningAliasesParseAndEvaluate(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.setRemainingFireTicks(40);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();

            assertConditionEvaluatesTrue(helper, player, cow, "event-entity is burning", "burning");
            assertConditionEvaluatesTrue(helper, player, cow, "event-entity is ignited", "ignited");
            assertConditionEvaluatesTrue(helper, player, cow, "event-entity is on fire", "on fire");
        });
    }

    private void assertConditionEvaluatesTrue(
            GameTestHelper helper,
            ServerPlayer player,
            Cow cow,
            String text,
            String label
    ) {
        Condition condition = parseConditionInEvent(text, FabricUseEntityHandle.class);
        helper.assertTrue(condition != null, Component.literal("Expected event-entity " + label + " condition to parse."));
        if (condition == null) {
            throw new IllegalStateException(text + " did not parse");
        }

        helper.assertTrue(
                condition.check(new SkriptEvent(
                        new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                        helper.getLevel().getServer(),
                        helper.getLevel(),
                        player
                )),
                Component.literal("Expected event-entity " + label + " condition to evaluate true for a burning cow.")
        );
    }
}
