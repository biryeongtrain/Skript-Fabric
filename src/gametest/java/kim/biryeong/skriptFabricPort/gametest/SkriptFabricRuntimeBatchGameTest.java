package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.expressions.ExprChestInventory;
import ch.njol.skript.expressions.ExprCommand;
import ch.njol.skript.expressions.ExprCommandSender;
import ch.njol.skript.expressions.ExprPickupDelay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.StringMode;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.PrivateItemEntityAccess;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricRuntimeBatchGameTest extends AbstractSkriptFabricGameTestSupport {

    @GameTest
    public void functionDeclarationsExecuteAndUnload(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();

            runtime.loadFromResource("skript/gametest/base/function_declaration_records_values.sk");

            helper.assertTrue(
                    Integer.valueOf(12).equals(Variables.getVariable("function::double", null, false)),
                    Component.literal("Expected declared function result to be 12.")
            );
            helper.assertTrue(
                    "head:tail".equals(Variables.getVariable("function::local", null, false)),
                    Component.literal("Expected local declared function result to be head:tail.")
            );
            helper.assertTrue(
                    Functions.getFunction("double_runtime") != null,
                    Component.literal("Expected double_runtime function to be registered after loading.")
            );

            runtime.clearScripts();

            helper.assertTrue(
                    Functions.getFunction("double_runtime") == null,
                    Component.literal("Expected double_runtime function to be null after clearScripts.")
            );

            Variables.clearAll();
        });
    }

    @GameTest
    public void chestInventoryUsesRowsAndTitle(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ExprChestInventory inventoryExpression = new ExprChestInventory();
            inventoryExpression.init(new Expression[]{
                    new SimpleLiteral<>("Menu", false),
                    new SimpleLiteral<>(2, false)
            }, 0, Kleenean.FALSE, parseResult(""));

            FabricInventory inventory = inventoryExpression.getSingle(SkriptEvent.EMPTY);

            helper.assertTrue(
                    inventory != null,
                    Component.literal("Expected inventory expression to return a non-null inventory.")
            );
            helper.assertTrue(
                    MenuType.GENERIC_9x2.equals(inventory.menuType()),
                    Component.literal("Expected menu type to be GENERIC_9x2.")
            );
            helper.assertTrue(
                    inventory.container().getContainerSize() == 18,
                    Component.literal("Expected container size to be 18.")
            );
            helper.assertTrue(
                    "Menu".equals(inventory.title().getString()),
                    Component.literal("Expected title to be 'Menu' but was '" + inventory.title().getString() + "'.")
            );
        });
    }

    @GameTest
    public void pickupDelayExpressionReadsAndMutates(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            ItemEntity itemEntity = new ItemEntity(
                    helper.getLevel(),
                    0, 64, 0,
                    new ItemStack(Items.STICK)
            );
            PrivateItemEntityAccess.setPickupDelay(itemEntity, 20);

            ExprPickupDelay pickupDelay = new ExprPickupDelay();
            pickupDelay.init(
                    new Expression[]{new SimpleLiteral<Entity>(itemEntity, false)},
                    0, Kleenean.FALSE, parseResult("pickup delay")
            );

            Timespan current = pickupDelay.getSingle(SkriptEvent.EMPTY);
            helper.assertTrue(
                    current != null && current.getAs(TimePeriod.TICK) == 20L,
                    Component.literal("Expected pickup delay to be 20 ticks.")
            );

            pickupDelay.change(
                    SkriptEvent.EMPTY,
                    new Object[]{new Timespan(TimePeriod.TICK, 5)},
                    ChangeMode.ADD
            );

            helper.assertTrue(
                    PrivateItemEntityAccess.pickupDelay(itemEntity) == 25,
                    Component.literal("Expected pickup delay to be 25 after adding 5.")
            );
        });
    }

    @GameTest
    public void variableStringPatboxPlaceholder(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            VariableString string = VariableString.newInstance("%player:name%", StringMode.MESSAGE);

            helper.assertTrue(
                    string != null,
                    Component.literal("Expected VariableString to be created for patbox placeholder syntax.")
            );

            String result = string.toString(SkriptEvent.EMPTY);

            helper.assertTrue(
                    "%player:name%".equals(result),
                    Component.literal("Expected patbox placeholder to remain untouched but was '" + result + "'.")
            );
        });
    }

    @GameTest
    public void commandHandleBindsExpressions(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            Class<?> commandEventClass = resolveEventClass("ch.njol.skript.events.FabricPlayerEventHandles$Command");
            ParserInstance parser = ParserInstance.get();
            String previousEventName = parser.getCurrentEventName();
            Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
            try {
                parser.setCurrentEvent("command", commandEventClass);

                Expression<?> fullCommand = new SkriptParser("full command", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                        .parseExpression(new Class[]{String.class});
                helper.assertTrue(
                        fullCommand instanceof ExprCommand,
                        Component.literal("Expected 'full command' to parse as ExprCommand.")
                );

                Expression<?> commandSender = new SkriptParser("command sender", SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                        .parseExpression(new Class[]{ServerPlayer.class});
                helper.assertTrue(
                        commandSender instanceof ExprCommandSender,
                        Component.literal("Expected 'command sender' to parse as ExprCommandSender.")
                );
            } finally {
                if (previousEventName == null) {
                    parser.deleteCurrentEvent();
                } else {
                    parser.setCurrentEvent(previousEventName, previousEventClasses);
                }
            }
        });
    }

    private static Class<?> resolveEventClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
