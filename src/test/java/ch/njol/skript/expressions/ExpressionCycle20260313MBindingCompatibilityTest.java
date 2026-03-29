package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import com.mojang.authlib.GameProfile;

final class ExpressionCycle20260313MBindingCompatibilityTest {

    private static final AtomicBoolean SUPPORT_REGISTERED = new AtomicBoolean(false);

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        EntityData.register();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void clearRuntime() {
        SkriptRuntime.instance().clearScripts();
    }

    @Disabled("Moved to GameTest")
    @Test
    void bootstrapRegistersCycle20260313mExpressions() {
        assertExpressionRegistered(ExprSkull.class);
        assertExpressionRegistered(ExprSignText.class);
        assertExpressionRegistered(ExprSpawnerType.class);
    }

    @Test
    void parserBindsCycle20260313mExpressions() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-m-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on gametest cycle m syntax1 context:
                    set {_head} to skull of lane-m-offlineplayer
                    set line 2 of lane-m-sign-block to "cycle-m"
                    set {_line} to line 2 of lane-m-sign-block
                    set spawner type of lane-m-spawner-block to a cow
                    set {_type} to spawner type of lane-m-spawner-block
                """
        );

        Script script = SkriptRuntime.instance().loadFromPath(scriptPath);
        assertNotNull(script);
        assertEquals(1, script.getStructures().size());
    }

    private static void ensureSupportRegistered() {
        if (!SUPPORT_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        if (Classes.getExactClassInfo(EntityData.class) == null) {
            EntityData.register();
        }
        Skript.registerEvent(GameTestCycleMSyntax1Event.class, "gametest cycle m syntax1 context");
        Skript.registerExpression(LaneMOfflinePlayerExpression.class, GameProfile.class, "lane-m-offlineplayer");
        Skript.registerExpression(LaneMSignBlockExpression.class, FabricBlock.class, "lane-m-sign-block");
        Skript.registerExpression(LaneMSpawnerBlockExpression.class, FabricBlock.class, "lane-m-spawner-block");
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    private record CycleMSyntax1Handle() {
    }

    public static final class GameTestCycleMSyntax1Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleMSyntax1Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleMSyntax1Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle m syntax1 context";
        }
    }

    public static final class LaneMOfflinePlayerExpression extends ch.njol.skript.lang.util.SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000c1"), "cyclem")};
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

    public static final class LaneMSignBlockExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
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

    public static final class LaneMSpawnerBlockExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
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
}
