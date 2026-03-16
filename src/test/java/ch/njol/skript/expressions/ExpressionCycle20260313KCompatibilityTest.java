package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.TriggerSection;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.sections.SecLoop;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313KCompatibilityTest {

    private static boolean supportRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSupportRegistered();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().setCurrentSections(List.of());
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsCycle20260313kExpressions() {
        assertInstanceOf(ExprElement.class, parseExpression("first element out of lane-k-values", Object.class));
        assertInstanceOf(ExprElement.class, parseExpression("elements from 2 to 3 out of lane-k-values", Object.class));
        assertInstanceOf(ExprXOf.class, parseExpression("3 of lane-k-itemtype", Object.class));

        ParserInstance parser = ParserInstance.get();
        parser.setCurrentSections(List.of(new StubLoopSection()));
        assertInstanceOf(ExprLoopValue.class, parseExpression("loop-value", Object.class));
        assertInstanceOf(ExprLoopValue.class, parseExpression("previous loop-value", Object.class));
        assertInstanceOf(ExprLoopValue.class, parseExpression("next loop-value", Object.class));
    }

    @Test
    void elementExpressionPreservesRangeAndKeys() {
        ExprElement<Object> first = new ExprElement<>();
        assertTrue(first.init(new Expression[]{new KeyedStringExpression(
                new String[]{"alpha", "beta", "gamma"},
                new String[]{"first", "second", "third"}
        )}, 0, ch.njol.util.Kleenean.FALSE, parseResult("first element out of lane-k-values")));
        assertArrayEquals(new Object[]{"alpha"}, first.getArray(SkriptEvent.EMPTY));

        ExprElement<Object> range = new ExprElement<>();
        assertTrue(range.init(new Expression[]{
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>(3, false),
                new KeyedStringExpression(
                        new String[]{"alpha", "beta", "gamma"},
                        new String[]{"first", "second", "third"}
                )
        }, 4, ch.njol.util.Kleenean.FALSE, parseResult("elements from 2 to 3 out of lane-k-values")));
        assertArrayEquals(new Object[]{"beta", "gamma"}, range.getArray(SkriptEvent.EMPTY));
        assertArrayEquals(new String[]{"second", "third"}, range.getArrayKeys(SkriptEvent.EMPTY));
    }

    @Test
    void xOfExpressionAdjustsFabricItemTypeAndEntityTypeAmounts() {
        ExprXOf itemTypeExpression = new ExprXOf();
        assertTrue(itemTypeExpression.init(new Expression[]{
                new SimpleLiteral<>(3, false),
                new SimpleLiteral<>(new FabricItemType(Items.DIAMOND), false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult("3 of lane-k-itemtype")));
        FabricItemType scaledItem = assertInstanceOf(FabricItemType.class, itemTypeExpression.getSingle(SkriptEvent.EMPTY));
        assertEquals(3, scaledItem.amount());
        assertEquals("diamond", scaledItem.itemId());

        ExprXOf entityTypeExpression = new ExprXOf();
        assertTrue(entityTypeExpression.init(new Expression[]{
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>(new EntityType(Cow.class, 1), false)
        }, 0, ch.njol.util.Kleenean.FALSE, parseResult("2 of lane-k-entitytype")));
        EntityType scaledEntity = assertInstanceOf(EntityType.class, entityTypeExpression.getSingle(SkriptEvent.EMPTY));
        assertEquals(2, scaledEntity.getAmount());
    }

    @Disabled("Moved to GameTest")
    @Test
    void loopValueReadsPreviousCurrentAndNextFromCurrentLoop() {
        ParserInstance parser = ParserInstance.get();
        parser.setCurrentSections(List.of(new StubLoopSection()));

        ExprLoopValue previous = assertInstanceOf(ExprLoopValue.class, parseExpression("previous loop-value", Object.class));
        ExprLoopValue current = assertInstanceOf(ExprLoopValue.class, parseExpression("loop-value", Object.class));
        ExprLoopValue next = assertInstanceOf(ExprLoopValue.class, parseExpression("next loop-value", Object.class));

        assertEquals("alpha", previous.getSingle(SkriptEvent.EMPTY));
        assertEquals("beta", current.getSingle(SkriptEvent.EMPTY));
        assertEquals("gamma", next.getSingle(SkriptEvent.EMPTY));
    }

    private static void ensureSupportRegistered() {
        if (supportRegistered) {
            return;
        }
        EntityData.register();
        registerClassInfo(FabricItemType.class, "itemtype");
        registerClassInfo(EntityType.class, "entitytype");
        registerClassInfo(ParticleEffect.class, "particle");
        Skript.registerExpression(LaneKValuesExpression.class, Object.class, "lane-k-values");
        Skript.registerExpression(LaneKItemTypeExpression.class, FabricItemType.class, "lane-k-itemtype");
        supportRegistered = true;
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

    public static final class LaneKValuesExpression extends SimpleExpression<Object> implements KeyProviderExpression<Object> {
        private static final Object[] VALUES = new Object[]{"alpha", "beta", "gamma"};
        private static final String[] KEYS = new String[]{"first", "second", "third"};

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return Arrays.copyOf(VALUES, VALUES.length);
        }

        @Override
        public @Nullable Iterator<? extends Object> iterator(SkriptEvent event) {
            return Arrays.asList(VALUES).iterator();
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
            return Arrays.copyOf(KEYS, KEYS.length);
        }

        @Override
        public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
            return Arrays.asList(KeyedValue.zip(VALUES, KEYS)).iterator();
        }
    }

    public static final class LaneKItemTypeExpression extends SimpleExpression<FabricItemType> {
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

    private static final class StubLoopSection extends SecLoop {
        private final Expression<?> looped = new SimpleLiteral<>(new String[]{"alpha", "beta", "gamma"}, String.class, true);

        @Override
        public @Nullable Object getCurrent(SkriptEvent event) {
            return "beta";
        }

        @Override
        public @Nullable Object getPrevious(SkriptEvent event) {
            return "alpha";
        }

        @Override
        public @Nullable Object getNext(SkriptEvent event) {
            return "gamma";
        }

        @Override
        public boolean supportsPeeking() {
            return true;
        }

        @Override
        public Expression<?> getLoopedExpression() {
            return looped;
        }

        @Override
        public Expression<?> getExpression() {
            return looped;
        }

        @Override
        public boolean isKeyedLoop() {
            return false;
        }

        @Override
        public SecLoop setNext(@Nullable TriggerItem next) {
            return this;
        }

        @Override
        public @Nullable TriggerItem getActualNext() {
            return null;
        }

        @Override
        public void exit(SkriptEvent event) {
        }

        @Override
        protected @Nullable TriggerItem walk(SkriptEvent event) {
            return null;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "stub loop";
        }
    }

    private static final class KeyedStringExpression extends SimpleExpression<Object> implements KeyProviderExpression<Object> {
        private final Object[] values;
        private final String[] keys;

        private KeyedStringExpression(Object[] values, String[] keys) {
            this.values = values;
            this.keys = keys;
        }

        @Override
        protected Object @Nullable [] get(SkriptEvent event) {
            return Arrays.copyOf(values, values.length);
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
            return Arrays.copyOf(keys, keys.length);
        }

        @Override
        public Iterator<KeyedValue<Object>> keyedIterator(SkriptEvent event) {
            return Arrays.asList(KeyedValue.zip(values, keys)).iterator();
        }
    }
}
