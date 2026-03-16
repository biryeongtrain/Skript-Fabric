package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.ExprAppliedEffect;
import ch.njol.skript.expressions.ExprCompassTarget;
import ch.njol.skript.expressions.ExprElement;
import ch.njol.skript.expressions.ExprLoopValue;
import ch.njol.skript.expressions.ExprMaxMinecartSpeed;
import ch.njol.skript.expressions.ExprMinecartDerailedFlyingVelocity;
import ch.njol.skript.expressions.ExprNearestEntity;
import ch.njol.skript.expressions.ExprPortal;
import ch.njol.skript.expressions.ExprProjectileForce;
import ch.njol.skript.expressions.ExprSignText;
import ch.njol.skript.expressions.ExprSkull;
import ch.njol.skript.expressions.ExprSpawnerType;
import ch.njol.skript.expressions.ExprTargetedBlock;
import ch.njol.skript.expressions.ExprXOf;
import ch.njol.skript.expressions.ExprCommandInfo;
import ch.njol.skript.expressions.ExprResult;
import ch.njol.skript.expressions.ExprScript;
import ch.njol.skript.expressions.ExprScriptsOld;
import ch.njol.skript.literals.LitConsole;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public final class SkriptFabricExpressionBindingBatchGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void cycleF6BindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprMaxMinecartSpeed.class);
        assertRegistered(helper, ExprMinecartDerailedFlyingVelocity.class);
        assertRegistered(helper, ExprCompassTarget.class);
        assertRegistered(helper, ExprPortal.class);
        assertRegistered(helper, LitConsole.class);
        helper.succeed();
    }

    @GameTest
    public void cycleF2BindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprCommandInfo.class);
        assertRegistered(helper, ExprResult.class);
        assertRegistered(helper, ExprScript.class);
        assertRegistered(helper, ExprScriptsOld.class);
        helper.succeed();
    }

    @GameTest
    public void cycleJBindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprAppliedEffect.class);
        assertRegistered(helper, ExprNearestEntity.class);
        assertRegistered(helper, ExprTargetedBlock.class);
        helper.succeed();
    }

    @GameTest
    public void cycleKBindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprElement.class);
        assertRegistered(helper, ExprLoopValue.class);
        assertRegistered(helper, ExprXOf.class);
        helper.succeed();
    }

    @GameTest
    public void cycleLBindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprProjectileForce.class);
        helper.succeed();
    }

    @GameTest
    public void cycleMBindingRegistered(GameTestHelper helper) {
        assertRegistered(helper, ExprSkull.class);
        assertRegistered(helper, ExprSignText.class);
        assertRegistered(helper, ExprSpawnerType.class);
        helper.succeed();
    }

    private void assertRegistered(GameTestHelper helper, Class<?> type) {
        boolean found = false;
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (info.type() == type) {
                found = true;
                break;
            }
        }
        helper.assertTrue(found, Component.literal(type.getSimpleName() + " was not registered by bootstrap"));
    }
}
