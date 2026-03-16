package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.potion.util.PotionEffectSupport;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313JCompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsCycle20260313jExpressionsInExpectedContexts() {
        ParserInstance parser = ParserInstance.get();

        parser.setCurrentEvent("beacon effect", FabricEventCompatHandles.BeaconEffect.class);
        assertInstanceOf(ExprAppliedEffect.class, parseExpression("applied effect", Holder.class));
        parser.deleteCurrentEvent();

        assertInstanceOf(ExprNearestEntity.class, parseExpression("nearest cow relative to lane-j-location", Entity.class));
        assertInstanceOf(ExprTargetedBlock.class, parseExpression("target block of lane-j-livingentity", FabricBlock.class));
        assertInstanceOf(ExprTargetedBlock.class, parseExpression("actual target block of lane-j-livingentity", FabricBlock.class));
    }

    @Test
    void appliedEffectReadsBeaconCompatHandle() {
        ParserInstance.get().setCurrentEvent("beacon effect", FabricEventCompatHandles.BeaconEffect.class);
        ExprAppliedEffect expression = new ExprAppliedEffect();
        assertTrue(expression.init(new Expression[0], 0, ch.njol.util.Kleenean.FALSE, parseResult("applied effect")));

        Holder<MobEffect> effect = expression.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.BeaconEffect(null, BlockPos.ZERO, true, "speed"),
                null,
                null,
                null
        ));
        assertNotNull(effect);
        assertEquals("speed", PotionEffectSupport.effectId(effect));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        EntityData.register();
        registerClassInfo(Holder.class, "potioneffecttype");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(FabricLocation.class, "location");

        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-j-livingentity");
        Skript.registerExpression(TestLocationExpression.class, FabricLocation.class, "lane-j-location");
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

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
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

    public static final class TestLocationExpression extends SimpleExpression<FabricLocation> {
        @Override
        protected FabricLocation @Nullable [] get(SkriptEvent event) {
            return null;
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
