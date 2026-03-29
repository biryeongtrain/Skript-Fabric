package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.lang.Condition;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricAliveKillGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void aliveConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/alive_entity_names_entity.sk",
                cow,
                "alive entity"
        );
    }

    @GameTest
    public void killEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptSetsMarker(
                helper,
                "skript/gametest/effect/kill_entity_marks_block.sk",
                cow,
                new net.minecraft.core.BlockPos(9, 1, 0),
                net.minecraft.world.level.block.Blocks.RED_WOOL,
                () -> helper.assertTrue(
                        !cow.isAlive() || cow.isRemoved(),
                        Component.literal("Expected kill effect to leave the entity dead or removed.")
                )
        );
    }

    @GameTest
    public void aliveAndDeadConditionsParseAndEvaluate(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();

            Condition alive = parseConditionInEvent("event-entity is alive", FabricUseEntityHandle.class);
            helper.assertTrue(alive != null, Component.literal("Expected event-entity alive condition to parse."));
            if (alive == null) {
                throw new IllegalStateException("event-entity is alive did not parse");
            }

            helper.assertTrue(
                    alive.check(new SkriptEvent(
                            new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                            helper.getLevel().getServer(),
                            helper.getLevel(),
                            player
                    )),
                    Component.literal("Expected event-entity alive condition to evaluate true for a live cow.")
            );

            cow.remove(Entity.RemovalReason.KILLED);

            Condition dead = parseConditionInEvent("event-entity is dead", FabricUseEntityHandle.class);
            helper.assertTrue(dead != null, Component.literal("Expected event-entity dead condition to parse."));
            if (dead == null) {
                throw new IllegalStateException("event-entity is dead did not parse");
            }

            helper.assertTrue(
                    dead.check(new SkriptEvent(
                            new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, null),
                            helper.getLevel().getServer(),
                            helper.getLevel(),
                            player
                    )),
                    Component.literal("Expected event-entity dead condition to evaluate true for a removed cow.")
            );
        });
    }
}
