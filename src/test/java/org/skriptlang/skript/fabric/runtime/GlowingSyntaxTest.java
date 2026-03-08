package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.parser.ParserInstance;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;

final class GlowingSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
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

            java.lang.reflect.Field changeWith = eff.getClass().getDeclaredField("changeWith");
            changeWith.setAccessible(true);
            Object value = changeWith.get(eff);
            assertNotNull(value);
            String rendered = ((ch.njol.skript.lang.Expression<?>) value).toString(null, false);
            // accept any boolean literal rendering that evaluates to true
            org.junit.jupiter.api.Assertions.assertTrue(Boolean.parseBoolean(rendered.toLowerCase()), rendered);
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
