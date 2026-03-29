package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.Config;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.expressions.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import ch.njol.skript.util.Time;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.util.SkriptQueue;
import org.skriptlang.skript.util.Executable;

public final class SkriptFabricExpressionParseBatchGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean TEST_EXPRESSIONS_REGISTERED = new AtomicBoolean(false);

    private static synchronized void ensureTestExpressions() {
        if (!TEST_EXPRESSIONS_REGISTERED.compareAndSet(false, true)) {
            return;
        }

        // Register class infos if not already present
        registerClassInfo(Executable.class, "executable");
        registerClassInfo(Script.class, "script");
        registerClassInfo(FabricLocation.class, "location");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(LevelChunk.class, "chunk");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(Projectile.class, "projectile");
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(WorldBorder.class, "worldborder");
        registerClassInfo(String.class, "string");
        registerClassInfo(Number.class, "number");
        registerClassInfo(Object.class, "object");
        registerClassInfo(SkriptQueue.class, "queue");
        registerClassInfo(FabricItemType.class, "itemtype");
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(FabricInventory.class, "inventory");
        registerClassInfo(Holder.class, "potioneffecttype");
        registerClassInfo(java.util.UUID.class, "uuid");
        registerClassInfo(AbstractArrow.class, "arrow");

        // Register test expressions for parse tests
        Skript.registerExpression(TestExecutableExpression.class, Executable.class, "lane-f-binding-executable");
        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-s2-world");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-s2-player");
        Skript.registerExpression(TestWorldBorderExpression.class, WorldBorder.class, "lane-s2-worldborder");
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-s2-location");
        Skript.registerExpression(TestStringExpression.class, String.class, "lane-s2-string");
        Skript.registerExpression(TestWorldExpression2.class, ServerLevel.class, "lane-s2-first-world");
        Skript.registerExpression(TestLocationExpression2.class, FabricLocation.class, "lane-s2-first-location");
        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-s2-first-block");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-s2-first-entity");
        Skript.registerExpression(TestChunkExpression.class, LevelChunk.class, "lane-s2-first-chunk");
        Skript.registerExpression(TestStringExpression2.class, String.class, "lane-s2-first-string");
        Skript.registerExpression(TestEntityExpression2.class, Entity.class, "lane-s4-entity");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-s4-livingentity");
        Skript.registerExpression(TestProjectileExpression.class, Projectile.class, "lane-s4-projectile");
        Skript.registerExpression(LaneKValuesExpression.class, Object.class, "lane-k-values");
        Skript.registerExpression(LaneKItemTypeExpression.class, FabricItemType.class, "lane-k-itemtype");
        Skript.registerExpression(TestLivingEntityExpressionJ.class, LivingEntity.class, "lane-j-livingentity");
        Skript.registerExpression(TestLocationExpressionJ.class, FabricLocation.class, "lane-j-location");
        Skript.registerExpression(LaneMOfflinePlayerExpression.class, GameProfile.class, "lane-m-offlineplayer");
        Skript.registerExpression(LaneMSignBlockExpression.class, FabricBlock.class, "lane-m-sign-block");
        Skript.registerExpression(LaneMSpawnerBlockExpression.class, FabricBlock.class, "lane-m-spawner-block");
        Skript.registerExpression(TestM6StringExpression.class, String.class, "lane-m6-strings");
        Skript.registerExpression(TestM6NumberListExpression.class, Number.class, "lane-m6-number-list");
        Skript.registerExpression(TestM6QueueExpression.class, SkriptQueue.class, "lane-m6-queue");
        Skript.registerExpression(TestM6KeyedStringExpression.class, String.class, "lane-m6-keyed-strings");
        Skript.registerExpression(TestM6NestedStringExpression.class, String.class, "lane-m6-nested-strings");
        Skript.registerExpression(TestUuidExpression.class, java.util.UUID.class, "lane-c20260312-s4-uuid");
        Skript.registerExpression(TestArrowExpression.class, AbstractArrow.class, "lane-c20260312-s4-projectile");
        Skript.registerExpression(TestE2BlockExpression.class, FabricBlock.class, "lane-e2-block");
        Skript.registerExpression(TestE2ItemTypeExpression.class, FabricItemType.class, "lane-e2-itemtype");
        Skript.registerExpression(TestE2LivingEntityExpression.class, LivingEntity.class, "lane-e2-livingentity");
        Skript.registerExpression(TestM2PlayerExpression.class, ServerPlayer.class, "lane-m2-player");
        Skript.registerExpression(TestM2LivingEntityExpression.class, LivingEntity.class, "lane-m2-livingentity");
        Skript.registerExpression(TestM2EntityExpression.class, Entity.class, "lane-m2-entity");
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null && Classes.getClassInfoNoError(codeName) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    // =========================================================================
    // Test methods for cycleF2 parse
    // =========================================================================

    @GameTest
    public void cycleF2ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        ParserInstance parser = ParserInstance.get();
        try {
            Class<?> commandEventClass = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
            parser.setCurrentScript(new Script(new Config("gametest-f2", "gametest-f2.sk", null), new ArrayList<>()));
            parser.setCurrentEvent("command", commandEventClass);
            assertParseType(helper, "main command label of command \"/say hello\"", ExprCommandInfo.class, String.class);
            assertParseType(helper, "current script", ExprScript.class, Script.class);
            assertParseType(helper, "all enabled scripts without paths", ExprScriptsOld.class, String.class);
            assertParseType(helper, "result of lane-f-binding-executable", ExprResult.class, Object.class);
        } catch (ClassNotFoundException e) {
            helper.fail(Component.literal("Could not find command event class: " + e.getMessage()));
        } finally {
            parser.deleteCurrentEvent();
            parser.setCurrentScript(null);
        }
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycleK parse
    // =========================================================================

    @GameTest
    public void cycleKExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        ParserInstance parser = ParserInstance.get();

        assertParseType(helper, "first element out of lane-k-values", ExprElement.class, Object.class);
        assertParseType(helper, "3 of lane-k-itemtype", ExprXOf.class, Object.class);

        parser.setCurrentSections(List.of(new StubLoopSection()));
        try {
            assertParseType(helper, "loop-value", ExprLoopValue.class, Object.class);
            assertParseType(helper, "previous loop-value", ExprLoopValue.class, Object.class);
            assertParseType(helper, "next loop-value", ExprLoopValue.class, Object.class);
        } finally {
            parser.setCurrentSections(List.of());
        }
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycleL parse
    // =========================================================================

    @GameTest
    public void cycleLExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentEvent("shoot bow", FabricEventCompatHandles.EntityShootBow.class);
        try {
            assertParseType(helper, "projectile force", ExprProjectileForce.class, Float.class);
        } finally {
            parser.deleteCurrentEvent();
        }
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycleM parse
    // =========================================================================

    @GameTest
    public void cycleMExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "skull of lane-m-offlineplayer", ExprSkull.class, FabricItemType.class);
        assertParseType(helper, "line 2 of lane-m-sign-block", ExprSignText.class, String.class);
        @SuppressWarnings("unchecked")
        Class<EntityData<?>> entityDataType = (Class<EntityData<?>>) (Class<?>) EntityData.class;
        assertParseType(helper, "spawner type of lane-m-spawner-block", ExprSpawnerType.class, entityDataType);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for world border parse (syntaxS2)
    // =========================================================================

    @GameTest
    public void worldBorderExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "all worlds", ExprWorlds.class, ServerLevel.class);
        assertParseType(helper, "world named lane-s2-string", ExprWorldFromName.class, ServerLevel.class);
        assertParseType(helper, "world border of lane-s2-world", ExprWorldBorder.class, WorldBorder.class);
        assertParseType(helper, "world border of lane-s2-player", ExprWorldBorder.class, WorldBorder.class);
        assertParseType(helper, "world border center of lane-s2-worldborder", ExprWorldBorderCenter.class, FabricLocation.class);
        assertParseType(helper, "world border radius of lane-s2-worldborder", ExprWorldBorderSize.class, Double.class);
        assertParseType(helper, "world border damage amount of lane-s2-worldborder", ExprWorldBorderDamageAmount.class, Double.class);
        assertParseType(helper, "world border damage buffer of lane-s2-worldborder", ExprWorldBorderDamageBuffer.class, Double.class);
        assertParseType(helper, "world border warning distance of lane-s2-worldborder", ExprWorldBorderWarningDistance.class, Integer.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for syntaxS4 parse
    // =========================================================================

    @GameTest
    public void syntaxS4ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "vehicle of lane-s4-entity", ExprVehicle.class, Object.class);
        assertParseType(helper, "passengers of lane-s4-entity", ExprPassenger.class, Object.class);
        assertParseType(helper, "target of lane-s4-livingentity", ExprTarget.class, Object.class);
        assertParseType(helper, "shooter of lane-s4-projectile", ExprShooter.class, Object.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for world/time parse
    // =========================================================================

    @GameTest
    public void worldTimeExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "world of lane-s2-first-location", ExprWorld.class, ServerLevel.class);
        assertParseType(helper, "lane-s2-first-location's world", ExprWorld.class, ServerLevel.class);
        assertParseType(helper, "world of lane-s2-first-entity", ExprWorld.class, ServerLevel.class);
        assertParseType(helper, "world of lane-s2-first-chunk", ExprWorld.class, ServerLevel.class);
        assertParseType(helper, "all worlds", ExprWorlds.class, ServerLevel.class);
        assertParseType(helper, "world named lane-s2-first-string", ExprWorldFromName.class, ServerLevel.class);
        assertParseType(helper, "environment of lane-s2-first-world", ExprWorldEnvironment.class, String.class);
        assertParseType(helper, "temperature of lane-s2-first-block", ExprTemperature.class, Number.class);
        assertParseType(helper, "time within lane-s2-first-world", ExprTime.class, Time.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for mixed runtime M6 parse
    // =========================================================================

    @GameTest
    public void mixedRuntimeM6ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "last caught runtime errors", ExprCaughtErrors.class, String.class);
        assertParseType(helper, "dequeued lane-m6-queue", ExprDequeuedQueue.class, Object.class);
        assertParseType(helper, "the function named \"lane_m6_echo\"", ExprFunction.class, DynamicFunctionReference.class);
        assertParseType(helper, "keyed lane-m6-keyed-strings", ExprKeyed.class, Object.class);
        assertParseType(helper, "a queue of lane-m6-strings", ExprQueue.class, SkriptQueue.class);
        assertParseType(helper, "start of lane-m6-queue", ExprQueueStartEnd.class, Object.class);
        assertParseType(helper, "recursive lane-m6-nested-strings", ExprRecursive.class, Object.class);
        assertParseType(helper, "lane-m6-strings repeated 3 times", ExprRepeat.class, String.class);
        assertParseType(helper, "rounded lane-m6-number-list", ExprRound.class, Long.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for syntax4 parse (from UUID, memory, etc.)
    // =========================================================================

    @GameTest
    public void syntax4ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "offline player from lane-c20260312-s4-uuid", ExprFromUUID.class, GameProfile.class);
        assertParseType(helper, "entity from lane-c20260312-s4-uuid", ExprFromUUID.class, Entity.class);
        assertParseType(helper, "world from lane-c20260312-s4-uuid", ExprFromUUID.class, ServerLevel.class);
        assertParseType(helper, "free memory", ExprMemory.class, Double.class);
        assertParseType(helper, "maximum ram", ExprMemory.class, Double.class);
        assertParseType(helper, "projectile critical state of lane-c20260312-s4-projectile", ExprProjectileCriticalState.class, Boolean.class);
        assertParseType(helper, "all banned players", ExprAllBannedEntries.class, GameProfile.class);
        assertParseType(helper, "all banned ip addresses", ExprAllBannedEntries.class, String.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycle E syntax2 parse (plain, named, item flags)
    // =========================================================================

    @GameTest
    public void cycleESyntax2ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "sea pickle count of lane-e2-block", ExprSeaPickles.class, Integer.class);
        assertParseType(helper, "plain lane-e2-itemtype", ExprPlain.class, FabricItemType.class);
        assertParseType(helper, "lane-e2-itemtype named \"Lane E\"", ExprNamed.class, FabricItemType.class);
        assertParseType(helper, "lane-e2-itemtype with item flags \"hide enchants\"", ExprWithItemFlags.class, FabricItemType.class);
        assertParseType(helper, "carried blockdata of lane-e2-livingentity", ExprCarryingBlockData.class, net.minecraft.world.level.block.state.BlockState.class);
        assertParseType(helper, "most angered entity of lane-e2-livingentity", ExprWardenAngryAt.class, LivingEntity.class);
        assertParseType(helper, "anger level of lane-e2-livingentity towards lane-e2-livingentity", ExprWardenEntityAnger.class, Integer.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for inventory container parse
    // =========================================================================

    @GameTest
    public void inventoryContainerExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        assertParseType(helper, "open inventory of lane-m2-player", ExprOpenedInventory.class, FabricInventory.class);
        assertParseType(helper, "armor items of lane-m2-livingentity", ExprArmorSlot.class, Slot.class);
        assertParseType(helper, "pickup delay of lane-m2-entity", ExprPickupDelay.class, ch.njol.skript.util.Timespan.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycleJ parse
    // =========================================================================

    @GameTest
    public void cycleJExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("beacon effect", FabricEventCompatHandles.BeaconEffect.class);
        try {
            assertParseType(helper, "applied effect", ExprAppliedEffect.class, Holder.class);
        } finally {
            parser.deleteCurrentEvent();
        }

        assertParseType(helper, "nearest cow relative to lane-j-location", ExprNearestEntity.class, Entity.class);
        assertParseType(helper, "target block of lane-j-livingentity", ExprTargetedBlock.class, FabricBlock.class);
        assertParseType(helper, "actual target block of lane-j-livingentity", ExprTargetedBlock.class, FabricBlock.class);
        helper.succeed();
    }

    // =========================================================================
    // Test methods for cycleF Safe5 parse
    // =========================================================================

    @GameTest
    public void cycleFSafe5ExpressionsParse(GameTestHelper helper) {
        ensureTestExpressions();
        // ExprEnchantmentLevel, ExprEnchantments, ExprTypeOf, ExprSkullOwner, ExprMe require
        // specific lane expressions; since they are already tested by the safe5 test,
        // here we verify the registered class infos and ExprMe parsing in GameTest context
        ParserInstance parser = ParserInstance.get();
        try {
            Class<?> commandEventClass = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
            parser.setCurrentEvent("command", commandEventClass);
            assertParseType(helper, "me", ExprMe.class, ServerPlayer.class);
        } catch (ClassNotFoundException e) {
            helper.fail(Component.literal("Could not find command event class: " + e.getMessage()));
        } finally {
            parser.deleteCurrentEvent();
        }
        helper.succeed();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void assertParseType(GameTestHelper helper, String input, Class<?> expectedType, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(returnTypes.length > 0 ? returnTypes : new Class[]{Object.class});
        helper.assertTrue(parsed != null, Component.literal(input + " => expected: not null"));
        helper.assertTrue(expectedType.isInstance(parsed),
                Component.literal(input + " => expected: " + expectedType.getSimpleName()
                        + " but was: " + (parsed == null ? "null" : parsed.getClass().getSimpleName())));
    }

    // =========================================================================
    // Stub loop section for loop-value parse tests
    // =========================================================================

    private static final class StubLoopSection extends SecLoop {
        private final Expression<?> looped = new SimpleLiteral<>(new String[]{"alpha", "beta", "gamma"}, String.class, true);

        @Override
        public @Nullable Object getCurrent(SkriptEvent event) {
            return "beta";
        }

        @Override
        public @Nullable Object getPrevious(SkriptEvent event) {
            return "alpha";
        }

        @Override
        public @Nullable Object getNext(SkriptEvent event) {
            return "gamma";
        }

        @Override
        public boolean supportsPeeking() {
            return true;
        }

        @Override
        public Expression<?> getLoopedExpression() {
            return looped;
        }

        @Override
        public Expression<?> getExpression() {
            return looped;
        }

        @Override
        public boolean isKeyedLoop() {
            return false;
        }

        @Override
        public SecLoop setNext(@Nullable TriggerItem next) {
            return this;
        }

        @Override
        public @Nullable TriggerItem getActualNext() {
            return null;
        }

        @Override
        public void exit(SkriptEvent event) {
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stub loop";
        }
    }

    // =========================================================================
    // Test expression stubs: minimal expressions returning specific types
    // =========================================================================

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final class TestExecutableExpression extends SimpleExpression<Executable> {
        @Override
        protected Executable @Nullable [] get(SkriptEvent event) {
            return new Executable[]{(caller, arguments) -> "ok"};
        }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends Executable> getReturnType() { return Executable.class; }
    }

    public static final class TestWorldExpression extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) { return new ServerLevel[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends ServerLevel> getReturnType() { return ServerLevel.class; }
    }

    public static final class TestWorldExpression2 extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) { return new ServerLevel[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends ServerLevel> getReturnType() { return ServerLevel.class; }
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) { return new ServerPlayer[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends ServerPlayer> getReturnType() { return ServerPlayer.class; }
    }

    public static final class TestWorldBorderExpression extends SimpleExpression<WorldBorder> {
        @Override
        protected WorldBorder @Nullable [] get(SkriptEvent event) { return new WorldBorder[]{new WorldBorder()}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends WorldBorder> getReturnType() { return WorldBorder.class; }
    }

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) { return new FabricLocation[]{new FabricLocation(null, Vec3.ZERO)}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricLocation> getReturnType() { return FabricLocation.class; }
    }

    public static final class TestLocationExpression2 extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) { return new FabricLocation[]{new FabricLocation((ServerLevel) null, Vec3.ZERO)}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricLocation> getReturnType() { return FabricLocation.class; }
    }

    public static final class TestStringExpression extends SimpleExpression<String> {
        @Override
        protected String @Nullable [] get(SkriptEvent event) { return new String[]{"overworld"}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends String> getReturnType() { return String.class; }
    }

    public static final class TestStringExpression2 extends SimpleExpression<String> {
        @Override
        protected String @Nullable [] get(SkriptEvent event) { return new String[]{"overworld"}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends String> getReturnType() { return String.class; }
    }

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) { return new FabricBlock[]{new FabricBlock((ServerLevel) null, BlockPos.ZERO)}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricBlock> getReturnType() { return FabricBlock.class; }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) { return new Entity[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends Entity> getReturnType() { return Entity.class; }
    }

    public static final class TestEntityExpression2 extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends Entity> getReturnType() { return Entity.class; }
    }

    public static final class TestChunkExpression extends SimpleExpression<LevelChunk> {
        @Override
        protected LevelChunk @Nullable [] get(SkriptEvent event) { return new LevelChunk[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends LevelChunk> getReturnType() { return LevelChunk.class; }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends LivingEntity> getReturnType() { return LivingEntity.class; }
    }

    public static final class TestProjectileExpression extends SimpleExpression<Projectile> {
        @Override
        protected Projectile @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends Projectile> getReturnType() { return Projectile.class; }
    }

    public static final class LaneKValuesExpression extends SimpleExpression<Object> implements KeyProviderExpression<Object> {
        private static final Object[] VALUES = new Object[]{"alpha", "beta", "gamma"};
        private static final String[] KEYS = new String[]{"first", "second", "third"};

        @Override
        protected Object @Nullable [] get(SkriptEvent event) { return Arrays.copyOf(VALUES, VALUES.length); }

        @Override
        public @Nullable Iterator<? extends Object> iterator(SkriptEvent event) { return Arrays.asList(VALUES).iterator(); }

        @Override
        public boolean supportsLoopPeeking() { return true; }

        @Override
        public boolean isSingle() { return false; }

        @Override
        public Class<?> getReturnType() { return Object.class; }

        @Override
        public String[] getArrayKeys(SkriptEvent event) { return Arrays.copyOf(KEYS, KEYS.length); }

        @Override
        public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) { return Arrays.asList(KeyedValue.zip(VALUES, KEYS)).iterator(); }
    }

    public static final class LaneKItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) { return new FabricItemType[]{new FabricItemType(Items.DIAMOND)}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricItemType> getReturnType() { return FabricItemType.class; }
    }

    public static final class TestLivingEntityExpressionJ extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends LivingEntity> getReturnType() { return LivingEntity.class; }
    }

    public static final class TestLocationExpressionJ extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricLocation> getReturnType() { return FabricLocation.class; }
    }

    public static final class LaneMOfflinePlayerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) { return new GameProfile[]{new GameProfile(java.util.UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "cyclem")}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends GameProfile> getReturnType() { return GameProfile.class; }
    }

    public static final class LaneMSignBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricBlock> getReturnType() { return FabricBlock.class; }
    }

    public static final class LaneMSpawnerBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricBlock> getReturnType() { return FabricBlock.class; }
    }

    public static class TestM6StringExpression extends SimpleExpression<String> {
        @Override
        protected String[] get(SkriptEvent event) { return new String[]{"alpha", "beta"}; }

        @Override
        public boolean isSingle() { return false; }

        @Override
        public Class<? extends String> getReturnType() { return String.class; }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, SkriptParser.ParseResult parseResult) { return true; }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) { return "lane-m6-strings"; }
    }

    public static class TestM6NumberListExpression extends SimpleExpression<Number> {
        @Override
        protected Number[] get(SkriptEvent event) { return new Number[]{1.2, 2.8}; }

        @Override
        public boolean isSingle() { return false; }

        @Override
        public Class<? extends Number> getReturnType() { return Number.class; }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, SkriptParser.ParseResult parseResult) { return true; }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) { return "lane-m6-number-list"; }
    }

    public static class TestM6QueueExpression extends SimpleExpression<SkriptQueue> {
        @Override
        protected SkriptQueue[] get(SkriptEvent event) {
            SkriptQueue queue = new SkriptQueue();
            queue.add("first");
            queue.add("second");
            return new SkriptQueue[]{queue};
        }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends SkriptQueue> getReturnType() { return SkriptQueue.class; }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, SkriptParser.ParseResult parseResult) { return true; }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) { return "lane-m6-queue"; }
    }

    public static class TestM6KeyedStringExpression extends SimpleExpression<String> implements KeyProviderExpression<String> {
        @Override
        protected String[] get(SkriptEvent event) { return new String[]{"alpha", "beta"}; }

        @Override
        public String[] getArrayKeys(SkriptEvent event) { return new String[]{"a", "b"}; }

        @Override
        public boolean isSingle() { return false; }

        @Override
        public Class<? extends String> getReturnType() { return String.class; }

        @Override
        public boolean init(Expression<?>[] expressions, int matchedPattern, ch.njol.util.Kleenean isDelayed, SkriptParser.ParseResult parseResult) { return true; }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) { return "lane-m6-keyed-strings"; }
    }

    public static final class TestM6NestedStringExpression extends TestM6KeyedStringExpression {
        @Override
        public String[] getArrayKeys(SkriptEvent event) { return new String[]{"branch::a", "branch::b"}; }

        @Override
        public boolean returnNestedStructures(boolean nested) { return nested; }

        @Override
        public boolean returnsNestedStructures() { return true; }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) { return "lane-m6-nested-strings"; }
    }

    public static final class TestUuidExpression extends SimpleExpression<java.util.UUID> {
        @Override
        protected java.util.UUID @Nullable [] get(SkriptEvent event) { return new java.util.UUID[]{java.util.UUID.randomUUID()}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends java.util.UUID> getReturnType() { return java.util.UUID.class; }
    }

    public static final class TestArrowExpression extends SimpleExpression<AbstractArrow> {
        @Override
        protected AbstractArrow @Nullable [] get(SkriptEvent event) { return new AbstractArrow[0]; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends AbstractArrow> getReturnType() { return AbstractArrow.class; }
    }

    public static final class TestE2BlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricBlock> getReturnType() { return FabricBlock.class; }
    }

    public static final class TestE2ItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) { return new FabricItemType[]{new FabricItemType(Items.DIAMOND_SWORD)}; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends FabricItemType> getReturnType() { return FabricItemType.class; }
    }

    public static final class TestE2LivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends LivingEntity> getReturnType() { return LivingEntity.class; }
    }

    public static final class TestM2PlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends ServerPlayer> getReturnType() { return ServerPlayer.class; }
    }

    public static final class TestM2LivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends LivingEntity> getReturnType() { return LivingEntity.class; }
    }

    public static final class TestM2EntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) { return null; }

        @Override
        public boolean isSingle() { return true; }

        @Override
        public Class<? extends Entity> getReturnType() { return Entity.class; }
    }
}
