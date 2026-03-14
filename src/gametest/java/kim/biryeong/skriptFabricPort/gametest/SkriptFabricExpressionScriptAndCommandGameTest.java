package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.util.Executable;

public final class SkriptFabricExpressionScriptAndCommandGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @GameTest
    public void scriptAndResultExpressionsExecuteRealScripts(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            runtime.loadFromResource("skript/gametest/expression/script-and-command/script_reflection_records_values.sk");
            runtime.loadFromResource("skript/gametest/expression/script-and-command/quests/daily/support.sk");
            runtime.loadFromResource("skript/gametest/expression/script-and-command/quests/weekly/bonus.sk");
            runtime.loadFromResource("skript/gametest/expression/script-and-command/legacy_scripts_record_values.sk");

            int executed = runtime.dispatch(new SkriptEvent(
                    new ScriptAndCommandHandle(),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));
            helper.assertTrue(
                    executed == 4,
                    Component.literal("Expected 4 script and command triggers but got " + executed + ".")
            );
            helper.assertTrue(
                    Boolean.TRUE.equals(Variables.getVariable("scriptcmd::support::daily", null, false)),
                    Component.literal("Expected daily support script to execute on the custom GameTest event.")
            );
            helper.assertTrue(
                    Boolean.TRUE.equals(Variables.getVariable("scriptcmd::support::weekly", null, false)),
                    Component.literal("Expected weekly support script to execute on the custom GameTest event.")
            );

            assertScriptPath(helper, "scriptcmd::reflection::current", "skript/gametest/expression/script-and-command/script_reflection_records_values.sk");
            assertScriptPath(helper, "scriptcmd::reflection::named", "skript/gametest/expression/script-and-command/quests/daily/support.sk");
            assertNumericValue(helper, "scriptcmd::reflection::directory_count", 2);
            assertScriptPath(helper, "scriptcmd::reflection::directory::1", "skript/gametest/expression/script-and-command/quests/daily/support.sk");
            assertScriptPath(helper, "scriptcmd::reflection::directory::2", "skript/gametest/expression/script-and-command/quests/weekly/bonus.sk");
            helper.assertTrue(
                    "executed:0".equals(Variables.getVariable("scriptcmd::reflection::single_result", null, false)),
                    Component.literal("Expected single result expression to store executed:0.")
            );
            helper.assertTrue(
                    "alpha".equals(Variables.getVariable("scriptcmd::reflection::plural_result::1", null, false)),
                    Component.literal("Expected first plural result value to be alpha.")
            );
            helper.assertTrue(
                    Integer.valueOf(7).equals(Variables.getVariable("scriptcmd::reflection::plural_result::2", null, false)),
                    Component.literal("Expected second plural result value to be 7.")
            );

            assertScriptPath(helper, "scriptcmd::legacy::all::1", "skript/gametest/expression/script-and-command/script_reflection_records_values.sk");
            assertScriptPath(helper, "scriptcmd::legacy::all::2", "skript/gametest/expression/script-and-command/quests/daily/support.sk");
            assertScriptPath(helper, "scriptcmd::legacy::all::3", "skript/gametest/expression/script-and-command/quests/weekly/bonus.sk");
            assertScriptPath(helper, "scriptcmd::legacy::all::4", "skript/gametest/expression/script-and-command/legacy_scripts_record_values.sk");
            helper.assertTrue(
                    "script_reflection_records_values.sk".equals(Variables.getVariable("scriptcmd::legacy::enabled::1", null, false)),
                    Component.literal("Expected first legacy enabled script leaf name to be script_reflection_records_values.sk.")
            );
            helper.assertTrue(
                    "support.sk".equals(Variables.getVariable("scriptcmd::legacy::enabled::2", null, false)),
                    Component.literal("Expected second legacy enabled script leaf name to be support.sk.")
            );
            helper.assertTrue(
                    "bonus.sk".equals(Variables.getVariable("scriptcmd::legacy::enabled::3", null, false)),
                    Component.literal("Expected third legacy enabled script leaf name to be bonus.sk.")
            );
            helper.assertTrue(
                    "legacy_scripts_record_values.sk".equals(Variables.getVariable("scriptcmd::legacy::enabled::4", null, false)),
                    Component.literal("Expected fourth legacy enabled script leaf name to be legacy_scripts_record_values.sk.")
            );
            assertNumericValue(helper, "scriptcmd::legacy::disabled_count", 0);

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void commandInfoExecutesOnRealCommandEvent(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/script-and-command/command_info_records_values.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            helper.getLevel().getServer().getCommands().performPrefixedCommand(
                    player.createCommandSourceStack(),
                    "say cycle-f live hook"
            );

            helper.assertTrue(
                    "say".equals(Variables.getVariable("scriptcmd::command::current_label", null, false)),
                    Component.literal("Expected current command label to be say.")
            );
            helper.assertTrue(
                    "/say".equals(Variables.getVariable("scriptcmd::command::current_usage", null, false)),
                    Component.literal("Expected current command usage to be /say.")
            );
            helper.assertTrue(
                    "say".equals(Variables.getVariable("scriptcmd::command::named_label", null, false)),
                    Component.literal("Expected named command label to be say.")
            );
            helper.assertTrue(
                    "/say".equals(Variables.getVariable("scriptcmd::command::named_usage", null, false)),
                    Component.literal("Expected named command usage to be /say.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected command info script to mark the block under the command sender.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void minecartExpressionsExecuteRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/script-and-command/syntax1/minecart_properties_record_values.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            MinecartChest minecart = new MinecartChest(EntityType.CHEST_MINECART, helper.getLevel());
            minecart.setPos(0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(minecart);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    minecart,
                    new EntityHitResult(minecart)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected minecart expression script to keep the use entity callback in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected minecart expression script to mark the player position.")
            );

            assertNumber(helper, "scriptcmd::minecart::max_before_reset", 1.0D);
            assertNumber(helper, "scriptcmd::minecart::max_after_reset", 0.4D);
            assertVector(helper, "scriptcmd::minecart::derailed", 2.0D, 2.0D, 2.0D);
            assertVector(helper, "scriptcmd::minecart::flying", 3.0D, 4.0D, 5.0D);

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void compassTargetExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/script-and-command/syntax1/compass_target_records_value.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(4, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    cow,
                    new EntityHitResult(cow)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected compass target script to keep the use entity callback in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(markerAbsolute).is(Blocks.DIAMOND_BLOCK),
                    Component.literal("Expected compass target expression script to mark the player position.")
            );

            Object value = Variables.getVariable("scriptcmd::compass::target", null, false);
            helper.assertTrue(
                    value instanceof FabricLocation location
                            && samePosition(location.position(), cow.position()),
                    Component.literal("Expected compass target variable to capture the interacted entity location but got " + value + ".")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void portalExpressionExecutesRealScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/script-and-command/syntax1/portal_blocks_mark_above.sk");

            BlockPos portalPos = helper.absolutePos(new BlockPos(0, 1, 0));
            helper.getLevel().setBlockAndUpdate(portalPos, Blocks.END_PORTAL.defaultBlockState());

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 0.5F, 1.0F, 0.5F);
            invokeEndPortalEntityInside(helper, portalPos, zombie);
            zombie.tick();

            helper.assertTrue(
                    helper.getLevel().getBlockState(portalPos.above()).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected portal expression script to mark the block above the active portal.")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void consoleLiteralExecutesRealScript(GameTestHelper helper) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            if (!loaded.get()) {
                if (!RUNTIME_LOCK.compareAndSet(false, true)) {
                    return;
                }
                Variables.clearAll();
                runtime.clearScripts();
                runtime.loadFromResource("skript/gametest/expression/script-and-command/syntax1/console_records_value.sk");
                loaded.set(true);
                return;
            }

            Object value = Variables.getVariable("scriptcmd::console", null, false);
            MinecraftServer server = helper.getLevel().getServer();
            helper.assertTrue(
                    value == server,
                    Component.literal("Expected console literal to resolve the active server instance but got " + value + ".")
            );

            runtime.clearScripts();
            Variables.clearAll();
            RUNTIME_LOCK.set(false);
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        registerClassInfo(Executable.class, "executable");
        registerClassInfo(Object.class, "object");
        registerClassInfo(Script.class, "script");
        Skript.registerEvent(GameTestScriptAndCommandEvent.class, "gametest script and command context");
        Skript.registerExpression(ScriptCmdExecutableExpression.class, Executable.class, "scriptcmd-executable");
        Skript.registerExpression(ScriptCmdArgumentsExpression.class, Object.class, "scriptcmd-args");
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static void assertScriptPath(GameTestHelper helper, String variable, String expectedPath) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Script script && expectedPath.equals(script.getConfig().getFileName()),
                Component.literal("Expected " + variable + " to point to " + expectedPath + " but got " + value + ".")
        );
    }

    private static void assertNumericValue(GameTestHelper helper, String variable, int expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number number && number.intValue() == expected,
                Component.literal("Expected " + variable + " to be " + expected + " but got " + value + ".")
        );
    }

    private static void assertNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Double.compare(((Number) value).doubleValue(), expected) == 0,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private static void assertVector(GameTestHelper helper, String variable, double x, double y, double z) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Vec3 vector
                        && Double.compare(vector.x, x) == 0
                        && Double.compare(vector.y, y) == 0
                        && Double.compare(vector.z, z) == 0,
                Component.literal("Expected " + variable + " to equal Vec3[" + x + ", " + y + ", " + z + "] but got " + value + ".")
        );
    }

    private static boolean samePosition(Vec3 first, Vec3 second) {
        return Double.compare(first.x, second.x) == 0
                && Double.compare(first.y, second.y) == 0
                && Double.compare(first.z, second.z) == 0;
    }

    private record ScriptAndCommandHandle() {
    }

    public static final class GameTestScriptAndCommandEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ScriptAndCommandHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ScriptAndCommandHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest script and command context";
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final class ScriptCmdExecutableExpression extends SimpleExpression<Executable> {
        @Override
        protected Executable @Nullable [] get(SkriptEvent event) {
            return new Executable[]{
                    (caller, arguments) -> arguments.length == 0 ? "executed:0" : new Object[]{arguments[0], arguments[1]}
            };
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Executable> getReturnType() {
            return Executable.class;
        }
    }

    public static final class ScriptCmdArgumentsExpression extends SimpleExpression<Object> {
        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return new Object[]{"alpha", 7};
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }
    }
}
