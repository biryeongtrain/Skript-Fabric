package kim.biryeong.skriptFabricPort.gametest;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyValued;
import ch.njol.skript.variables.Variables;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;

public final class SkriptFabricExpressionParsingAndArgumentsGameTest extends AbstractSkriptFabricGameTestSupport {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);
    private static final ParseArgsMutableValue VALUE_FIXTURE = new ParseArgsMutableValue("alpha");

    @GameTest
    public void argumentExpressionExecutesRealCommandHook(GameTestHelper helper) {
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/parsing-and-arguments/argument_records_values.sk");

            BlockPos markerRelative = new BlockPos(5, 1, 0);
            BlockPos markerAbsolute = helper.absolutePos(markerRelative);
            helper.getLevel().setBlockAndUpdate(markerAbsolute, Blocks.AIR.defaultBlockState());

            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            player.setGameMode(GameType.CREATIVE);
            player.teleportTo(markerAbsolute.getX() + 0.5D, markerAbsolute.getY() + 1.0D, markerAbsolute.getZ() + 0.5D);

            helper.getLevel().getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), "say alpha beta gamma");

            helper.assertBlockPresent(Blocks.EMERALD_BLOCK, markerRelative);
            assertString(helper, "parseargs::argument::first", "alpha");
            assertString(helper, "parseargs::argument::second", "beta");
            assertString(helper, "parseargs::argument::last", "gamma");
            assertString(helper, "parseargs::argument::all::1", "alpha");
            assertString(helper, "parseargs::argument::all::2", "beta");
            assertString(helper, "parseargs::argument::all::3", "gamma");

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void parseAndParseErrorExpressionsExecuteRealScript(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/parsing-and-arguments/parse_records_values.sk");

            int executed = dispatch(runtime, helper);
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for parsing and arguments parse script but got " + executed + ".")
            );

            assertNumber(helper, "parseargs::parse::integer", 12);
            helper.assertTrue(
                    Variables.getVariable("parseargs::parse::failed", null, false) == null,
                    Component.literal("Expected failed parse result to stay unset.")
            );
            Object parseError = Variables.getVariable("parseargs::parse::error", null, false);
            helper.assertTrue(
                    parseError instanceof String message && !message.isBlank() && message.contains("could not be parsed"),
                    Component.literal("Expected parse error to record a non-empty failure message but got " + parseError + ".")
            );

            runtime.clearScripts();
            Variables.clearAll();
        });
    }

    @GameTest
    public void valueExpressionReadsAndChangesRealScriptFixture(GameTestHelper helper) {
        ensureSupportRegistered();
        runWithRuntimeLock(helper, () -> {
            VALUE_FIXTURE.changeValue("alpha");

            SkriptRuntime runtime = SkriptRuntime.instance();
            runtime.clearScripts();
            Variables.clearAll();
            runtime.loadFromResource("skript/gametest/expression/parsing-and-arguments/value_records_changes.sk");

            int executed = dispatch(runtime, helper);
            helper.assertTrue(
                    executed == 1,
                    Component.literal("Expected 1 trigger for parsing and arguments value script but got " + executed + ".")
            );

            assertString(helper, "parseargs::value::before", "alpha");
            assertString(helper, "parseargs::value::after_set", "beta");
            helper.assertTrue(
                    Variables.getVariable("parseargs::value::after_delete", null, false) == null,
                    Component.literal("Expected deleted value expression result to be unset.")
            );
            helper.assertTrue(
                    VALUE_FIXTURE.value() == null,
                    Component.literal("Expected value fixture to be reset by delete string value of parseargs-valued.")
            );

            runtime.clearScripts();
            Variables.clearAll();
            VALUE_FIXTURE.changeValue("alpha");
        });
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestParsingAndArgumentsEvent.class, "gametest parsing and arguments context");
        Skript.registerExpression(ParseArgsValuedExpression.class, ParseArgsMutableValue.class, "parseargs-valued");
    }

    private static int dispatch(SkriptRuntime runtime, GameTestHelper helper) {
        return runtime.dispatch(new SkriptEvent(new ParsingAndArgumentsHandle(), helper.getLevel().getServer(), helper.getLevel(), null));
    }

    private static void assertString(GameTestHelper helper, String name, String expected) {
        Object value = Variables.getVariable(name, null, false);
        helper.assertTrue(
                expected.equals(value),
                Component.literal("Expected " + name + " to be " + expected + " but got " + value + ".")
        );
    }

    private static void assertNumber(GameTestHelper helper, String name, int expected) {
        Object value = Variables.getVariable(name, null, false);
        helper.assertTrue(
                value instanceof Number number && number.intValue() == expected,
                Component.literal("Expected " + name + " to be " + expected + " but got " + value + ".")
        );
    }

    private record ParsingAndArgumentsHandle() {
    }

    public static final class GameTestParsingAndArgumentsEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof ParsingAndArgumentsHandle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{ParsingAndArgumentsHandle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest parsing and arguments context";
        }
    }

    public static final class ParseArgsValuedExpression extends SimpleExpression<ParseArgsMutableValue> {
        @Override
        protected ParseArgsMutableValue @Nullable [] get(SkriptEvent event) {
            return new ParseArgsMutableValue[]{VALUE_FIXTURE};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ParseArgsMutableValue> getReturnType() {
            return ParseArgsMutableValue.class;
        }
    }

    public static final class ParseArgsMutableValue implements AnyValued<String> {
        private String value;

        private ParseArgsMutableValue(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean supportsValueChange() {
            return true;
        }

        @Override
        public void changeValue(String value) {
            this.value = value;
        }

        @Override
        public Class<String> valueType() {
            return String.class;
        }
    }
}
