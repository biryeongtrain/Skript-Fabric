package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.parser.ParserInstance;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.conditions.CondIsBurning;

final class BurningSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void burningConditionParsesExactUpstreamAliases() {
        CondIsBurning burning = parseConditionInEvent("event-entity is burning", CondIsBurning.class, FabricUseEntityHandle.class);
        assertFalse(burning.isNegated());

        CondIsBurning ignited = parseConditionInEvent("event-entity is ignited", CondIsBurning.class, FabricUseEntityHandle.class);
        assertFalse(ignited.isNegated());

        CondIsBurning onFire = parseConditionInEvent("event-entity is on fire", CondIsBurning.class, FabricUseEntityHandle.class);
        assertFalse(onFire.isNegated());
    }

    @Test
    void burningConditionParsesNegativeExactAlias() {
        CondIsBurning negative = parseConditionInEvent("event-entity is not on fire", CondIsBurning.class, FabricUseEntityHandle.class);
        assertTrue(negative.isNegated());
    }

    private <T> T parseConditionInEvent(String condition, Class<T> conditionClass, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            Condition parsed = Condition.parse(condition, null);
            assertNotNull(parsed);
            assertInstanceOf(conditionClass, parsed);
            return conditionClass.cast(parsed);
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
}
