package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.literals.LitConsole;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260313FSafe6CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrap() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void minecartSyntaxInstantiatesAndChangeContractsMatch() {
        assertInstanceOf(ExprMaxMinecartSpeed.class, new ExprMaxMinecartSpeed());
        assertInstanceOf(ExprMinecartDerailedFlyingVelocity.class, new ExprMinecartDerailedFlyingVelocity());

        ExprMaxMinecartSpeed maxSpeed = new ExprMaxMinecartSpeed();
        assertArrayEquals(new Class[]{Number.class}, maxSpeed.acceptChange(ChangeMode.SET));
        assertArrayEquals(new Class[]{Number.class}, maxSpeed.acceptChange(ChangeMode.RESET));

        ExprMinecartDerailedFlyingVelocity velocity = new ExprMinecartDerailedFlyingVelocity();
        assertArrayEquals(new Class[]{Vec3.class}, velocity.acceptChange(ChangeMode.SET));
        assertArrayEquals(new Class[]{Vec3.class}, velocity.acceptChange(ChangeMode.ADD));
    }

    @Test
    void minecartCompatStateUsesExpectedDefaultsAndMutations() {
        Object minecart = new Object();

        assertEquals(MinecartExpressionSupport.DEFAULT_MAX_SPEED, MinecartExpressionSupport.maxSpeed(minecart));
        assertEquals(MinecartExpressionSupport.DEFAULT_DERAILED_VELOCITY, MinecartExpressionSupport.velocity(minecart, false));
        assertEquals(MinecartExpressionSupport.DEFAULT_FLYING_VELOCITY, MinecartExpressionSupport.velocity(minecart, true));

        MinecartExpressionSupport.setMaxSpeed(minecart, 1.25D);
        MinecartExpressionSupport.setVelocity(minecart, false, new Vec3(1.0D, 2.0D, 3.0D));
        MinecartExpressionSupport.setVelocity(minecart, true, new Vec3(4.0D, 5.0D, 6.0D));

        assertEquals(1.25D, MinecartExpressionSupport.maxSpeed(minecart));
        assertEquals(new Vec3(1.0D, 2.0D, 3.0D), MinecartExpressionSupport.velocity(minecart, false));
        assertEquals(new Vec3(4.0D, 5.0D, 6.0D), MinecartExpressionSupport.velocity(minecart, true));

        MinecartExpressionSupport.resetMaxSpeed(minecart);
        assertEquals(MinecartExpressionSupport.DEFAULT_MAX_SPEED, MinecartExpressionSupport.maxSpeed(minecart));
    }

    @Test
    void consoleLiteralParsesAgainstServerType() {
        Expression<? extends MinecraftServer> parsed = parse("the console", MinecraftServer.class);
        assertNotNull(parsed);
        assertInstanceOf(LitConsole.class, parsed);
        assertEquals("the console", parsed.toString(SkriptEvent.EMPTY, false));
    }

    @Test
    void compassTargetContractsAndStoredTargetsBehave() {
        assertInstanceOf(ExprCompassTarget.class, new ExprCompassTarget());

        ExprCompassTarget compassTarget = new ExprCompassTarget();
        assertArrayEquals(new Class[]{FabricLocation.class}, compassTarget.acceptChange(ChangeMode.SET));
        assertArrayEquals(new Class[]{FabricLocation.class}, compassTarget.acceptChange(ChangeMode.RESET));
        assertNull(compassTarget.acceptChange(ChangeMode.ADD));

        Object player = new Object();
        FabricLocation target = new FabricLocation(null, new Vec3(8.0D, 64.0D, -2.0D));

        assertNull(CompassTargetExpressionSupport.get(player));
        CompassTargetExpressionSupport.set(player, target);
        assertEquals(target, CompassTargetExpressionSupport.get(player));
        CompassTargetExpressionSupport.clear(player);
        assertNull(CompassTargetExpressionSupport.get(player));
    }

    @Test
    void portalExpressionRequiresPortalEventAndReturnsFabricBlocks() {
        assertInstanceOf(ExprPortal.class, new ExprPortal());
        assertEquals(FabricBlock.class, new ExprPortal().getReturnType());

        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("portal", ch.njol.skript.events.FabricEventCompatHandles.Portal.class);
            ExprPortal portal = new ExprPortal();
            assertTrue(portal.init(new Expression<?>[0], 0, ch.njol.util.Kleenean.FALSE, new SkriptParser.ParseResult()));
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }

        ExprPortal portal = new ExprPortal();
        assertFalse(portal.init(new Expression<?>[0], 0, ch.njol.util.Kleenean.FALSE, new SkriptParser.ParseResult()));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(MinecraftServer.class, "server");
        new ExprMaxMinecartSpeed();
        new ExprMinecartDerailedFlyingVelocity();
        new ExprCompassTarget();
        new ExprPortal();
        LitConsole.register();
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        ClassInfo<T> existing = Classes.getExactClassInfo(type);
        if (existing == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Expression<? extends T> parse(String input, Class<T> type) {
        return (Expression<? extends T>) new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(new Class[]{type});
    }

    private static void restoreEventContext(ParserInstance parser, @Nullable String previousEventName, Class<?>[] previousEventClasses) {
        if (previousEventName == null) {
            parser.deleteCurrentEvent();
        } else {
            parser.setCurrentEvent(previousEventName, previousEventClasses);
        }
    }
}
