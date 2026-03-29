package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.variables.Variables;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;

public final class SkriptFabricExpressionProjectileForceGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void projectileForceExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/projectile-force/projectile_force_marks_block.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Skeleton skeleton = (Skeleton) helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.SKELETON, 0.5F, 1.0F, 0.5F);
            skeleton.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));

            Cow target = (Cow) helper.spawnWithNoFreeWill(net.minecraft.world.entity.EntityType.COW, 6.5F, 1.0F, 0.5F);
            target.setCustomName(null);

            skeleton.performRangedAttack(target, 1.0F);

            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected projectile force expression to execute through the real bow shot path.")
            );
            assertNumberAtLeast(helper, "projectileforce::projectile_force", 0.01D);
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    private static void assertNumberAtLeast(GameTestHelper helper, String name, double minimum) {
        Object value = Variables.getVariable(name, null, false);
        helper.assertTrue(
                value instanceof Number && ((Number) value).doubleValue() >= minimum,
                Component.literal("Expected " + name + " to be >= " + minimum + " but got " + value + ".")
        );
    }
}
