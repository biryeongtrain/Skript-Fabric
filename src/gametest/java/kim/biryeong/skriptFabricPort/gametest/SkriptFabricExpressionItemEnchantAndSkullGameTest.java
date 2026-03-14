package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.variables.Variables;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionItemEnchantAndSkullGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    private static volatile @Nullable FabricItemType item;
    private static volatile @Nullable ItemStack itemStack;
    private static volatile @Nullable FabricItemType skullItem;
    private static volatile @Nullable Enchantment enchantment;
    private static volatile @Nullable Enchantment secondEnchantment;
    private static volatile @Nullable GameProfile nextOwner;

    @GameTest
    public void safe5ItemExpressionsExecuteRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            Enchantment sharpness = enchantment(helper, Enchantments.SHARPNESS);
            Enchantment unbreaking = enchantment(helper, Enchantments.UNBREAKING);

            ItemStack swordStack = new ItemStack(Items.DIAMOND_SWORD);
            swordStack.enchant(Holder.direct(sharpness), 2);
            item = new FabricItemType(swordStack);
            itemStack = swordStack.copy();

            GameProfile startingOwner = new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000f5"), "safe5-a");
            ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
            headStack.set(DataComponents.PROFILE, new ResolvableProfile(startingOwner));
            skullItem = new FabricItemType(headStack);

            enchantment = sharpness;
            secondEnchantment = unbreaking;
            nextOwner = new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000f6"), "safe5-b");

            runtime.loadFromResource("skript/gametest/expression/item-enchant-and-skull/item_expressions_execute.sk");

            int executed = dispatch(runtime, helper, new ItemEnchantAndSkullHandle());
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for safe5 item expressions but got " + executed + ".")
            );
            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, new BlockPos(4, 1, 0));

            assertNumber(helper, "itemenchant::level_before", 2.0D);
            assertNumber(helper, "itemenchant::level_after", 3.0D);
            helper.assertTrue(
                    Variables.getVariable("itemenchant::type", null, false) instanceof FabricItemType type
                            && type.item() == Items.DIAMOND_SWORD,
                    Component.literal("Expected type of itemenchant-itemstack to resolve to a diamond sword item type.")
            );
            helper.assertTrue(
                    Variables.getVariable("itemenchant::owner_before", null, false) instanceof GameProfile ownerBefore
                            && startingOwner.equals(ownerBefore),
                    Component.literal("Expected skull owner getter to expose the original owner profile.")
            );
            helper.assertTrue(
                    Variables.getVariable("itemenchant::owner_after", null, false) instanceof GameProfile ownerAfter
                            && nextOwner != null
                            && nextOwner.equals(ownerAfter),
                    Component.literal("Expected skull owner getter to expose the updated owner profile.")
            );
            helper.assertTrue(
                    item != null && levelOf(item.toStack(), sharpness) == 3 && levelOf(item.toStack(), unbreaking) == 1,
                    Component.literal("Expected safe5 item mutations to update enchantment level and set the second enchantment.")
            );
            helper.assertTrue(
                    skullItem != null
                            && skullItem.toStack().get(DataComponents.PROFILE) != null
                            && nextOwner != null
                            && nextOwner.equals(skullItem.toStack().get(DataComponents.PROFILE).gameProfile()),
                    Component.literal("Expected skull owner mutation to update the underlying player head profile.")
            );

            runtime.clearScripts();
            Variables.clearAll();
            item = null;
            itemStack = null;
            skullItem = null;
            enchantment = null;
            secondEnchantment = null;
            nextOwner = null;
        });
    }

    @GameTest
    public void safe5MeExpressionExecutesThroughRealCommandEvent(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            runtime.loadFromResource("skript/gametest/expression/item-enchant-and-skull/me_executes_in_command_event.sk");

            BlockPos markerAbsolute = helper.absolutePos(new BlockPos(6, 1, 0));
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "say safe5 live hook");

            helper.assertTrue(
                    player.getCustomName() != null && "say safe5 live hook".equals(player.getCustomName().getString()),
                    Component.literal("Expected me expression to resolve to the live command sender.")
            );
            runtime.clearScripts();
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestItemEnchantAndSkullEvent.class, "gametest item enchant and skull context");
        Skript.registerExpression(ItemEnchantItemExpression.class, FabricItemType.class, "itemenchant-item");
        Skript.registerExpression(ItemEnchantItemStackExpression.class, ItemStack.class, "itemenchant-itemstack");
        Skript.registerExpression(ItemEnchantSkullItemExpression.class, FabricItemType.class, "itemenchant-skull-item");
        Skript.registerExpression(ItemEnchantEnchantmentExpression.class, Enchantment.class, "itemenchant-enchantment");
        Skript.registerExpression(ItemEnchantSecondEnchantmentExpression.class, Enchantment.class, "itemenchant-second-enchantment");
        Skript.registerExpression(ItemEnchantNextOwnerExpression.class, GameProfile.class, "itemenchant-next-owner");
    }

    private static Enchantment enchantment(GameTestHelper helper, net.minecraft.resources.ResourceKey<Enchantment> key) {
        return helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key).value();
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper, Object handle) {
        return runtime.dispatch(new SkriptEvent(handle, helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private static int levelOf(ItemStack stack, Enchantment target) {
        for (var entry : stack.getEnchantments().entrySet()) {
            if (entry.getKey().value() == target) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    private static void assertNumber(GameTestHelper helper, String variable, double expected) {
        Object value = Variables.getVariable(variable, null, false);
        helper.assertTrue(
                value instanceof Number && Double.compare(((Number) value).doubleValue(), expected) == 0,
                Component.literal("Expected " + variable + " to equal " + expected + " but got " + value + ".")
        );
    }

    private record ItemEnchantAndSkullHandle() {
    }

    public static final class GameTestItemEnchantAndSkullEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ItemEnchantAndSkullHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ItemEnchantAndSkullHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest item enchant and skull context";
        }
    }

    public static final class ItemEnchantItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return item == null ? null : new FabricItemType[]{item};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }

    public static final class ItemEnchantItemStackExpression extends SimpleExpression<ItemStack> {
        @Override
        protected ItemStack @Nullable [] get(SkriptEvent event) {
            return itemStack == null ? null : new ItemStack[]{itemStack};
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

    public static final class ItemEnchantSkullItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return skullItem == null ? null : new FabricItemType[]{skullItem};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }

    public static final class ItemEnchantEnchantmentExpression extends SimpleExpression<Enchantment> {
        @Override
        protected Enchantment @Nullable [] get(SkriptEvent event) {
            return enchantment == null ? null : new Enchantment[]{enchantment};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Enchantment> getReturnType() {
            return Enchantment.class;
        }
    }

    public static final class ItemEnchantSecondEnchantmentExpression extends SimpleExpression<Enchantment> {
        @Override
        protected Enchantment @Nullable [] get(SkriptEvent event) {
            return secondEnchantment == null ? null : new Enchantment[]{secondEnchantment};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Enchantment> getReturnType() {
            return Enchantment.class;
        }
    }

    public static final class ItemEnchantNextOwnerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return nextOwner == null ? null : new GameProfile[]{nextOwner};
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
}
