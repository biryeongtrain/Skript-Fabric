package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.events.FabricEventCompatHandles;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313LCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        EntityData.register();
        SkriptFabricBootstrap.bootstrap();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Disabled("Moved to GameTest")
    @Test
    void parserBindsCycle20260313lExpressions() {
        ParserInstance.get().setCurrentEvent("shoot bow", FabricEventCompatHandles.EntityShootBow.class);
        assertInstanceOf(ExprProjectileForce.class, parseExpression("projectile force", Float.class));
    }

    @Test
    void projectileForceReadsShootBowHandle() {
        ParserInstance.get().setCurrentEvent("shoot bow", FabricEventCompatHandles.EntityShootBow.class);
        ExprProjectileForce expression = new ExprProjectileForce();
        assertTrue(expression.init(new Expression[0], 0, Kleenean.FALSE, parseResult("projectile force")));

        Float force = expression.getSingle(new SkriptEvent(
                new FabricEventCompatHandles.EntityShootBow((LivingEntity) null, null, 0.85F),
                null,
                null,
                null
        ));
        assertNotNull(force);
        assertEquals(0.85F, force);
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private static SkriptParser.ParseResult parseResult(String expression) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expression;
        return result;
    }
}
