package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.DyeColorMapping;
import ch.njol.skript.variables.Variables;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionColorAndBannerGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    private static volatile @Nullable Sheep testSheep;
    private static volatile @Nullable FabricBlock testBlock;
    private static volatile @Nullable ItemStack testItem;
    private static volatile @Nullable Color testColor;

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestColorAndBannerEvent.class, "gametest color and banner context");
        Skript.registerExpression(ColorBannerSheepExpression.class, Sheep.class, "colorbanner-sheep");
        Skript.registerExpression(ColorBannerBlockExpression.class, FabricBlock.class, "colorbanner-block");
        Skript.registerExpression(ColorBannerItemExpression.class, ItemStack.class, "colorbanner-item");
        Skript.registerExpression(ColorBannerColorExpression.class, Color.class, "colorbanner-color");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    // ── Lane A: DyeColorMapping ──

    @GameTest
    public void dyeColorToColorRgbRoundTripForAllSixteenColors(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            for (DyeColor dye : DyeColor.values()) {
                ColorRGB color = DyeColorMapping.toColor(dye);
                helper.assertTrue(
                        color != null,
                        Component.literal("DyeColorMapping.toColor(" + dye.name() + ") returned null.")
                );
                DyeColor back = DyeColorMapping.toDyeColorExact(color);
                helper.assertTrue(
                        back == dye,
                        Component.literal("DyeColorMapping exact round-trip failed for " + dye.name()
                                + ": got " + back + " from " + color + ".")
                );
            }
        });
    }

    @GameTest
    public void pureRedAndWhiteMapToNearestDyeColor(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            // Pure red (255,0,0) should map to RED
            DyeColor nearest = DyeColorMapping.toDyeColor(new ColorRGB(255, 0, 0));
            helper.assertTrue(
                    nearest == DyeColor.RED,
                    Component.literal("Expected pure red to map to RED but got " + nearest + ".")
            );
            // Pure white (255,255,255) should map to WHITE
            DyeColor nearestWhite = DyeColorMapping.toDyeColor(new ColorRGB(255, 255, 255));
            helper.assertTrue(
                    nearestWhite == DyeColor.WHITE,
                    Component.literal("Expected pure white to map to WHITE but got " + nearestWhite + ".")
            );
        });
    }

    // ── Lane B: ExprNewBannerPattern ──

    @GameTest
    public void exprNewBannerPatternCreatesRedCreeperLayer(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/color-and-banner/new_banner_pattern_records_variable.sk");

            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for new banner pattern but got " + executed + ".")
            );

            Object value = Variables.getVariable("colorbanner::pattern", null, false);
            helper.assertTrue(
                    value instanceof BannerPatternLayers.Layer layer && layer.color() == DyeColor.RED,
                    Component.literal("Expected banner pattern layer with RED color but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    // ── Lane C: ExprBannerPatterns ──

    @GameTest
    public void exprBannerPatternsReturnEmptyForFreshBanner(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            // Place a banner block
            BlockPos bannerPos = helper.absolutePos(new BlockPos(1, 1, 0));
            helper.getLevel().setBlockAndUpdate(bannerPos, Blocks.WHITE_BANNER.defaultBlockState());
            testBlock = new FabricBlock(helper.getLevel(), bannerPos);

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/banner_patterns_records_variable.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for banner patterns get but got " + executed + ".")
            );

            // A freshly placed banner has 0 patterns
            Map<String, Object> patterns = Variables.getVariablesWithPrefix("colorbanner::patterns::", null, false);
            helper.assertTrue(
                    patterns.isEmpty(),
                    Component.literal("Expected 0 patterns on fresh banner but got " + patterns.size() + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testBlock = null;
        });
    }

    @GameTest
    public void addRedCreeperPatternToBannerBlockViaSkript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            BlockPos bannerPos = helper.absolutePos(new BlockPos(2, 1, 0));
            helper.getLevel().setBlockAndUpdate(bannerPos, Blocks.WHITE_BANNER.defaultBlockState());
            testBlock = new FabricBlock(helper.getLevel(), bannerPos);

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/add_banner_pattern_to_block.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for banner pattern add but got " + executed + ".")
            );

            // Verify the banner now has 1 pattern
            BannerBlockEntity banner = (BannerBlockEntity) helper.getLevel().getBlockEntity(bannerPos);
            helper.assertTrue(
                    banner != null && banner.getPatterns().layers().size() == 1,
                    Component.literal("Expected 1 pattern on banner after add but got "
                            + (banner != null ? banner.getPatterns().layers().size() : "null") + ".")
            );
            helper.assertTrue(
                    banner.getPatterns().layers().getFirst().color() == DyeColor.RED,
                    Component.literal("Expected added pattern to have RED color.")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testBlock = null;
        });
    }

    // ── Lane D: ExprColorOf ──

    @GameTest
    public void exprColorOfReadsBlueSheepWoolColor(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Sheep sheep = (Sheep) helper.spawnWithNoFreeWill(EntityType.SHEEP, 0.5F, 1.0F, 0.5F);
            sheep.setColor(DyeColor.BLUE);
            testSheep = sheep;

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/color_of_sheep_records_variable.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for color of sheep but got " + executed + ".")
            );

            Object value = Variables.getVariable("colorbanner::sheep_color", null, false);
            helper.assertTrue(
                    value instanceof Color color && color.rgb() == DyeColorMapping.toColor(DyeColor.BLUE).rgb(),
                    Component.literal("Expected sheep color to match BLUE dye color but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testSheep = null;
        });
    }

    @GameTest
    public void setColorOfSheepChangesWoolFromWhiteToRed(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Sheep sheep = (Sheep) helper.spawnWithNoFreeWill(EntityType.SHEEP, 0.5F, 1.0F, 0.5F);
            sheep.setColor(DyeColor.WHITE);
            testSheep = sheep;
            testColor = DyeColorMapping.toColor(DyeColor.RED);

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/set_color_of_sheep.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for set color of sheep but got " + executed + ".")
            );

            helper.assertTrue(
                    sheep.getColor() == DyeColor.RED,
                    Component.literal("Expected sheep color to be RED after set but got " + sheep.getColor() + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testSheep = null;
            testColor = null;
        });
    }

    @GameTest
    public void exprColorOfReadsRedDyedLeatherChestplate(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            ItemStack leatherChestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
            leatherChestplate.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFF0000));
            testItem = leatherChestplate;

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/color_of_item_records_variable.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for color of item but got " + executed + ".")
            );

            Object value = Variables.getVariable("colorbanner::item_color", null, false);
            helper.assertTrue(
                    value instanceof Color color && color.red() == 255 && color.green() == 0 && color.blue() == 0,
                    Component.literal("Expected item color to be pure red but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testItem = null;
        });
    }

    @GameTest
    public void setColorOfLeatherChestplateDyesItGreen(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            ItemStack leatherChestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
            testItem = leatherChestplate;
            testColor = new ColorRGB(0, 255, 0);

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/set_color_of_item.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for set color of item but got " + executed + ".")
            );

            DyedItemColor dyed = testItem.get(DataComponents.DYED_COLOR);
            helper.assertTrue(
                    dyed != null && dyed.rgb() == 0x00FF00,
                    Component.literal("Expected item to be dyed green but got "
                            + (dyed != null ? Integer.toHexString(dyed.rgb()) : "null") + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testItem = null;
            testColor = null;
        });
    }

    @GameTest
    public void exprColorOfReadsRedBannerBlockBaseColor(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            BlockPos bannerPos = helper.absolutePos(new BlockPos(3, 1, 0));
            helper.getLevel().setBlockAndUpdate(bannerPos, Blocks.RED_BANNER.defaultBlockState());
            testBlock = new FabricBlock(helper.getLevel(), bannerPos);

            runtime.loadFromResource("skript/gametest/expression/color-and-banner/color_of_banner_records_variable.sk");
            int executed = dispatch(runtime, helper, new ColorAndBannerHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for color of banner but got " + executed + ".")
            );

            Object value = Variables.getVariable("colorbanner::banner_color", null, false);
            helper.assertTrue(
                    value instanceof Color color && color.rgb() == DyeColorMapping.toColor(DyeColor.RED).rgb(),
                    Component.literal("Expected banner color to match RED dye color but got " + value + ".")
            );
            runtime.clearScripts();
            Variables.clearAll();
            testBlock = null;
        });
    }

    // ── Support types ──

    private record ColorAndBannerHandle() {
    }

    public static final class GameTestColorAndBannerEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ColorAndBannerHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ColorAndBannerHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest color and banner context";
        }
    }

    public static final class ColorBannerSheepExpression extends ch.njol.skript.lang.util.SimpleExpression<Sheep> {
        @Override
        protected Sheep @Nullable [] get(SkriptEvent event) {
            return testSheep == null ? null : new Sheep[]{testSheep};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Sheep> getReturnType() {
            return Sheep.class;
        }
    }

    public static final class ColorBannerBlockExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return testBlock == null ? null : new FabricBlock[]{testBlock};
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

    public static final class ColorBannerItemExpression extends ch.njol.skript.lang.util.SimpleExpression<ItemStack> {
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

    public static final class ColorBannerColorExpression extends ch.njol.skript.lang.util.SimpleExpression<Color> {
        @Override
        protected Color @Nullable [] get(SkriptEvent event) {
            return testColor == null ? null : new Color[]{testColor};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Color> getReturnType() {
            return Color.class;
        }
    }
}
