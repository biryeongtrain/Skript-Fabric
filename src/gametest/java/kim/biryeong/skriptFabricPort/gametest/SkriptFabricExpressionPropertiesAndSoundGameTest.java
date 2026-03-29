package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionPropertiesAndSoundGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    private static volatile @Nullable ServerPlayer testPlayer;
    private static volatile @Nullable Entity testEntity;
    private static volatile @Nullable ItemStack testItem;
    private static volatile @Nullable FabricLocation testLocation;

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestPropertiesAndSoundEvent.class, "gametest properties and sound context");
        Skript.registerExpression(PropSoundPlayerExpression.class, ServerPlayer.class, "propsound-player");
        Skript.registerExpression(PropSoundEntityExpression.class, Entity.class, "propsound-entity");
        Skript.registerExpression(PropSoundItemExpression.class, ItemStack.class, "propsound-item");
        Skript.registerExpression(PropSoundLocationExpression.class, FabricLocation.class, "propsound-location");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    // ── Lane A: Properties Module ──

    @GameTest
    public void propExprNameReturnsPlayerAccountName(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            testPlayer = helper.makeMockServerPlayerInLevel();
            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/name_of_player_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for name of player but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::player_name", null, false);
            helper.assertTrue(
                    value instanceof String name && name.equals(testPlayer.getGameProfile().name()),
                    Component.literal("Expected player name '" + testPlayer.getGameProfile().name() + "' but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testPlayer = null;
        });
    }

    @GameTest
    public void propExprCustomNameSetsAndReadsEntityDisplayName(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            testEntity = cow;

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/display_name_of_entity_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for display name but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::display_name", null, false);
            helper.assertTrue(
                    value != null && value.toString().contains("TestCustomName"),
                    Component.literal("Expected display name containing 'TestCustomName' but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testEntity = null;
        });
    }

    @GameTest
    public void propExprAmountReturnsItemStackCount(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            testItem = new ItemStack(Items.DIAMOND, 42);

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/amount_of_item_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for amount of item but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::item_amount", null, false);
            helper.assertTrue(
                    value instanceof Number n && n.intValue() == 42,
                    Component.literal("Expected item amount 42 but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testItem = null;
        });
    }

    @GameTest
    public void propExprSizeReturnsListLength(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/size_of_list_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for size of list but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::list_size", null, false);
            helper.assertTrue(
                    value instanceof Number n && n.longValue() == 3L,
                    Component.literal("Expected list size 3 but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void propExprWXYZReturnsLocationCoordinates(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            testLocation = new FabricLocation(helper.getLevel(), new Vec3(10.5, 64.0, -30.25));

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/xyz_of_location_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for xyz of location but got " + executed + ".")
            );

            Object xVal = Variables.getVariable("props::loc_x", null, false);
            Object yVal = Variables.getVariable("props::loc_y", null, false);
            Object zVal = Variables.getVariable("props::loc_z", null, false);
            helper.assertTrue(
                    xVal instanceof Number x && Math.abs(x.doubleValue() - 10.5) < 0.01,
                    Component.literal("Expected x=10.5 but got " + xVal + ".")
            );
            helper.assertTrue(
                    yVal instanceof Number y && Math.abs(y.doubleValue() - 64.0) < 0.01,
                    Component.literal("Expected y=64.0 but got " + yVal + ".")
            );
            helper.assertTrue(
                    zVal instanceof Number z && Math.abs(z.doubleValue() - (-30.25)) < 0.01,
                    Component.literal("Expected z=-30.25 but got " + zVal + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testLocation = null;
        });
    }

    // ── Lane B: ExprBannerItem ──

    @GameTest
    public void exprBannerItemReturnsCreeperBannerPatternItem(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/banner_item_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for banner item but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::banner_item", null, false);
            helper.assertTrue(
                    value instanceof FabricItemType item
                            && item.item() == Items.CREEPER_BANNER_PATTERN,
                    Component.literal("Expected creeper banner pattern item but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    // ── Lane C: ExprEntitySound ──

    @GameTest
    public void exprEntitySoundReturnsDeathSoundOfCow(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            testEntity = cow;

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/death_sound_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for death sound but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::death_sound", null, false);
            helper.assertTrue(
                    value instanceof String sound && sound.contains("cow"),
                    Component.literal("Expected death sound containing 'cow' but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testEntity = null;
        });
    }

    @GameTest
    public void exprEntitySoundReturnsAmbientSoundOfCow(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Cow cow = (Cow) helper.spawnWithNoFreeWill(EntityType.COW, 0.5F, 1.0F, 0.5F);
            testEntity = cow;

            runtime.loadFromResource("skript/gametest/expression/properties-and-sound/ambient_sound_records_variable.sk");
            int executed = dispatch(runtime, helper, new PropertiesAndSoundHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for ambient sound but got " + executed + ".")
            );

            Object value = Variables.getVariable("props::ambient_sound", null, false);
            helper.assertTrue(
                    value instanceof String sound && sound.equals("entity.cow.ambient"),
                    Component.literal("Expected 'entity.cow.ambient' but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testEntity = null;
        });
    }

    // ── Support types ──

    private record PropertiesAndSoundHandle() {
    }

    public static final class GameTestPropertiesAndSoundEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof PropertiesAndSoundHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{PropertiesAndSoundHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest properties and sound context";
        }
    }

    public static final class PropSoundPlayerExpression extends ch.njol.skript.lang.util.SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return testPlayer == null ? null : new ServerPlayer[]{testPlayer};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }
    }

    public static final class PropSoundEntityExpression extends ch.njol.skript.lang.util.SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return testEntity == null ? null : new Entity[]{testEntity};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
        }
    }

    public static final class PropSoundItemExpression extends ch.njol.skript.lang.util.SimpleExpression<ItemStack> {
        @Override
        protected ItemStack @Nullable [] get(SkriptEvent event) {
            return testItem == null ? null : new ItemStack[]{testItem};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ItemStack> getReturnType() {
            return ItemStack.class;
        }
    }

    public static final class PropSoundLocationExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return testLocation == null ? null : new FabricLocation[]{testLocation};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricLocation> getReturnType() {
            return FabricLocation.class;
        }
    }
}
