package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.common.expressions.ExprColorFromHexCode;
import org.skriptlang.skript.common.expressions.ExprHexCode;
import org.skriptlang.skript.common.expressions.ExprRecursiveSize;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.structure.Structure;
import ch.njol.skript.util.Color;

@Tag("isolated-registry")
final class ExpressionCycle20260313FSafe4BindingCompatibilityTest {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Test
    void expressionsParseThroughBootstrap() {
        assertInstanceOf(ExprHexCode.class, parseExpression("hex code of (color from hex code \"#FF0000\")", String.class));
        assertInstanceOf(ExprColorFromHexCode.class, parseExpression("color from hex code \"AA3366CC\"", Color.class));
        assertInstanceOf(ExprRecursiveSize.class, parseExpression("recursive size of {cyclef::source::*}", Long.class));
        assertInstanceOf(ExprBlockSphere.class, parseExpression("blocks in radius 1 around lane-f-center-location", FabricBlock.class));
    }

    @Test
    void fixturesBindThroughRuntime() throws Exception {
        assertFixtureLoads("skript/gametest/expression/cycle-f-safe4/hex_code_records_value.sk");
        assertFixtureLoads("skript/gametest/expression/cycle-f-safe4/color_from_hex_records_channels.sk");
        assertFixtureLoads("skript/gametest/expression/cycle-f-safe4/recursive_size_records_value.sk");
        assertFixtureLoads("skript/gametest/expression/cycle-f-safe4/block_sphere_records_blocks.sk");
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        Skript.registerEvent(GameTestCycleFSafe4ContextEvent.class, "gametest cycle f safe4 context");
        Skript.registerExpression(LaneFCenterLocationExpression.class, FabricLocation.class, "lane-f-center-location");
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static void assertFixtureLoads(String resourcePath) throws Exception {
        SkriptRuntime runtime = SkriptRuntime.instance();
        runtime.clearScripts();
        runtime.loadFromPath(Path.of("src/gametest/resources").resolve(resourcePath));
        assertNotNull(onlyLoadedTrigger(runtime), resourcePath);
    }

    private static Trigger onlyLoadedTrigger(SkriptRuntime runtime) throws ReflectiveOperationException {
        Field scriptsField = SkriptRuntime.class.getDeclaredField("scripts");
        scriptsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Script> scripts = (List<Script>) scriptsField.get(runtime);
        Script script = scripts.getFirst();
        Structure structure = script.getStructures().getFirst();
        return ((ch.njol.skript.lang.SkriptEvent) structure).getTrigger();
    }

    public static final class GameTestCycleFSafe4ContextEvent extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return false;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{Object.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle f safe4 context";
        }
    }

    public static final class LaneFCenterLocationExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return new FabricLocation[]{new FabricLocation(null, new Vec3(0.5D, 0.5D, 0.5D))};
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
