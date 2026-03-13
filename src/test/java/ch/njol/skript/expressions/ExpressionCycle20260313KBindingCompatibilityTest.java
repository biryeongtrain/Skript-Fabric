package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.fabric.runtime.SkriptRuntime;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260313KBindingCompatibilityTest {

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
    void bootstrapRegistersCycle20260313kExpressions() {
        assertExpressionRegistered(ExprElement.class);
        assertExpressionRegistered(ExprLoopValue.class);
        assertExpressionRegistered(ExprXOf.class);
    }

    @Test
    void parserBindsCycle20260313kExpressions() throws Exception {
        Path scriptPath = Files.createTempFile("expr-cycle-k-binding", ".sk");
        Files.writeString(
                scriptPath,
                """
                on gametest cycle k syntax1 context:
                    set {_first} to first element out of lane-k-values
                    set {_range::*} to elements from 2 to 3 out of lane-k-values
                    set {_scaled} to 3 of lane-k-itemtype
                    loop lane-k-values:
                        if loop-iteration is 2:
                            set {_previous} to previous loop-value
                            set {_current} to loop-value
                            set {_next} to next loop-value
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
        Skript.registerEvent(GameTestCycleKSyntax1Event.class, "gametest cycle k syntax1 context");
        Skript.registerExpression(LaneKValuesExpression.class, Object.class, "lane-k-values");
        Skript.registerExpression(LaneKItemTypeExpression.class, FabricItemType.class, "lane-k-itemtype");
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    private record CycleKSyntax1Handle() {
    }

    public static final class GameTestCycleKSyntax1Event extends ch.njol.skript.lang.SkriptEvent {
        @Override
        public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
            return args.length == 0;
        }

        @Override
        public boolean check(SkriptEvent event) {
            return event.handle() instanceof CycleKSyntax1Handle;
        }

        @Override
        public Class<?>[] getEventClasses() {
            return new Class<?>[]{CycleKSyntax1Handle.class};
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "gametest cycle k syntax1 context";
        }
    }

    public static final class LaneKValuesExpression extends ch.njol.skript.lang.util.SimpleExpression<Object>
            implements ch.njol.skript.lang.KeyProviderExpression<Object> {
        private static final Object[] VALUES = new Object[]{"alpha", "beta", "gamma"};
        private static final String[] KEYS = new String[]{"first", "second", "third"};

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return java.util.Arrays.copyOf(VALUES, VALUES.length);
        }

        @Override
        public @Nullable java.util.Iterator<? extends Object> iterator(SkriptEvent event) {
            return java.util.Arrays.asList(VALUES).iterator();
        }

        @Override
        public boolean supportsLoopPeeking() {
            return true;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<?> getReturnType() {
            return Object.class;
        }

        @Override
        public String[] getArrayKeys(SkriptEvent event) {
            return java.util.Arrays.copyOf(KEYS, KEYS.length);
        }

        @Override
        public java.util.Iterator<ch.njol.skript.lang.KeyedValue<Object>> keyedIterator(SkriptEvent event) {
            return java.util.Arrays.asList(ch.njol.skript.lang.KeyedValue.zip(VALUES, KEYS)).iterator();
        }
    }

    public static final class LaneKItemTypeExpression extends ch.njol.skript.lang.util.SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[]{new FabricItemType(Items.DIAMOND)};
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
}
