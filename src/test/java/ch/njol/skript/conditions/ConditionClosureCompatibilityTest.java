package ch.njol.skript.conditions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Validated;

class ConditionClosureCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.CONDITION);
    }

    @Test
    void importedClosureConditionsInstantiate() {
        assertDoesNotThrow(CondEndermanStaredAt::new);
        assertDoesNotThrow(CondHasCustomModelData::new);
        assertDoesNotThrow(CondHasLineOfSight::new);
        assertDoesNotThrow(CondIsCharged::new);
        assertDoesNotThrow(CondIsDancing::new);
        assertDoesNotThrow(CondIsEating::new);
        assertDoesNotThrow(CondIsFireResistant::new);
        assertDoesNotThrow(CondIsJumping::new);
        assertDoesNotThrow(CondIsPersistent::new);
        assertDoesNotThrow(CondIsTicking::new);
        assertDoesNotThrow(CondIsValid::new);
        assertDoesNotThrow(CondLidState::new);
    }

    @Test
    void importedClosureConditionsPreserveLegacyToStringShapes() {
        CondEndermanStaredAt staredAt = new CondEndermanStaredAt();
        staredAt.init(new Expression[]{new TestExpression<>("enderman", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("enderman has stared at", staredAt.toString(SkriptEvent.EMPTY, false));

        CondHasCustomModelData customModelData = new CondHasCustomModelData();
        SkriptParser.ParseResult customModelDataParse = parseResult("");
        customModelDataParse.mark = 2;
        customModelData.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STICK), false)}, 0, Kleenean.FALSE, customModelDataParse);
        assertEquals("[stick] has custom model data flags", customModelData.toString(SkriptEvent.EMPTY, false));

        CondHasLineOfSight lineOfSight = new CondHasLineOfSight();
        lineOfSight.init(new Expression[]{
                new TestExpression<>("watchers", LivingEntity.class),
                new TestExpression<>("targets", Object.class)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("watchers has line of sight to targets", lineOfSight.toString(SkriptEvent.EMPTY, false));

        CondIsCharged charged = new CondIsCharged();
        charged.init(new Expression[]{new TestExpression<>("creeper", Entity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("creeper is charged", charged.toString(SkriptEvent.EMPTY, false));

        CondIsDancing dancing = new CondIsDancing();
        dancing.init(new Expression[]{new TestExpression<>("parrot", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("parrot is dancing", dancing.toString(SkriptEvent.EMPTY, false));

        CondIsEating eating = new CondIsEating();
        eating.init(new Expression[]{new TestExpression<>("horse", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("horse is eating", eating.toString(SkriptEvent.EMPTY, false));

        CondIsJumping jumping = new CondIsJumping();
        jumping.init(new Expression[]{new TestExpression<>("mob", LivingEntity.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("mob is jumping", jumping.toString(SkriptEvent.EMPTY, false));

        CondIsPersistent persistent = new CondIsPersistent();
        persistent.init(new Expression[]{new TestExpression<>("entities", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("entities are persistent", persistent.toString(SkriptEvent.EMPTY, false));

        CondIsTicking ticking = new CondIsTicking();
        ticking.init(new Expression[]{new TestExpression<>("entity", Entity.class)}, 1, Kleenean.FALSE, parseResult(""));
        assertEquals("entity is not ticking", ticking.toString(SkriptEvent.EMPTY, false));

        CondIsValid valid = new CondIsValid();
        valid.init(new Expression[]{new TestExpression<>("script", Object.class)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("script is valid", valid.toString(SkriptEvent.EMPTY, false));

        CondLidState lidState = new CondLidState();
        SkriptParser.ParseResult lidParse = parseResult("");
        lidParse.tags.add("close");
        lidState.init(new Expression[]{new TestExpression<>("containers", FabricBlock.class)}, 0, Kleenean.FALSE, lidParse);
        assertEquals("the lids of containers are closed", lidState.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void importedClosureConditionsRetainBasicRuntimeChecks() {
        CondIsFireResistant fireResistant = new CondIsFireResistant();
        fireResistant.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.NETHERITE_SWORD), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(fireResistant.check(SkriptEvent.EMPTY));

        CondIsFireResistant notFireResistant = new CondIsFireResistant();
        notFireResistant.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.DIAMOND_SWORD), false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(notFireResistant.check(SkriptEvent.EMPTY));

        CondIsValid valid = new CondIsValid();
        valid.init(new Expression[]{new SimpleLiteral<>(new TestValidated(true), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertTrue(valid.check(SkriptEvent.EMPTY));

        CondIsValid invalid = new CondIsValid();
        invalid.init(new Expression[]{new SimpleLiteral<>(new TestValidated(false), false)}, 1, Kleenean.FALSE, parseResult(""));
        assertTrue(invalid.check(SkriptEvent.EMPTY));

        CondIsJumping jumping = new CondIsJumping();
        assertFalse(jumping.init(new Expression[]{new TestExpression<>("player", ServerPlayer.class)}, 0, Kleenean.FALSE, parseResult("")));
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private record TestValidated(boolean valid) implements Validated {

        @Override
        public void invalidate() {
        }
    }

    private static final class TestExpression<T> extends SimpleExpression<T> {

        private final String text;
        private final Class<? extends T> returnType;

        private TestExpression(String text, Class<? extends T> returnType) {
            this.text = text;
            this.returnType = returnType;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            @SuppressWarnings("unchecked")
            T[] empty = (T[]) java.lang.reflect.Array.newInstance(returnType, 0);
            return empty;
        }

        @Override
        public boolean isSingle() {
            return !text.endsWith("s");
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return text;
        }
    }
}
