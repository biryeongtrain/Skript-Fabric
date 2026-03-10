package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.lang.event.SkriptEvent;
import com.mojang.authlib.GameProfile;

class ExpressionEntityCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void importedEntityExpressionsInstantiate() {
        assertDoesNotThrow(ExprActiveItem::new);
        assertDoesNotThrow(ExprAge::new);
        assertDoesNotThrow(ExprAllayJukebox::new);
        assertDoesNotThrow(ExprArrowsStuck::new);
        assertDoesNotThrow(ExprBeehiveFlower::new);
        assertDoesNotThrow(ExprBeehiveHoneyLevel::new);
        assertDoesNotThrow(ExprBreakSpeed::new);
        assertDoesNotThrow(ExprCreeperMaxFuseTicks::new);
        assertDoesNotThrow(ExprDomestication::new);
        assertDoesNotThrow(ExprDuplicateCooldown::new);
        assertDoesNotThrow(ExprEntityItemUseTime::new);
        assertDoesNotThrow(ExprEyeLocation::new);
        assertDoesNotThrow(ExprEntityOwner::new);
        assertDoesNotThrow(ExprEntitySize::new);
        assertDoesNotThrow(ExprExperienceCooldown::new);
        assertDoesNotThrow(ExprFoodLevel::new);
        assertDoesNotThrow(ExprGlidingState::new);
        assertDoesNotThrow(ExprHealth::new);
        assertDoesNotThrow(ExprItemOwner::new);
        assertDoesNotThrow(ExprItemThrower::new);
        assertDoesNotThrow(ExprLevel::new);
        assertDoesNotThrow(ExprMaxHealth::new);
        assertDoesNotThrow(ExprNoDamageTime::new);
        assertDoesNotThrow(ExprNoDamageTicks::new);
        assertDoesNotThrow(ExprPortalCooldown::new);
        assertDoesNotThrow(ExprRemainingAir::new);
        assertDoesNotThrow(ExprSpeed::new);
    }

    @Test
    void ageRejectsMaximumForEntityOnlyExpressions() {
        ExprAge age = new ExprAge();
        SkriptParser.ParseResult parse = parseResult("");
        parse.tags.add("max");
        boolean initialized = age.init(new Expression[]{new TypedExpression<>(net.minecraft.world.entity.LivingEntity.class)}, 0, Kleenean.FALSE, parse);
        assertFalse(initialized);
    }

    @Test
    void propertyHelpersRecognizeUpstreamBlockStateSemantics() {
        BlockState wheat = Blocks.WHEAT.defaultBlockState();
        assertEquals("age", ExprAge.findAgeProperty(wheat).getName());
        assertEquals(7, ExprAge.findAgeProperty(wheat).getPossibleValues().stream().mapToInt(Integer::intValue).max().orElseThrow());

        BlockState beehive = Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.HONEY_LEVEL, 3);
        assertEquals(3, ExprBeehiveHoneyLevel.honeyLevel(beehive, false));
        assertEquals(BeehiveBlock.MAX_HONEY_LEVELS, ExprBeehiveHoneyLevel.honeyLevel(beehive, true));
        assertNull(ExprBeehiveHoneyLevel.honeyLevel(Blocks.STONE.defaultBlockState(), false));
    }

    @Test
    void changeContractsMatchEntityPropertyExpectations() {
        ExprPortalCooldown portalCooldown = new ExprPortalCooldown();
        assertArrayEquals(new Class[]{ch.njol.skript.util.Timespan.class}, portalCooldown.acceptChange(ChangeMode.SET));

        ExprHealth health = new ExprHealth();
        assertArrayEquals(new Class[]{Number.class}, health.acceptChange(ChangeMode.ADD));

        ExprLevel level = new ExprLevel();
        assertArrayEquals(new Class[]{Number.class}, level.acceptChange(ChangeMode.SET));

        ExprMaxHealth maxHealth = new ExprMaxHealth();
        assertArrayEquals(new Class[]{Number.class}, maxHealth.acceptChange(ChangeMode.ADD));

        ExprNoDamageTicks noDamageTicks = new ExprNoDamageTicks();
        assertArrayEquals(new Class[]{Number.class}, noDamageTicks.acceptChange(ChangeMode.SET));

        ExprEntityOwner owner = new ExprEntityOwner();
        assertEquals(com.mojang.authlib.GameProfile.class, owner.getReturnType());

        ExprItemOwner itemOwner = new ExprItemOwner();
        assertEquals(UUID.class, itemOwner.getReturnType());
        assertArrayEquals(new Class[]{net.minecraft.world.entity.Entity.class, GameProfile.class, UUID.class}, itemOwner.acceptChange(ChangeMode.SET));

        ExprItemThrower itemThrower = new ExprItemThrower();
        assertEquals(UUID.class, itemThrower.getReturnType());

        ExprGlidingState gliding = new ExprGlidingState();
        assertArrayEquals(new Class[]{Boolean.class}, gliding.acceptChange(ChangeMode.SET));
        assertNull(gliding.acceptChange(ChangeMode.ADD));
    }

    @Test
    void entityItemUseTimeAndHoneyLevelRespectParseTags() {
        ExprEntityItemUseTime remaining = new ExprEntityItemUseTime();
        SkriptParser.ParseResult remainingParse = parseResult("");
        remainingParse.tags.add("remaining");
        assertTrue(remaining.init(new Expression[]{new TypedExpression<>(net.minecraft.world.entity.LivingEntity.class)}, 0, Kleenean.FALSE, remainingParse));
        assertEquals("remaining item usage time of TypedExpression", remaining.toString(SkriptEvent.EMPTY, false));

        ExprBeehiveHoneyLevel maxHoney = new ExprBeehiveHoneyLevel();
        SkriptParser.ParseResult maxParse = parseResult("");
        maxParse.tags.add("max");
        assertTrue(maxHoney.init(new Expression[]{new TypedExpression<>(FabricBlock.class)}, 0, Kleenean.FALSE, maxParse));
        assertNull(maxHoney.acceptChange(ChangeMode.SET));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static final class TypedExpression<T> implements Expression<T> {

        private final Class<? extends T> type;

        private TypedExpression(Class<? extends T> type) {
            this.type = type;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return type;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public String toString(SkriptEvent event, boolean debug) {
            return "TypedExpression";
        }
    }
}
