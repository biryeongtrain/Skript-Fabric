package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260312DBindingCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void parserBindsCycle20260312dRecoveredSyntax() {
        assertExpressionRegistered(ExprTimeState.class);
        assertInstanceOf(ExprSlotIndex.class, parseExpression("raw index of lane-d-slot", Long.class));
        assertInstanceOf(ExprTool.class, parseExpression("tool of lane-d-livingentity", Slot.class));
        assertInstanceOf(ExprWithFireResistance.class, parseExpression("lane-d-itemtype with fire resistance", FabricItemType.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(String.class, "string");
        registerClassInfo(Slot.class, "slot");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(FabricItemType.class, "itemtype");

        Skript.registerExpression(TestStringExpression.class, String.class, "lane-d-string");
        Skript.registerExpression(TestSlotExpression.class, Slot.class, "lane-d-slot");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-d-livingentity");
        Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-d-itemtype");
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static void assertExpressionRegistered(Class<?> type) {
        for (SyntaxInfo<?> syntax : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            if (syntax.type() == type) {
                return;
            }
        }
        throw new AssertionError(type.getSimpleName() + " was not registered by bootstrap");
    }

    public static final class TestStringExpression extends SimpleExpression<String> {
        @Override
        protected String @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends String> getReturnType() {
            return String.class;
        }
    }

    public static final class TestSlotExpression extends SimpleExpression<Slot> {
        @Override
        protected Slot @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Slot> getReturnType() {
            return Slot.class;
        }
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return null;
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
