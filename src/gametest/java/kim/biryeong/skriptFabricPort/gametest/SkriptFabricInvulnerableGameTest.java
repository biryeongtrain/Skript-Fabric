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

public final class SkriptFabricInvulnerableGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void invulnerableConditionExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        cow.setInvulnerable(true);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/condition/invulnerable_entity_names_entity.sk",
                cow,
                "invulnerable entity"
        );
    }

    @GameTest
    public void makeInvulnerableEffectExecutesRealScript(GameTestHelper helper) {
        Cow cow = createCow(helper, false);
        assertUseEntityScriptNamesEntity(
                helper,
                "skript/gametest/effect/make_invulnerable_names_entity.sk",
                cow,
                "made invulnerable",
                () -> helper.assertTrue(cow.isInvulnerable(), Component.literal("Expected make-invulnerable effect to set the Mojang invulnerable flag."))
        );
    }

    @GameTest
    public void invulnerableConditionParsesAndEvaluates(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            cow.setInvulnerable(true);

            Condition condition = parseConditionInEvent("event-entity is invincible", FabricUseEntityHandle.class);
            helper.assertTrue(condition != null, Component.literal("Expected event-entity invincible condition to parse."));
            if (condition == null) {
                throw new IllegalStateException("event-entity is invincible did not parse");
            }

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            helper.assertTrue(
                    condition.check(new SkriptEvent(
                            new FabricUseEntityHandle(helper.getLevel(), player, InteractionHand.MAIN_HAND, cow, new EntityHitResult(cow)),
                            helper.getLevel().getServer(),
                            helper.getLevel(),
                            player
                    )),
                    Component.literal("Expected event-entity invincible condition to evaluate true for an invulnerable cow.")
            );
        });
    }
}
