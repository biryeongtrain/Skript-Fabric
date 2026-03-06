package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemStackClassInfo;
import org.skriptlang.skript.bukkit.base.types.ItemTypeClassInfo;
import org.skriptlang.skript.bukkit.base.types.LocationClassInfo;
import org.skriptlang.skript.bukkit.base.types.NameableClassInfo;
import org.skriptlang.skript.bukkit.base.types.OfflinePlayerClassInfo;
import org.skriptlang.skript.bukkit.base.types.SlotClassInfo;
import org.skriptlang.skript.bukkit.base.types.VectorClassInfo;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricBlockBreakHandle;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SkriptFabricGameTest {

    private static final AtomicBoolean RUNTIME_LOCK = new AtomicBoolean(false);

    @GameTest
    public void executesRealSkriptFile(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/set_test_block.sk");

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
            runtime.loadFromResource("skript/gametest/set_test_block_at_location.sk");

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
    public void fabricServerTickBridgeExecutesLoadedScript(GameTestHelper helper) {
        SkriptRuntime runtime = SkriptRuntime.instance();
        BlockPos absoluteTarget = new BlockPos(1, 80, 1);
        AtomicBoolean loaded = new AtomicBoolean(false);
        helper.succeedWhen(() -> {
            if (!loaded.get()) {
                helper.assertTrue(
                        RUNTIME_LOCK.compareAndSet(false, true),
                        Component.literal("Waiting for exclusive Skript runtime access.")
                );
                runtime.clearScripts();
                helper.getLevel().setBlockAndUpdate(absoluteTarget, Blocks.AIR.defaultBlockState());
                runtime.loadFromResource("skript/gametest/server_tick_sets_block.sk");
                loaded.set(true);
            }
            helper.assertTrue(
                    helper.getLevel().getBlockState(absoluteTarget).is(Blocks.LAPIS_BLOCK),
                    Component.literal("Expected server tick event bridge to execute loaded Skript file.")
            );
            runtime.clearScripts();
            RUNTIME_LOCK.set(false);
        });
    }

    @GameTest
    public void fabricBlockBreakBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/block_break_sets_block.sk");

            BlockPos brokenRelative = new BlockPos(0, 1, 0);
            BlockPos brokenAbsolute = helper.absolutePos(brokenRelative);

            helper.getLevel().setBlockAndUpdate(brokenAbsolute, Blocks.STONE.defaultBlockState());

            var player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(brokenAbsolute.getX() + 0.5D, brokenAbsolute.getY(), brokenAbsolute.getZ() + 0.5D);

            helper.assertTrue(
                player.gameMode.destroyBlock(brokenAbsolute),
                Component.literal("Expected mock server player to break the test block.")
            );
            helper.assertTrue(
                helper.getLevel().getBlockState(brokenAbsolute).is(Blocks.REDSTONE_BLOCK),
                Component.literal("Expected block break bridge to execute loaded Skript file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void fabricUseBlockBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/use_block_sets_blocks.sk");

            BlockPos clickedAbsolute = helper.absolutePos(new BlockPos(0, 1, 0));
            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(2, 1, 0));

            helper.getLevel().setBlockAndUpdate(clickedAbsolute, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseBlockCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(clickedAbsolute), Direction.UP, clickedAbsolute, false)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use block bridge to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(clickedAbsolute).is(Blocks.EMERALD_BLOCK),
                    Component.literal("Expected use block bridge to resolve event-block inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.GOLD_BLOCK),
                    Component.literal("Expected use block bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void useEntityBridgeExecutesLoadedScript(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/use_entity_names_entity.sk");

            BlockPos playerMarkerAbsolute = helper.absolutePos(new BlockPos(3, 1, 0));
            helper.getLevel().setBlockAndUpdate(playerMarkerAbsolute, Blocks.AIR.defaultBlockState());

            ArmorStand armorStand = new ArmorStand(helper.getLevel(), 0.5D, 1.0D, 0.5D);
            helper.getLevel().addFreshEntity(armorStand);

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(playerMarkerAbsolute.getX() + 0.5D, playerMarkerAbsolute.getY() + 1.0D, playerMarkerAbsolute.getZ() + 0.5D);

            InteractionResult result = UseEntityCallback.EVENT.invoker().interact(
                    player,
                    helper.getLevel(),
                    InteractionHand.MAIN_HAND,
                    armorStand,
                    new EntityHitResult(armorStand)
            );
            helper.assertTrue(
                    result == InteractionResult.PASS,
                    Component.literal("Expected use entity bridge to keep Fabric callback flow in PASS state.")
            );
            helper.assertTrue(
                    armorStand.getCustomName() != null && "clicked entity".equals(armorStand.getCustomName().getString()),
                    Component.literal("Expected use entity bridge to resolve event-entity inside a real .sk file.")
            );
            helper.assertTrue(
                    helper.getLevel().getBlockState(playerMarkerAbsolute).is(Blocks.AMETHYST_BLOCK),
                    Component.literal("Expected use entity bridge to resolve event-player inside a real .sk file.")
            );
            runtime.clearScripts();
        });
    }

    @GameTest
    public void eventPayloadExpressionsParseAndResolve(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 0));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());

        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.CREATIVE);

        @SuppressWarnings("unchecked")
        Expression<? extends ServerPlayer> eventPlayerExpression = new SkriptParser(
                "event-player",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{ServerPlayer.class});
        helper.assertTrue(
                eventPlayerExpression != null,
                Component.literal("Expected event-player expression to parse from registry.")
        );
        if (eventPlayerExpression == null) {
            throw new IllegalStateException("event-player expression did not parse");
        }

        ServerPlayer resolvedPlayer = eventPlayerExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                null,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedPlayer == player,
                Component.literal("event-player expression should resolve the event player.")
        );

        @SuppressWarnings("unchecked")
        Expression<? extends FabricBlock> eventBlockExpression = new SkriptParser(
                "event-block",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{FabricBlock.class});
        helper.assertTrue(
                eventBlockExpression != null,
                Component.literal("Expected event-block expression to parse from registry.")
        );
        if (eventBlockExpression == null) {
            throw new IllegalStateException("event-block expression did not parse");
        }

        FabricBlockBreakHandle handle = new FabricBlockBreakHandle(
                helper.getLevel(),
                player,
                pos,
                helper.getLevel().getBlockState(pos),
                helper.getLevel().getBlockEntity(pos)
        );
        FabricBlock resolvedBlock = eventBlockExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                handle,
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedBlock != null && resolvedBlock.position().equals(pos),
                Component.literal("event-block expression should resolve the broken block position.")
        );

        ArmorStand armorStand = new ArmorStand(helper.getLevel(), pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(armorStand);

        @SuppressWarnings("unchecked")
        Expression<? extends net.minecraft.world.entity.Entity> eventEntityExpression = new SkriptParser(
                "event-entity",
                SkriptParser.ALL_FLAGS,
                ParseContext.DEFAULT
        ).parseExpression(new Class[]{net.minecraft.world.entity.Entity.class});
        helper.assertTrue(
                eventEntityExpression != null,
                Component.literal("Expected event-entity expression to parse from registry.")
        );
        if (eventEntityExpression == null) {
            throw new IllegalStateException("event-entity expression did not parse");
        }

        net.minecraft.world.entity.Entity resolvedEntity = eventEntityExpression.getSingle(new org.skriptlang.skript.lang.event.SkriptEvent(
                new FabricUseEntityHandle(
                        helper.getLevel(),
                        player,
                        InteractionHand.MAIN_HAND,
                        armorStand,
                        new EntityHitResult(armorStand)
                ),
                helper.getLevel().getServer(),
                helper.getLevel(),
                player
        ));
        helper.assertTrue(
                resolvedEntity == armorStand,
                Component.literal("event-entity expression should resolve the interacted entity.")
        );
        helper.succeed();
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

        FabricItemType parsedItemType = Classes.parse("3 minecraft:diamond", FabricItemType.class, ParseContext.CONFIG);
        helper.assertTrue(
                parsedItemType != null && parsedItemType.amount() == 3 && parsedItemType.item() == Items.DIAMOND,
                Component.literal("Item type parser should resolve Mojang items and preserve count.")
        );
        if (parsedItemType == null) {
            throw new IllegalStateException("Parsed item type was null");
        }

        ItemTypeClassInfo.ItemTypeNameHandler itemTypeNameHandler = new ItemTypeClassInfo.ItemTypeNameHandler();
        itemTypeNameHandler.change(parsedItemType, new Object[]{"cut gem"}, ChangeMode.SET);
        helper.assertTrue(
                "cut gem".equals(itemTypeNameHandler.convert(parsedItemType)),
                Component.literal("Item type display name handler should mutate adapter state.")
        );

        GameProfile offline = Classes.parse("Notch", GameProfile.class, ParseContext.CONFIG);
        helper.assertTrue(
                offline != null && "Notch".equals(offline.getName()),
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

    private void runWithRuntimeLock(GameTestHelper helper, LockedRuntimeBody body) {
        helper.succeedWhen(() -> {
            helper.assertTrue(
                    RUNTIME_LOCK.compareAndSet(false, true),
                    Component.literal("Waiting for exclusive Skript runtime access.")
            );
            try {
                body.run();
            } finally {
                RUNTIME_LOCK.set(false);
            }
        });
    }

    @FunctionalInterface
    private interface LockedRuntimeBody {
        void run();
    }
}
