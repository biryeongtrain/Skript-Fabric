package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.ExecutionIntent;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.LogEntry;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import com.mojang.authlib.GameProfile;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Brightness;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemTypeClassInfo;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.bukkit.base.types.NameableClassInfo;
import org.skriptlang.skript.bukkit.base.types.OfflinePlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.SlotClassInfo;
import org.skriptlang.skript.bukkit.base.types.VectorClassInfo;
import org.skriptlang.skript.bukkit.damagesource.elements.CondScalesWithDifficulty;
import org.skriptlang.skript.bukkit.damagesource.elements.CondWasIndirect;
import org.skriptlang.skript.bukkit.displays.text.CondTextDisplayHasDropShadow;
import org.skriptlang.skript.bukkit.brewing.elements.CondBrewingConsume;
import org.skriptlang.skript.bukkit.fishing.elements.CondFishingLure;
import org.skriptlang.skript.bukkit.fishing.elements.CondIsInOpenWater;
import org.skriptlang.skript.bukkit.input.InputKey;
import org.skriptlang.skript.bukkit.input.elements.conditions.CondIsPressingKey;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDamage;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompDispensable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompInteract;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompShearable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.CondEquipCompSwapEquipment;
import org.skriptlang.skript.bukkit.interactions.elements.conditions.CondIsResponsive;
import org.skriptlang.skript.bukkit.loottables.LootTable;
import org.skriptlang.skript.bukkit.loottables.LootTableUtils;
import org.skriptlang.skript.bukkit.loottables.elements.conditions.CondHasLootTable;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanAge;
import org.skriptlang.skript.bukkit.breeding.elements.CondCanBreed;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsAdult;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsBaby;
import org.skriptlang.skript.bukkit.breeding.elements.CondIsInLove;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondHasPotion;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPoisoned;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionAmbient;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondIsPotionInstant;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasIcon;
import org.skriptlang.skript.bukkit.potion.elements.conditions.CondPotionHasParticles;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.bukkit.tags.elements.CondIsTagged;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricBreedingState;
import org.skriptlang.skript.fabric.compat.FabricFishingState;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.compat.MinecraftResourceParser;
import org.skriptlang.skript.fabric.compat.PrivateBlockEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateEntityAccess;
import org.skriptlang.skript.fabric.compat.PrivateFishingHookAccess;
import org.skriptlang.skript.fabric.compat.PrivateFurnaceAccess;
import org.skriptlang.skript.fabric.runtime.FabricAttackEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricBrewingFuelHandle;
import org.skriptlang.skript.fabric.runtime.FabricDamageHandle;
import org.skriptlang.skript.fabric.runtime.FabricFishingEventHandle;
import org.skriptlang.skript.fabric.runtime.FabricPotionEffectCause;
import org.skriptlang.skript.fabric.runtime.FabricFishingHandle;
import org.skriptlang.skript.fabric.runtime.FabricPlayerInputHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseItemHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import ch.njol.util.Kleenean;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.function.Consumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SkriptFabricBaseGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean BUILT_IN_SET_HINT_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean FAILED_EFFECT_FALLBACK_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean MATERIAL_ALIAS_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean TIED_PARSE_ERROR_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean QUALITY_PRIORITY_PARSE_ERROR_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean UNREACHABLE_TEST_STATEMENT_REGISTERED = new AtomicBoolean(false);
    private static final AtomicBoolean STATEMENT_FALLBACK_SECTION_HINT_TEST_SYNTAX_REGISTERED = new AtomicBoolean(false);
    private static Integer lastBuiltInHintedInteger;
    private static String lastBuiltInHintedText;
    private static Integer lastStatementFallbackHintedInteger;
    private static String lastCapturedMaterialAlias;

    @GameTest
    public void executesRealSkriptFile(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingMappedLocationType(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/set_test_block_at_location.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingOptionsReplacement(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/options_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingCommentAwareLoaderParsing(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/comment_aware_loader_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingCaseInsensitiveVariables(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/mixed_case_variable_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingPrefixedVariables(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/prefixed_variable_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingVariableNameExpressionsWithInnerListMarkers(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/variable_name_expression_inner_list_marker_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingBuiltInSetLocalHints(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureBuiltInSetHintTestSyntaxRegistered();

            lastBuiltInHintedInteger = null;
            lastBuiltInHintedText = null;

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/built_in_set_local_hint_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().noneMatch(message ->
                                message.contains("capture hinted integer {_value}")
                                        || message.contains("capture hinted text {_value}")
                        ),
                        Component.literal("Expected the real .sk load path to accept typed later lines after built-in local-variable sets.")
                );
            }

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertTrue(
                    Integer.valueOf(1).equals(lastBuiltInHintedInteger),
                    Component.literal("Expected built-in `set {_value} to 1` to hint later %integer% syntax during load.")
            );
            helper.assertTrue(
                    "gold_block".equals(lastBuiltInHintedText),
                    Component.literal("Expected the later built-in string set to override the local hint for %string% syntax.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingStatementFallbackSectionHints(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureStatementFallbackSectionHintTestSyntaxRegistered();

            lastStatementFallbackHintedInteger = null;

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/statement_fallback_section_hint_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().noneMatch(message ->
                                message.contains("capture statement fallback hinted integer {_value}")
                        ),
                        Component.literal("Expected statement-managed section hints to stay visible to later sibling lines on the real .sk load path.")
                );
            }

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertTrue(
                    Integer.valueOf(1).equals(lastStatementFallbackHintedInteger),
                    Component.literal("Expected the later sibling line to read the integer stored inside the statement-managed section body.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingClassInfoUserAliasPlaceholder(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureMaterialAliasTestSyntaxRegistered();

            lastCapturedMaterialAlias = null;

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/material_alias_placeholder_set_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().noneMatch(message -> message.contains("capture material alias stone")),
                        Component.literal("Expected the real .sk load path to accept %material% through the registered item-type alias.")
                );
            }

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertTrue(
                    "stone".equals(lastCapturedMaterialAlias),
                    Component.literal("Expected %material% to resolve through the item-type alias and capture stone.")
            );
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingConditionalChains(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/conditional_chain_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(1, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingParenthesizedConditionalChains(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/parenthesized_conditional_chain_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingParseIfConditionalChains(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/parse_if_conditional_chain_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(1, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingParseIfToSkipInvalidBodies(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/parse_if_skips_invalid_body_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(1, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingMultilineConditionalThenSections(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/multiline_conditional_then_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(1, 1, 0));
            helper.assertBlockPresent(Blocks.IRON_BLOCK, new BlockPos(2, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingImplicitConditionalSections(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/implicit_conditional_chain_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(1, 1, 0));
            helper.assertBlockPresent(Blocks.IRON_BLOCK, new BlockPos(2, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingListVariableReindexingOnSet(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/list_variable_reindex_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            net.minecraft.world.level.block.Block firstBlock = helper.getBlockState(new BlockPos(0, 1, 0)).getBlock();
            net.minecraft.world.level.block.Block secondBlock = helper.getBlockState(new BlockPos(1, 1, 0)).getBlock();
            helper.assertTrue(
                    firstBlock != secondBlock,
                    Component.literal("Expected reindexed list values to populate two distinct numeric slots.")
            );
            helper.assertTrue(
                    java.util.Set.of(Blocks.GOLD_BLOCK, Blocks.EMERALD_BLOCK).contains(firstBlock),
                    Component.literal("Expected the first numeric slot to resolve to a copied block id.")
            );
            helper.assertTrue(
                    java.util.Set.of(Blocks.GOLD_BLOCK, Blocks.EMERALD_BLOCK).contains(secondBlock),
                    Component.literal("Expected the second numeric slot to resolve to a copied block id.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingNaturalNumericListOrdering(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/list_variable_numeric_order_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(1, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingShallowListVariableCopy(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/list_variable_shallow_copy_set_test_block.sk");

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertTrue(
                    helper.getBlockState(new BlockPos(1, 1, 0)).getBlock() == Blocks.AIR,
                    Component.literal("Expected nested descendant-only source entries to stay out of shallow list copies.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingPatboxPlaceholdersForEntityNames(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/base/patbox_placeholder_entity_name_test_block.sk");

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    player
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected Patbox placeholder script to execute exactly one trigger but got " + executed)
            );
            helper.assertTrue(
                    player.getCustomName() != null,
                    Component.literal("Expected Patbox placeholder script to set a custom name on the event-player.")
            );
            helper.assertTrue(
                    player.getCustomName() != null
                            && player.getGameProfile().name().equals(player.getCustomName().getString()),
                    Component.literal("Expected %player:name% to resolve through TextPlaceholderAPI for the event-player.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingUnreachableCodeWarnings(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureUnreachableTestStatementRegistered();

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            helper.setBlock(new BlockPos(0, 1, 0), Blocks.AIR.defaultBlockState());
            helper.setBlock(new BlockPos(1, 1, 0), Blocks.AIR.defaultBlockState());

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/unreachable_code_warning_stop_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().anyMatch(message ->
                                message.contains("Unreachable code. The previous statement stops further execution.")
                        ),
                        Component.literal("Expected the real .sk load path to emit an unreachable-code warning.")
                );
            }

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.GOLD_BLOCK, new BlockPos(0, 1, 0));
            helper.assertTrue(
                    helper.getBlockState(new BlockPos(1, 1, 0)).is(Blocks.AIR),
                    Component.literal("Expected the unreachable line to never execute after the stopping statement.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileUsingStatementFallbackAfterFailedEffectParse(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureFailedEffectFallbackTestSyntaxRegistered();

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            helper.setBlock(new BlockPos(0, 1, 0), Blocks.AIR.defaultBlockState());

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/statement_fallback_after_failed_effect_set_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().noneMatch(message -> message.contains("ambiguous gametest effect rejected")),
                        Component.literal("Expected the real .sk load path to keep parsing after the failed effect candidate.")
                );
            }

            int executed = runtime.dispatch(new org.skriptlang.skript.lang.event.SkriptEvent(
                    helper,
                    helper.getLevel().getServer(),
                    helper.getLevel(),
                    null
            ));

            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected exactly one Skript trigger execution but got " + executed)
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(0, 1, 0));
            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileKeepingEarlierEqualQualityParseError(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureTiedParseErrorTestSyntaxRegistered();

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/equal_quality_parse_error_prefers_effect_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().anyMatch(message ->
                                message.contains("ambiguous gametest effect rejected")
                        ),
                        Component.literal("Expected the real .sk load path to retain the earlier effect parse error.")
                );
                helper.assertTrue(
                        logs.messages().stream().noneMatch(message ->
                                message.contains("ambiguous gametest condition rejected")
                        ),
                        Component.literal("Expected the later equal-quality condition error to stay suppressed.")
                );
            }

            runtime.clearScripts();
        });
    }

    @GameTest
    public void executesRealSkriptFileKeepingHigherQualityEarlierParseError(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ensureHigherQualityParseErrorTestSyntaxRegistered();

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();

            try (TestLogAppender logs = TestLogAppender.attach()) {
                runtime.loadFromResource("skript/gametest/base/higher_quality_parse_error_prefers_effect_test_block.sk");

                helper.assertTrue(
                        logs.messages().stream().anyMatch(message ->
                                message.contains("higher-quality gametest effect rejected")
                        ),
                        Component.literal("Expected the real .sk load path to retain the earlier higher-quality effect parse error.")
                );
                helper.assertTrue(
                        logs.messages().stream().noneMatch(message ->
                                message.contains("lower-quality gametest statement rejected")
                        ),
                        Component.literal("Expected the later lower-quality statement error to stay suppressed.")
                );
                helper.assertTrue(
                        logs.messages().stream().noneMatch(message ->
                                message.contains("Can't understand this condition/effect: ambiguous loader quality syntax")
                        ),
                        Component.literal("Expected the generic loader fallback to stay suppressed.")
                );
            }

            runtime.clearScripts();
        });
    }

    @GameTest
    public void coreMappingsExposeMojangBackedTypes(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.GOLD_BLOCK.defaultBlockState());

        helper.assertTrue(
                Classes.getSuperClassInfo(FabricLocation.class).getPropertyInfo(Property.WXYZ) != null,
                Component.literal("FabricLocation should register the WXYZ property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(ItemStack.class).getPropertyInfo(Property.AMOUNT) != null,
                Component.literal("ItemStack should register the amount property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(FabricInventory.class).getPropertyInfo(Property.CONTAINS) != null,
                Component.literal("FabricInventory should register the contains property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(FabricItemType.class).getPropertyInfo(Property.AMOUNT) != null,
                Component.literal("FabricItemType should register the amount property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(Slot.class).getPropertyInfo(Property.IS_EMPTY) != null,
                Component.literal("Slot should register the empty property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(GameProfile.class).getPropertyInfo(Property.NAME) != null,
                Component.literal("Offline player mapping should register the name property.")
        );
        helper.assertTrue(
                Classes.getSuperClassInfo(DamageSource.class).getPropertyInfo(Property.NAME) != null,
                Component.literal("DamageSource should register the name property.")
        );

        FabricLocation location = new FabricLocation(helper.getLevel(), new Vec3(1.5, 2.0, 3.5));
        LocationClassInfo.LocationWXYZHandler locationX = new LocationClassInfo.LocationWXYZHandler();
        locationX.axis(WXYZHandler.Axis.X);
        helper.assertTrue(
                Double.compare(locationX.convert(location), 1.5D) == 0,
                Component.literal("Location X handler should expose Mojang Vec3 coordinates.")
        );

        VectorClassInfo.VectorWXYZHandler vectorZ = new VectorClassInfo.VectorWXYZHandler();
        vectorZ.axis(WXYZHandler.Axis.Z);
        helper.assertTrue(
                Double.compare(vectorZ.convert(new Vec3(4.0, 5.0, 6.25)), 6.25D) == 0,
                Component.literal("Vector Z handler should expose Mojang Vec3 coordinates.")
        );

        ItemStack stack = new ItemStack(Items.DIAMOND, 5);
        ItemStackClassInfo.ItemStackAmountHandler amountHandler = new ItemStackClassInfo.ItemStackAmountHandler();
        helper.assertTrue(
                Integer.valueOf(5).equals(amountHandler.convert(stack)),
                Component.literal("ItemStack amount handler should expose Mojang ItemStack counts.")
        );
        amountHandler.change(stack, new Object[]{2}, ChangeMode.SET);
        helper.assertTrue(
                stack.getCount() == 2,
                Component.literal("ItemStack amount handler should mutate Mojang ItemStack counts.")
        );

        FabricInventory inventory = new FabricInventory(new SimpleContainer(stack.copy()));
        InventoryClassInfo.InventoryContainsHandler containsHandler = new InventoryClassInfo.InventoryContainsHandler();
        helper.assertTrue(
                containsHandler.contains(inventory, new ItemStack(Items.DIAMOND, 1)),
                Component.literal("FabricInventory should match Mojang container contents.")
        );

        FabricBlock block = new FabricBlock(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
        helper.assertTrue(
                block.block() == Blocks.GOLD_BLOCK,
                Component.literal("FabricBlock should expose Mojang block state at a world position.")
        );

        helper.succeed();
    }

    @GameTest
    public void baseTypeParsersAndWrappersWork(GameTestHelper helper) {
        FabricLocation parsedLocation = Classes.parse("0, 1, 2.5", FabricLocation.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedLocation != null && parsedLocation.level() == null && parsedLocation.position().z == 2.5D,
                Component.literal("Location parser should resolve coordinate literals into FabricLocation wrappers.")
        );

        FabricItemType parsedItemType = Classes.parse("3 diamond", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedItemType != null && parsedItemType.amount() == 3 && parsedItemType.item() == Items.DIAMOND,
                Component.literal("Item type parser should default bare item ids to the minecraft namespace.")
        );
        if (parsedItemType == null) {
            throw new IllegalStateException("Parsed item type was null");
        }
        helper.assertTrue(
                "diamond".equals(parsedItemType.itemId()),
                Component.literal("Minecraft item ids should stringify without the default namespace.")
        );

        FabricItemType naturalItemType = Classes.parse("2 blaze powder", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                naturalItemType != null && naturalItemType.amount() == 2 && naturalItemType.item() == Items.BLAZE_POWDER,
                Component.literal("Item type parser should resolve natural item names through registry id candidates.")
        );

        FabricItemType explicitNamespacedItemType = Classes.parse("2 minecraft:apple", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                explicitNamespacedItemType != null
                        && explicitNamespacedItemType.amount() == 2
                        && explicitNamespacedItemType.item() == Items.APPLE,
                Component.literal("Explicit namespaces should still be preserved during parsing.")
        );

        LootTable parsedLootTable = Classes.parse("chests/simple_dungeon", LootTable.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedLootTable != null && BuiltInLootTables.SIMPLE_DUNGEON.equals(parsedLootTable.key()),
                Component.literal("Loot table parser should default bare loot table ids to the minecraft namespace.")
        );
        if (parsedLootTable == null) {
            throw new IllegalStateException("Parsed loot table was null");
        }
        helper.assertTrue(
                "chests/simple_dungeon".equals(parsedLootTable.toString()),
                Component.literal("Minecraft loot table ids should stringify without the default namespace.")
        );

        Holder<net.minecraft.world.effect.MobEffect> parsedPotionType = PotionEffectSupport.parsePotionType("poison");
        helper.assertTrue(
                parsedPotionType != null && parsedPotionType.is(MobEffects.POISON),
                Component.literal("Potion type parser should default bare effect ids to the minecraft namespace.")
        );
        if (parsedPotionType == null) {
            throw new IllegalStateException("Parsed potion type was null");
        }
        helper.assertTrue(
                "poison".equals(PotionEffectSupport.effectId(parsedPotionType)),
                Component.literal("Minecraft potion effect ids should stringify without the default namespace.")
        );

        Holder<net.minecraft.world.effect.MobEffect> explicitNamespacedPotionType =
                PotionEffectSupport.parsePotionType("minecraft:poison");
        helper.assertTrue(
                explicitNamespacedPotionType != null && explicitNamespacedPotionType.is(MobEffects.POISON),
                Component.literal("Explicit namespaces should still be preserved during potion effect parsing.")
        );

        Holder<net.minecraft.world.effect.MobEffect> potionNameType = PotionEffectSupport.parsePotionType("speed potion");
        helper.assertTrue(
                potionNameType != null && potionNameType.is(MobEffects.SPEED),
                Component.literal("Potion type parser should resolve natural potion names through registry id candidates.")
        );

        Holder<net.minecraft.world.effect.MobEffect> amplifiedPotionNameType = PotionEffectSupport.parsePotionType("speed 2 potion");
        helper.assertTrue(
                amplifiedPotionNameType != null && amplifiedPotionNameType.is(MobEffects.SPEED),
                Component.literal("Potion type parser should ignore trailing amplifier suffixes for type filters.")
        );

        ItemTypeClassInfo.ItemTypeNameHandler itemTypeNameHandler = new ItemTypeClassInfo.ItemTypeNameHandler();
        itemTypeNameHandler.change(parsedItemType, new Object[]{"cut gem"}, ChangeMode.SET);
        helper.assertTrue(
                "cut gem".equals(itemTypeNameHandler.convert(parsedItemType)),
                Component.literal("Item type display name handler should mutate adapter state.")
        );

        GameProfile offline = Classes.parse("Notch", GameProfile.class, ParseContext.CONFIG);
        helper.assertTrue(
                offline != null && "Notch".equals(offline.name()),
                Component.literal("Offline player parser should resolve named GameProfiles.")
        );
        if (offline == null) {
            throw new IllegalStateException("Parsed offline player was null");
        }

        ArmorStand armorStand = new ArmorStand(helper.getLevel(), 2.0, 1.0, 2.0);
        helper.getLevel().addFreshEntity(armorStand);
        NameableClassInfo.NameableDisplayNameHandler nameableHandler = new NameableClassInfo.NameableDisplayNameHandler();
        nameableHandler.change(armorStand, new Object[]{"Target Dummy"}, ChangeMode.SET);
        helper.assertTrue(
                armorStand.getCustomName() != null && "Target Dummy".equals(armorStand.getCustomName().getString()),
                Component.literal("Nameable display name handler should mutate Mojang entities.")
        );

        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, new ItemStack(Items.APPLE, 4));
        Slot slot = new Slot(container, 0, 0, 0);

        SlotClassInfo.SlotAmountHandler slotAmountHandler = new SlotClassInfo.SlotAmountHandler();
        helper.assertTrue(
                Integer.valueOf(4).equals(slotAmountHandler.convert(slot)),
                Component.literal("Slot amount handler should read Mojang slot counts.")
        );
        slotAmountHandler.change(slot, new Object[]{2}, ChangeMode.SET);
        helper.assertTrue(
                slot.getItem().getCount() == 2,
                Component.literal("Slot amount handler should mutate Mojang slot counts.")
        );

        SlotClassInfo.SlotNameHandler slotNameHandler = new SlotClassInfo.SlotNameHandler();
        slotNameHandler.change(slot, new Object[]{"fresh apple"}, ChangeMode.SET);
        helper.assertTrue(
                "fresh apple".equals(slotNameHandler.convert(slot)),
                Component.literal("Slot name handler should mutate Mojang item custom names.")
        );

        OfflinePlayerClassInfo.OfflinePlayerNameHandler offlinePlayerNameHandler = new OfflinePlayerClassInfo.OfflinePlayerNameHandler();
        helper.assertTrue(
                "Notch".equals(offlinePlayerNameHandler.convert(offline)),
                Component.literal("Offline player name handler should expose GameProfile names.")
        );

        helper.succeed();
    }

    private static void ensureFailedEffectFallbackTestSyntaxRegistered() {
        if (FAILED_EFFECT_FALLBACK_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerEffect(RejectingGameTestEffect.class, "ambiguous loader syntax");
            Skript.registerStatement(LoadsAfterFailedEffectStatement.class, "ambiguous loader syntax");
        }
    }

    private static void ensureBuiltInSetHintTestSyntaxRegistered() {
        if (BUILT_IN_SET_HINT_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerEffect(CaptureHintedIntegerGameTestEffect.class, "capture hinted integer %number%");
            Skript.registerEffect(CaptureHintedTextGameTestEffect.class, "capture hinted text %string%");
        }
    }

    private static void ensureMaterialAliasTestSyntaxRegistered() {
        if (MATERIAL_ALIAS_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerEffect(CaptureMaterialAliasEffect.class, "capture material alias %material%");
        }
    }

    private static void ensureTiedParseErrorTestSyntaxRegistered() {
        if (TIED_PARSE_ERROR_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerEffect(RejectingGameTestEffect.class, "ambiguous loader tie syntax");
            Skript.registerCondition(RejectingGameTestCondition.class, "ambiguous loader tie syntax");
        }
    }

    private static void ensureHigherQualityParseErrorTestSyntaxRegistered() {
        if (QUALITY_PRIORITY_PARSE_ERROR_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerEffect(HigherQualityRejectingGameTestEffect.class, "ambiguous loader quality syntax");
            Skript.registerStatement(LowerQualityRejectingGameTestStatement.class, "ambiguous loader quality syntax");
        }
    }

    private static void ensureUnreachableTestStatementRegistered() {
        if (UNREACHABLE_TEST_STATEMENT_REGISTERED.compareAndSet(false, true)) {
            Skript.registerStatement(StopGameTestTriggerStatement.class, "stop gametest trigger");
        }
    }

    private static void ensureStatementFallbackSectionHintTestSyntaxRegistered() {
        if (STATEMENT_FALLBACK_SECTION_HINT_TEST_SYNTAX_REGISTERED.compareAndSet(false, true)) {
            Skript.registerExpression(
                    StatementManagedHintGameTestExpression.class,
                    Number.class,
                    "statement hint value"
            );
            Skript.registerStatement(
                    StatementManagedHintGameTestStatement.class,
                    "statement-managed hint %number%"
            );
            Skript.registerEffect(
                    CaptureStatementFallbackHintedIntegerGameTestEffect.class,
                    "capture statement fallback hinted integer %number%"
            );
        }
    }

    public static final class RejectingGameTestEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("ambiguous gametest effect rejected");
            return false;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "ambiguous gametest effect";
        }
    }

    public static final class CaptureHintedIntegerGameTestEffect extends ch.njol.skript.lang.Effect {

        private Expression<Number> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            value = (Expression<Number>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            Number captured = value.getSingle(event);
            lastBuiltInHintedInteger = captured == null ? null : captured.intValue();
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "capture hinted integer";
        }
    }

    public static final class CaptureHintedTextGameTestEffect extends ch.njol.skript.lang.Effect {

        private Expression<String> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            value = (Expression<String>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            lastBuiltInHintedText = value.getSingle(event);
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "capture hinted text";
        }
    }

    public static final class CaptureMaterialAliasEffect extends ch.njol.skript.lang.Effect {

        private Expression<FabricItemType> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            if (!FabricItemType.class.isAssignableFrom(expressions[0].getReturnType())) {
                Skript.error("Expected %material% to resolve to an item type.");
                return false;
            }
            value = (Expression<FabricItemType>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            FabricItemType itemType = value.getSingle(event);
            lastCapturedMaterialAlias = itemType == null ? null : itemType.itemId();
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "capture material alias";
        }
    }

    public static final class RejectingGameTestCondition extends Condition {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            Skript.error("ambiguous gametest condition rejected");
            return false;
        }

        @Override
        public boolean check(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "ambiguous gametest condition";
        }
    }

    public static final class HigherQualityRejectingGameTestEffect extends ch.njol.skript.lang.Effect {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            SkriptLogger.log(new LogEntry(
                    Level.SEVERE,
                    ErrorQuality.NOT_AN_EXPRESSION,
                    "higher-quality gametest effect rejected"
            ));
            return false;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "higher-quality gametest effect";
        }
    }

    public static final class LowerQualityRejectingGameTestStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            SkriptLogger.log(new LogEntry(
                    Level.SEVERE,
                    ErrorQuality.GENERIC,
                    "lower-quality gametest statement rejected"
            ));
            return false;
        }

        @Override
        protected boolean run(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return true;
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "lower-quality gametest statement";
        }
    }

    public static final class LoadsAfterFailedEffectStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        protected boolean run(org.skriptlang.skript.lang.event.SkriptEvent event) {
            if (event.handle() instanceof GameTestHelper helper) {
                helper.setBlock(new BlockPos(0, 1, 0), Blocks.EMERALD_BLOCK.defaultBlockState());
            } else if (event.level() != null) {
                event.level().setBlockAndUpdate(new BlockPos(0, 1, 0), Blocks.EMERALD_BLOCK.defaultBlockState());
            }
            return true;
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "ambiguous loader syntax";
        }
    }

    public static final class StopGameTestTriggerStatement extends Statement {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                ch.njol.skript.lang.SkriptParser.ParseResult parseResult
        ) {
            return true;
        }

        @Override
        protected boolean run(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return false;
        }

        @Override
        protected ExecutionIntent executionIntent() {
            return ExecutionIntent.stopTrigger();
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "stop gametest trigger";
        }
    }

    public static final class StatementManagedHintGameTestStatement extends Statement {

        private StatementManagedHintGameTestExpression expression;

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            expression = (StatementManagedHintGameTestExpression) expressions[0];
            return true;
        }

        @Override
        protected boolean run(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return expression.runManagedSection(event);
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "statement-managed hint";
        }
    }

    public static final class StatementManagedHintGameTestExpression extends SectionExpression<Number> {

        @Override
        public boolean init(
                Expression<?>[] expressions,
                int pattern,
                Kleenean delayed,
                SkriptParser.ParseResult parseResult,
                @org.jetbrains.annotations.Nullable SectionNode node,
                @org.jetbrains.annotations.Nullable List<TriggerItem> triggerItems
        ) {
            ParserInstance.get().getHintManager().set("value", Number.class);
            if (node != null) {
                loadOptionalCode(node);
            }
            return true;
        }

        @Override
        protected Number @org.jetbrains.annotations.Nullable [] get(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return new Number[]{1L};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Number> getReturnType() {
            return Number.class;
        }

        public boolean runManagedSection(org.skriptlang.skript.lang.event.SkriptEvent event) {
            return runSection(event);
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "statement hint value";
        }
    }

    public static final class CaptureStatementFallbackHintedIntegerGameTestEffect extends ch.njol.skript.lang.Effect {

        private Expression<Number> value;

        @Override
        @SuppressWarnings("unchecked")
        public boolean init(
                Expression<?>[] expressions,
                int matchedPattern,
                Kleenean isDelayed,
                SkriptParser.ParseResult parseResult
        ) {
            value = (Expression<Number>) expressions[0];
            return true;
        }

        @Override
        protected void execute(org.skriptlang.skript.lang.event.SkriptEvent event) {
            Number captured = value.getSingle(event);
            lastStatementFallbackHintedInteger = captured == null ? null : captured.intValue();
        }

        @Override
        public String toString(org.skriptlang.skript.lang.event.SkriptEvent event, boolean debug) {
            return "capture statement fallback hinted integer";
        }
    }

    private static final class TestLogAppender extends AbstractAppender implements AutoCloseable {

        private final List<String> messages = new ArrayList<>();
        private final Logger logger;

        private TestLogAppender(Logger logger) {
            super("base-gametest-loader-warning", null, PatternLayout.createDefaultLayout(), false, null);
            this.logger = logger;
        }

        static TestLogAppender attach() {
            Logger logger = (Logger) LogManager.getLogger("skfabric");
            TestLogAppender appender = new TestLogAppender(logger);
            appender.start();
            logger.addAppender(appender);
            return appender;
        }

        List<String> messages() {
            return messages;
        }

        @Override
        public void append(LogEvent event) {
            messages.add(event.getMessage().getFormattedMessage());
        }

        @Override
        public void close() {
            logger.removeAppender((Appender) this);
            stop();
        }
    }
}
