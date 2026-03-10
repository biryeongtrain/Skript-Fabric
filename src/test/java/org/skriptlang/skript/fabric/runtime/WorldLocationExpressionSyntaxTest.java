package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.expressions.ExprChunkX;
import ch.njol.skript.expressions.ExprChunkZ;
import ch.njol.skript.expressions.ExprHumidity;
import ch.njol.skript.expressions.ExprLocation;
import ch.njol.skript.expressions.ExprLocationAt;
import ch.njol.skript.expressions.ExprLocationOf;
import ch.njol.skript.expressions.ExprRedstoneBlockPower;
import ch.njol.skript.expressions.ExprSeaLevel;
import ch.njol.skript.expressions.ExprSeed;
import ch.njol.skript.expressions.ExprSimulationDistance;
import ch.njol.skript.expressions.ExprSpawn;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("isolated-registry")
final class WorldLocationExpressionSyntaxTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
    }

    @Test
    void worldLocationExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprChunkX.class, parseExpression("chunk x-coordinate of chunk at location at x = 0, y = 64, and z = 0"));
        assertInstanceOf(ExprChunkZ.class, parseExpression("chunk z-coordinate of chunk at location at x = 0, y = 64, and z = 0"));
        assertInstanceOf(ExprHumidity.class, parseExpression("humidity of event-block", ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class));
        assertInstanceOf(ExprLocation.class, parseExpression("event-location", ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class));
        assertInstanceOf(ExprLocationAt.class, parseExpression("location at x = 1, y = 2, and z = 3"));
        assertInstanceOf(ExprLocationOf.class, parseExpression("location of event-block", ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class));
        assertInstanceOf(ExprRedstoneBlockPower.class, parseExpression("redstone power of event-block", ch.njol.skript.events.FabricEventCompatHandles.BeaconToggle.class));
        assertInstanceOf(ExprSeaLevel.class, parseExpression("sea level of world of location at x = 0, y = 64, and z = 0"));
        assertInstanceOf(ExprSeed.class, parseExpression("seed of world of location at x = 0, y = 64, and z = 0"));
        assertInstanceOf(ExprSimulationDistance.class, parseExpression("simulation distance of world of location at x = 0, y = 64, and z = 0"));
        assertInstanceOf(ExprSpawn.class, parseExpression("spawn location of world of location at x = 0, y = 64, and z = 0"));
    }

    private Expression<?> parseExpression(String input, Class<?>... eventClasses) {
        var parser = ch.njol.skript.lang.parser.ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            if (eventClasses.length > 0) {
                parser.setCurrentEvent("gametest", eventClasses);
            }
            Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                    .parseExpression(new Class[]{Object.class});
            assertNotNull(parsed, input);
            return parsed;
        } finally {
            if (previousEventName == null) {
                parser.deleteCurrentEvent();
            } else {
                parser.setCurrentEvent(previousEventName, previousEventClasses);
            }
        }
    }
}
