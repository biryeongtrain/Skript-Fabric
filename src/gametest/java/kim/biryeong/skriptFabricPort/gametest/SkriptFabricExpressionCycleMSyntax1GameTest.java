package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycleMSyntax1GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    private static volatile @Nullable GameProfile offlinePlayer;
    private static volatile @Nullable FabricBlock signBlock;
    private static volatile @Nullable FabricBlock spawnerBlock;

    @GameTest
    public void cycleMSyntax1ExpressionsExecuteRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            BlockPos signPos = helper.absolutePos(new BlockPos(1, 1, 0));
            helper.getLevel().setBlockAndUpdate(signPos, Blocks.OAK_SIGN.defaultBlockState());
            SignBlockEntity sign = helper.getLevel().getBlockEntity(signPos) instanceof SignBlockEntity foundSign ? foundSign : null;
            helper.assertTrue(sign != null, Component.literal("Expected sign block entity for cycle m expression test."));
            if (sign == null) {
                throw new IllegalStateException("Missing sign block entity");
            }
            sign.setText(sign.getFrontText().setMessage(1, Component.literal("before")), true);
            sign.setChanged();
            helper.getLevel().sendBlockUpdated(signPos, helper.getLevel().getBlockState(signPos), helper.getLevel().getBlockState(signPos), 3);

            BlockPos spawnerPos = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(spawnerPos, Blocks.SPAWNER.defaultBlockState());
            SpawnerBlockEntity spawner = helper.getLevel().getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity foundSpawner ? foundSpawner : null;
            helper.assertTrue(spawner != null, Component.literal("Expected spawner block entity for cycle m expression test."));
            if (spawner == null) {
                throw new IllegalStateException("Missing spawner block entity");
            }
            spawner.setEntityId(EntityType.PIG, helper.getLevel().getRandom());
            spawner.setChanged();
            helper.getLevel().sendBlockUpdated(spawnerPos, helper.getLevel().getBlockState(spawnerPos), helper.getLevel().getBlockState(spawnerPos), 3);

            offlinePlayer = new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "cyclem");
            signBlock = new FabricBlock(helper.getLevel(), signPos);
            spawnerBlock = new FabricBlock(helper.getLevel(), spawnerPos);

            runtime.loadFromResource("skript/gametest/expression/cycle-m/syntax1/skull_sign_spawner_record_values.sk");

            int executed = dispatch(runtime, helper, new CycleMSyntax1Handle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for cycle m syntax1 expressions but got " + executed + ".")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 1, 0))).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected cycle m syntax1 fixture to mark the verification block.")
            );

            Object skullValue = Variables.getVariable("cyclem::skull", null, false);
            helper.assertTrue(
                    skullValue instanceof FabricItemType itemType
                            && itemType.item() == Items.PLAYER_HEAD,
                    Component.literal("Expected cyclem::skull to hold a player head item but got " + skullValue + ".")
            );

            Object signValue = Variables.getVariable("cyclem::sign-line", null, false);
            helper.assertTrue(
                    "cycle-m".equals(signValue),
                    Component.literal("Expected cyclem::sign-line to be cycle-m but got " + signValue + ".")
            );
            helper.assertTrue(
                    "cycle-m".equals(sign.getFrontText().getMessage(1, false).getString()),
                    Component.literal("Expected sign line 2 to be cycle-m after script execution.")
            );

            Object spawnerValue = Variables.getVariable("cyclem::spawner-type", null, false);
            helper.assertTrue(
                    spawnerValue instanceof EntityData<?> data && data.matches(EntityType.COW),
                    Component.literal("Expected cyclem::spawner-type to resolve to cow but got " + spawnerValue + ".")
            );
            EntityData<?> liveType = new ch.njol.skript.expressions.ExprSpawnerType().convert(new FabricBlock(helper.getLevel(), spawnerPos));
            helper.assertTrue(
                    liveType != null && liveType.matches(EntityType.COW),
                    Component.literal("Expected spawner block to hold cow spawn data after script execution.")
            );

            runtime.clearScripts();
            Variables.clearAll();
            offlinePlayer = null;
            signBlock = null;
            spawnerBlock = null;
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleMSyntax1Event.class, "gametest cycle m syntax1 context");
        Skript.registerExpression(LaneMOfflinePlayerExpression.class, GameProfile.class, "lane-m-offlineplayer");
        Skript.registerExpression(LaneMSignBlockExpression.class, FabricBlock.class, "lane-m-sign-block");
        Skript.registerExpression(LaneMSpawnerBlockExpression.class, FabricBlock.class, "lane-m-spawner-block");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private record CycleMSyntax1Handle() {
    }

    public static final class GameTestCycleMSyntax1Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleMSyntax1Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleMSyntax1Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle m syntax1 context";
        }
    }

    public static final class LaneMOfflinePlayerExpression extends ch.njol.skript.lang.util.SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return offlinePlayer == null ? null : new GameProfile[]{offlinePlayer};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends GameProfile> getReturnType() {
            return GameProfile.class;
        }
    }

    public static final class LaneMSignBlockExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return signBlock == null ? null : new FabricBlock[]{signBlock};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }
    }

    public static final class LaneMSpawnerBlockExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return spawnerBlock == null ? null : new FabricBlock[]{spawnerBlock};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }
    }
}
