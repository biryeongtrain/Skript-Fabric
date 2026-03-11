package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

final class ExpressionCycle20260312Syntax4CompatibilityTest {

    private static boolean syntaxRegistered;
    private static List<SyntaxInfo<?>> originalExpressions = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        originalExpressions = new ArrayList<>();
        for (SyntaxInfo<?> info : Skript.instance().syntaxRegistry().syntaxes(SyntaxRegistry.EXPRESSION)) {
            originalExpressions.add(info);
        }
        ensureSyntax();
    }

    @AfterAll
    static void restoreSyntax() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
        for (SyntaxInfo<?> info : originalExpressions) {
            Skript.instance().syntaxRegistry().register(SyntaxRegistry.EXPRESSION, info);
        }
    }

    @Test
    void parseCoverageIncludesLandedSyntax4Slice() throws Exception {
        assertInstanceOf(ExprFromUUID.class, parseExpression("offline player from lane-c20260312-s4-uuid", GameProfile.class));
        assertInstanceOf(ExprFromUUID.class, parseExpression("entity from lane-c20260312-s4-uuid", Entity.class));
        assertInstanceOf(ExprFromUUID.class, parseExpression("world from lane-c20260312-s4-uuid", ServerLevel.class));
        assertInstanceOf(ExprMemory.class, parseExpression("free memory", Double.class));
        assertInstanceOf(ExprMemory.class, parseExpression("maximum ram", Double.class));
        assertInstanceOf(ExprProjectileCriticalState.class, parseExpression(
                "projectile critical state of lane-c20260312-s4-projectile",
                Boolean.class
        ));
        assertInstanceOf(ExprAllBannedEntries.class, parseExpression("all banned players", GameProfile.class));
        assertInstanceOf(ExprAllBannedEntries.class, parseExpression("all banned ip addresses", String.class));

        Statement change = Statement.parse("set projectile critical state of lane-c20260312-s4-projectile to true", "failed");
        assertInstanceOf(EffChange.class, change);
        assertInstanceOf(ExprProjectileCriticalState.class, expression(change, "changed"));
    }

    @Test
    void fromUuidSupportsOfflineFallbackAndServerFreeMisses() {
        UUID uuid = UUID.randomUUID();

        ExprFromUUID offlinePlayers = new ExprFromUUID();
        SkriptParser.ParseResult offlineResult = parseResult("offline player from lane-c20260312-s4-uuid");
        offlineResult.tags.add("offline");
        assertTrue(offlinePlayers.init(new Expression[]{new SimpleLiteral<>(uuid, false)}, 0, Kleenean.FALSE, offlineResult));
        GameProfile profile = assertInstanceOf(GameProfile.class, offlinePlayers.getSingle(SkriptEvent.EMPTY));
        assertEquals(uuid, profile.getId());
        assertEquals(GameProfile.class, offlinePlayers.getReturnType());

        ExprFromUUID entities = new ExprFromUUID();
        assertTrue(entities.init(new Expression[]{new SimpleLiteral<>(uuid, false)}, 1, Kleenean.FALSE, parseResult("entity from lane-c20260312-s4-uuid")));
        assertNull(entities.getSingle(SkriptEvent.EMPTY));

        ExprFromUUID worlds = new ExprFromUUID();
        assertTrue(worlds.init(new Expression[]{new SimpleLiteral<>(uuid, false)}, 2, Kleenean.FALSE, parseResult("world from lane-c20260312-s4-uuid")));
        assertNull(worlds.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void memoryAndBanExpressionsExposeExpectedTypes() {
        ExprMemory free = new ExprMemory();
        SkriptParser.ParseResult freeResult = parseResult("free memory");
        freeResult.tags.add("free");
        assertTrue(free.init(new Expression[0], 0, Kleenean.FALSE, freeResult));
        assertNotNull(free.getSingle(SkriptEvent.EMPTY));
        assertTrue(free.getSingle(SkriptEvent.EMPTY) > 0.0D);
        assertEquals("free memory", free.toString(SkriptEvent.EMPTY, false));

        ExprAllBannedEntries players = new ExprAllBannedEntries();
        assertTrue(players.init(new Expression[0], 0, Kleenean.FALSE, parseResult("all banned players")));
        assertEquals(GameProfile.class, players.getReturnType());
        assertArrayEquals(new GameProfile[0], players.getArray(SkriptEvent.EMPTY));

        ExprAllBannedEntries ips = new ExprAllBannedEntries();
        SkriptParser.ParseResult ipResult = parseResult("all banned ip addresses");
        ipResult.tags.add("ips");
        assertTrue(ips.init(new Expression[0], 0, Kleenean.FALSE, ipResult));
        assertEquals(String.class, ips.getReturnType());
        assertArrayEquals(new String[0], ips.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void projectileCriticalStateAcceptsSetOnly() {
        ExprProjectileCriticalState expression = new ExprProjectileCriticalState();
        assertArrayEquals(new Class[]{Boolean.class}, expression.acceptChange(ChangeMode.SET));
        assertNull(expression.acceptChange(ChangeMode.ADD));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(UUID.class, "uuid");
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(AbstractArrow.class, "projectile");
        Skript.registerExpression(TestUuidExpression.class, UUID.class, "lane-c20260312-s4-uuid");
        Skript.registerExpression(TestProjectileExpression.class, AbstractArrow.class, "lane-c20260312-s4-projectile");
        new ExprFromUUID();
        new ExprMemory();
        new ExprProjectileCriticalState();
        new ExprAllBannedEntries();
        syntaxRegistered = true;
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

    private static SkriptParser.ParseResult parseResult(String expression) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expression;
        return result;
    }

    private static Expression<?> expression(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        Object value = field.get(owner);
        return assertInstanceOf(Expression.class, value);
    }

    private static Field findField(Class<?> owner, String fieldName) throws NoSuchFieldException {
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

    public static final class TestUuidExpression extends SimpleExpression<UUID> {
        @Override
        protected UUID @Nullable [] get(SkriptEvent event) {
            return new UUID[]{UUID.randomUUID()};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends UUID> getReturnType() {
            return UUID.class;
        }
    }

    public static final class TestProjectileExpression extends SimpleExpression<AbstractArrow> {
        @Override
        protected AbstractArrow @Nullable [] get(SkriptEvent event) {
            return new AbstractArrow[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends AbstractArrow> getReturnType() {
            return AbstractArrow.class;
        }
    }
}
