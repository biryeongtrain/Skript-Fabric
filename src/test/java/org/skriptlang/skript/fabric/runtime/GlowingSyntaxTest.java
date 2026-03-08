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
        // EffChange#toString should reflect the property expression text
        String text = statement.toString(null, false);
        assertEquals("set glowing of event-entity to true", text);
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

