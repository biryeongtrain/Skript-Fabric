package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.events.SpawnReason;
import ch.njol.skript.events.TeleportCause;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.fabric.runtime.TeleportCauseCapture;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionCycle4GameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycle4ContextEvent.class, "gametest cycle 4 context");
    }

    // ── Lane A: ExprSpawnReason ──

    @GameTest
    public void spawnReasonCapturedViaEntityTypeCreate(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/spawn_reason_names_entity.sk");

            // helper.spawn() calls EntityType.create(Level, STRUCTURE)
            // which triggers EntityTypeCreateMixin → SpawnReasonCapture.set(STRUCTURE)
            // then addFreshEntity → dispatchEntityLoad → SpawnReasonCapture.consume()
            Zombie zombie = (Zombie) helper.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

            helper.assertTrue(
                    zombie.getCustomName() != null && "structure".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected spawn reason expression to resolve to 'structure' for helper.spawn(), got: "
                            + (zombie.getCustomName() != null ? zombie.getCustomName().getString() : "null"))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void spawnReasonRecordedToVariable(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/spawn_reason_records_variable.sk");

            helper.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

            Object value = Variables.getVariable("cycle4::spawn_reason", null, false);
            helper.assertTrue(
                    value instanceof SpawnReason && value == SpawnReason.STRUCTURE,
                    Component.literal("Expected spawn reason variable to be STRUCTURE but got: " + value)
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void spawnReasonNullForDirectConstruction(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/spawn_reason_records_variable.sk");

            // Direct construction bypasses EntityType.create(), so no spawn reason captured
            Zombie zombie = new Zombie(EntityType.ZOMBIE, helper.getLevel());
            zombie.setPos(0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(zombie);

            Object value = Variables.getVariable("cycle4::spawn_reason", null, false);
            helper.assertTrue(
                    value == null,
                    Component.literal("Expected spawn reason to be null for directly constructed entity but got: " + value)
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    // ── Lane B: ExprTeleportCause ──

    @GameTest
    public void teleportCauseUnknownForDirectTeleportTo(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/teleport_cause_records_variable.sk");

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 17.5F, 2.0F, 1.5F);
            zombie.teleportTo(18.5D, 2.0D, 1.5D);

            Object value = Variables.getVariable("cycle4::teleport_cause", null, false);
            helper.assertTrue(
                    value instanceof TeleportCause && value == TeleportCause.UNKNOWN,
                    Component.literal("Expected teleport cause to be UNKNOWN for plain teleportTo but got: " + value)
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void teleportCauseChorusFruitCaptured(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/teleport_cause_records_variable.sk");

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 17.5F, 2.0F, 1.5F);

            // Simulate chorus fruit teleport by setting the ThreadLocal then teleporting
            TeleportCauseCapture.set(TeleportCause.CHORUS_FRUIT);
            zombie.teleportTo(18.5D, 2.0D, 1.5D);

            Object value = Variables.getVariable("cycle4::teleport_cause", null, false);
            helper.assertTrue(
                    value instanceof TeleportCause && value == TeleportCause.CHORUS_FRUIT,
                    Component.literal("Expected teleport cause to be CHORUS_FRUIT but got: " + value)
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void teleportCauseCommandCaptured(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/teleport_cause_names_entity.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            BlockPos startAbsolute = helper.absolutePos(new BlockPos(17, 1, 0));
            player.teleportTo(startAbsolute.getX() + 0.5D, startAbsolute.getY() + 1.0D, startAbsolute.getZ() + 0.5D);

            // Clear any name from the initial teleport
            player.setCustomName(null);

            // Use /tp command to trigger TeleportCommandMixin
            BlockPos targetAbsolute = helper.absolutePos(new BlockPos(18, 1, 0));
            String command = "tp " + player.getScoreboardName()
                    + " " + (targetAbsolute.getX() + 0.5D)
                    + " " + (targetAbsolute.getY() + 1.0D)
                    + " " + (targetAbsolute.getZ() + 0.5D);
            helper.getLevel().getServer().getCommands().performPrefixedCommand(
                    helper.getLevel().getServer().createCommandSourceStack(), command
            );

            helper.assertTrue(
                    player.getCustomName() != null && "command".equals(player.getCustomName().getString()),
                    Component.literal("Expected teleport cause to be 'command' after /tp but got: "
                            + (player.getCustomName() != null ? player.getCustomName().getString() : "null"))
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void teleportCauseEnderPearlNamesEntity(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/teleport_cause_records_variable.sk");

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 17.5F, 2.0F, 1.5F);

            // Simulate ender pearl teleport by setting the ThreadLocal then teleporting
            TeleportCauseCapture.set(TeleportCause.ENDER_PEARL);
            zombie.teleportTo(18.5D, 2.0D, 1.5D);

            Object value = Variables.getVariable("cycle4::teleport_cause", null, false);
            helper.assertTrue(
                    value instanceof TeleportCause && value == TeleportCause.ENDER_PEARL,
                    Component.literal("Expected teleport cause to be ENDER_PEARL but got: " + value)
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    // ── Lane C: ExprPlugins alias ──

    @GameTest
    public void pluginsAliasReturnsModIds(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/plugins_alias_records_variable.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);

            int executed = runtime.dispatch(new SkriptEvent(
                    new Cycle4ContextHandle(),
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for plugins alias but got " + executed + ".")
            );

            Map<String, Object> plugins = Variables.getVariablesWithPrefix("cycle4::plugins::", null, false);
            helper.assertTrue(
                    plugins.containsValue("fabricloader"),
                    Component.literal("Expected 'loaded plugins' to include 'fabricloader'.")
            );
            helper.assertTrue(
                    plugins.containsValue("skript-fabric-port-gametest"),
                    Component.literal("Expected 'loaded plugins' to include the gametest mod id.")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    // ── Enum ClassInfo registration ──

    @GameTest
    public void spawnReasonEnumParsesCorrectly(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/spawn_reason_names_entity.sk");

            // spawn() uses EntityType.create with STRUCTURE reason
            Zombie zombie = (Zombie) helper.spawn(EntityType.ZOMBIE, new BlockPos(0, 1, 0));

            // Verify the enum was parsed and rendered correctly as a string
            helper.assertTrue(
                    zombie.getCustomName() != null && zombie.getCustomName().getString().equals("structure"),
                    Component.literal("Expected spawn reason to be formatted as 'structure' (enum toString) but got: "
                            + (zombie.getCustomName() != null ? zombie.getCustomName().getString() : "null"))
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void teleportCauseEnumParsesCorrectly(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/cycle-4/teleport_cause_names_entity.sk");

            Zombie zombie = (Zombie) helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 17.5F, 2.0F, 1.5F);
            TeleportCauseCapture.set(TeleportCause.CHORUS_FRUIT);
            zombie.teleportTo(18.5D, 2.0D, 1.5D);

            helper.assertTrue(
                    zombie.getCustomName() != null && "chorus fruit".equals(zombie.getCustomName().getString()),
                    Component.literal("Expected teleport cause to be formatted as 'chorus fruit' but got: "
                            + (zombie.getCustomName() != null ? zombie.getCustomName().getString() : "null"))
            );
            runtime.clearScripts();
        });
    }

    // ── Support types ──

    private record Cycle4ContextHandle() {
    }

    public static final class GameTestCycle4ContextEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof Cycle4ContextHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{Cycle4ContextHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle 4 context";
        }
    }
}
