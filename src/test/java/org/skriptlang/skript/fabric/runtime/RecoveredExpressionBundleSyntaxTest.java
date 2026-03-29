package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.expressions.ExprAI;
import ch.njol.skript.expressions.ExprAttackCooldown;
import ch.njol.skript.expressions.ExprExhaustion;
import ch.njol.skript.expressions.ExprFallDistance;
import ch.njol.skript.expressions.ExprFireTicks;
import ch.njol.skript.expressions.ExprFlightMode;
import ch.njol.skript.expressions.ExprFreezeTicks;
import ch.njol.skript.expressions.ExprGravity;
import ch.njol.skript.expressions.ExprLastDamage;
import ch.njol.skript.expressions.ExprLevelProgress;
import ch.njol.skript.expressions.ExprMaxFreezeTicks;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;

@Tag("isolated-registry")
final class RecoveredExpressionBundleSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void recoveredExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprAI.class, parseExpressionInEvent("artificial intelligence of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprAttackCooldown.class, parseExpressionInEvent("attack cooldown of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprExhaustion.class, parseExpressionInEvent("exhaustion of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprFallDistance.class, parseExpressionInEvent("fall distance of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprFireTicks.class, parseExpressionInEvent("fire time of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprFlightMode.class, parseExpressionInEvent("flight mode of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprFreezeTicks.class, parseExpressionInEvent("freeze time of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprGravity.class, parseExpressionInEvent("gravity of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprLastDamage.class, parseExpressionInEvent("last damage of event-entity", FabricUseEntityHandle.class));
        assertInstanceOf(ExprLevelProgress.class, parseExpressionInEvent("level progress of event-player", FabricUseEntityHandle.class));
        assertInstanceOf(ExprMaxFreezeTicks.class, parseExpressionInEvent("maximum freeze time of event-entity", FabricUseEntityHandle.class));
    }

    @Test
    void recoveredMutableExpressionsParseAsChangeTargets() throws Exception {
        assertChangedExpression("set artificial intelligence of event-entity to false", "artificial intelligence of event-entity");
        assertChangedExpression("set freeze time of event-entity to 2 seconds", "freeze time of event-entity");
        assertChangedExpression("set flight mode of event-player to true", "flight mode of event-player");
        assertChangedExpression("add 0.25 to level progress of event-player", "level progress of event-player");
    }

    private void assertChangedExpression(String input, String expected) throws Exception {
        Statement statement = parseStatementInEvent(input, FabricUseEntityHandle.class);
        assertNotNull(statement);
        assertInstanceOf(EffChange.class, statement);
        assertEquals(expected, expression(statement, "changed").toString(null, false));
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(new Class[]{Object.class});
            assertNotNull(parsed, input);
            return parsed;
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Statement parseStatementInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return Statement.parse(input, "failed");
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private void restoreEventContext(ParserInstance parser, String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Object value = readObject(owner, fieldName);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
    }

    private Object readObject(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(owner);
    }

    private Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
        Class<?> current = owner;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
