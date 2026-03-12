package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260312EBindingCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void bootstrapRegistersCycle20260312eExpressions() {
        assertExpressionRegistered(ExprCarryingBlockData.class);
        assertExpressionRegistered(ExprNamed.class);
        assertExpressionRegistered(ExprOfflinePlayers.class);
        assertExpressionRegistered(ExprPandaGene.class);
        assertExpressionRegistered(ExprPlain.class);
        assertExpressionRegistered(ExprSeaPickles.class);
        assertExpressionRegistered(ExprWardenAngryAt.class);
        assertExpressionRegistered(ExprWardenEntityAnger.class);
        assertExpressionRegistered(ExprWithItemFlags.class);
    }

    @Test
    void parserBindsCycle20260312eExpressions() {
        assertInstanceOf(ExprSeaPickles.class, parseExpression("sea pickle count of lane-e-block", Integer.class));
        assertInstanceOf(ExprPandaGene.class, parseExpression("main gene of lane-e-livingentity", net.minecraft.world.entity.animal.Panda.Gene.class));
        assertInstanceOf(ExprPlain.class, parseExpression("plain lane-e-itemtype", FabricItemType.class));
        assertInstanceOf(ExprNamed.class, parseExpression("lane-e-itemtype named \"Lane E\"", FabricItemType.class));
        assertInstanceOf(ExprWithItemFlags.class, parseExpression("lane-e-itemtype with item flags \"hide enchants\"", FabricItemType.class));
        assertInstanceOf(ExprCarryingBlockData.class, parseExpression("carried blockdata of lane-e-livingentity", BlockState.class));
        assertInstanceOf(ExprWardenAngryAt.class, parseExpression("most angered entity of lane-e-livingentity", LivingEntity.class));
        assertInstanceOf(ExprWardenEntityAnger.class, parseExpression("anger level of lane-e-livingentity towards lane-e-livingentity", Integer.class));
        assertInstanceOf(ExprOfflinePlayers.class, parseExpression("offline players", GameProfile.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(FabricItemType.class, "itemtype");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(BlockState.class, "blockdata");
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(net.minecraft.world.entity.animal.Panda.Gene.class, "gene");

        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-e-block");
        Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-e-itemtype");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-e-livingentity");
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

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
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
}
