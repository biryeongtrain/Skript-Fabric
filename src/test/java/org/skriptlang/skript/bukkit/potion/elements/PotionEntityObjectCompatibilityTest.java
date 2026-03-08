package org.skriptlang.skript.bukkit.potion.elements;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffApplyPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.effects.EffPoison;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffect;
import org.skriptlang.skript.bukkit.potion.elements.expressions.ExprPotionEffects;
import org.skriptlang.skript.bukkit.potion.util.SkriptPotionEffect;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class PotionEntityObjectCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void applyPotionEffectAcceptsObjectBackedEntityExpression() {
        EffApplyPotionEffect effect = new EffApplyPotionEffect();

        boolean initialized = effect.init(
                new Expression<?>[]{
                        new ConstantExpression<>(SkriptPotionEffect.class, SkriptPotionEffect.fromType(MobEffects.POISON)),
                        new ObjectBackedEntityExpression()
                },
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        assertDoesNotThrow(() -> TriggerItem.walk(effect, SkriptEvent.EMPTY));
    }

    @Test
    void poisonEffectAcceptsObjectBackedEntityExpression() {
        EffPoison effect = new EffPoison();

        boolean initialized = effect.init(
                new Expression<?>[]{new ObjectBackedEntityExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        assertDoesNotThrow(() -> TriggerItem.walk(effect, SkriptEvent.EMPTY));
    }

    @Test
    void specificPotionEffectAcceptsObjectBackedEntityExpression() {
        ExprPotionEffect expression = new ExprPotionEffect();

        boolean initialized = expression.init(
                new Expression<?>[]{
                        new ConstantExpression<>(Object.class, "poison"),
                        new ObjectBackedEntityExpression()
                },
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        assertEquals(0, assertDoesNotThrow(() -> expression.getArray(SkriptEvent.EMPTY)).length);
    }

    @Test
    void activePotionEffectsAcceptObjectBackedEntityExpression() {
        ExprPotionEffects expression = new ExprPotionEffects();

        boolean initialized = expression.init(
                new Expression<?>[]{new ObjectBackedEntityExpression()},
                0,
                Kleenean.FALSE,
                new ParseResult()
        );

        assertTrue(initialized);
        assertEquals(0, assertDoesNotThrow(() -> expression.getArray(SkriptEvent.EMPTY)).length);
    }

    private static final class ObjectBackedEntityExpression implements Expression<Object> {

        @Override
        public Object[] getArray(SkriptEvent event) {
            return new Object[0];
        }

        @Override
        public Object[] getAll(SkriptEvent event) {
            return new Object[0];
        }

        @Override
        public boolean canReturn(Class<?> returnType) {
            return returnType == Object.class || returnType == Entity.class;
        }

        @Override
        public boolean isSingle() {
            return false;
        }

        @Override
        public Class<? extends Object> getReturnType() {
            return Object.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "object backed entities";
        }
    }

    private static final class ConstantExpression<T> extends SimpleExpression<T> {

        private final Class<? extends T> returnType;
        private final T[] values;

        @SafeVarargs
        private ConstantExpression(Class<? extends T> returnType, T... values) {
            this.returnType = returnType;
            this.values = values;
        }

        @Override
        protected T @Nullable [] get(SkriptEvent event) {
            return values;
        }

        @Override
        public boolean isSingle() {
            return values.length == 1;
        }

        @Override
        public Class<? extends T> getReturnType() {
            return returnType;
        }
    }
}
