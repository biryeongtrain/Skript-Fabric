package kim.biryeong.skriptFabricPort.gametest;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.EntityHitResult;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricSprintingGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void sprintingConditionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            ServerPlayer player = triggerUseEntityScript(helper, "skript/gametest/condition/sprinting_player_names_player.sk", cow, true);
            helper.assertTrue(
                    player.getCustomName() != null && "sprinting player".equals(player.getCustomName().getString()),
                    Component.literal("Expected sprinting condition script to name the sprinting event-player.")
            );
        });
    }

    @GameTest
    public void startSprintingEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            ServerPlayer player = triggerUseEntityScript(helper, "skript/gametest/effect/make_player_start_sprinting_names_player.sk", cow, false);
            helper.assertTrue(
                    player.getCustomName() != null && "made sprinting".equals(player.getCustomName().getString()),
                    Component.literal("Expected sprinting start effect script to name the event-player.")
            );
            helper.assertTrue(
                    player.isSprinting(),
                    Component.literal("Expected sprinting start effect to set the Mojang sprinting flag.")
            );
        });
    }

    @GameTest
    public void stopSprintingEffectExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Cow cow = createCow(helper, false);
            ServerPlayer player = triggerUseEntityScript(helper, "skript/gametest/effect/make_player_stop_sprinting_names_player.sk", cow, true);
            helper.assertTrue(
                    player.getCustomName() != null && "stopped sprinting".equals(player.getCustomName().getString()),
                    Component.literal("Expected sprinting stop effect script to name the event-player.")
            );
            helper.assertTrue(
                    !player.isSprinting(),
                    Component.literal("Expected sprinting stop effect to clear the Mojang sprinting flag.")
            );
        });
    }

    private ServerPlayer triggerUseEntityScript(GameTestHelper helper, String scriptPath, Cow cow, boolean sprintingBeforeEvent) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromResource(scriptPath);

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);
        player.teleportTo(cow.getX(), cow.getY() + 1.0D, cow.getZ());
        player.setSprinting(sprintingBeforeEvent);

        InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                player,
                helper.getLevel(),
                InteractionHand.MAIN_HAND,
                cow,
                new EntityHitResult(cow)
        );
        helper.assertTrue(
                result == InteractionResult.PASS,
                Component.literal("Expected sprinting script to keep Fabric callback flow in PASS state.")
        );
        runtime.clearScripts();
        return player;
    }
}
