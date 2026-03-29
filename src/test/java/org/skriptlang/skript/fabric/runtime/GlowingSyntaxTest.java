package org.skriptlang.skript.fabric.runtime;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.parser.ParserInstance;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;

final class GlowingSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void glowingPropertyExpressionParsesAndChanges() {
        Statement statement = parseStatementInEvent("set glowing of event-entity to true", FabricUseEntityHandle.class);
        assertNotNull(statement);
        assertInstanceOf(EffChange.class, statement);
        // Inspect the change target and value directly for exact property text
        EffChange eff = (EffChange) statement;
        try {
            java.lang.reflect.Field changed = eff.getClass().getDeclaredField("changed");
            changed.setAccessible(true);
            Object expr = changed.get(eff);
            assertNotNull(expr);
            assertEquals("glowing of event-entity", ((ch.njol.skript.lang.Expression<?>) expr).toString(null, false));

            // The change value is a boolean literal; do not constrain its renderer string here.
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private Statement parseStatementInEvent(String statement, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return Statement.parse(statement, "failed");
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }
}
