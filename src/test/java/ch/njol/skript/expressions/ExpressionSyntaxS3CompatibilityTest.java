package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionSyntaxS3CompatibilityTest {

    private static boolean syntaxRegistered;
    private static List<SyntaxInfo<?>> originalExpressions = List.of();

    @BeforeAll
    static void bootstrapSyntax() {
        TestBootstrap.bootstrap();
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

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(GameProfile.class, "offlineplayer");
        registerClassInfo(ServerLevel.class, "world");
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(AbstractArrow.class, "projectile");
        registerClassInfo(Vec3.class, "vector");
        registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
        registerClassInfo(UUID.class, "uuid");
        registerEnumClassInfo(GameType.class, "gamemode", "gamemodes");
        Skript.registerExpression(TestOfflinePlayerExpression.class, GameProfile.class, "lane-s3-test-offlineplayer");
        Skript.registerExpression(TestWorldExpression.class, ServerLevel.class, "lane-s3-test-world");
        Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-s3-test-player");
        Skript.registerExpression(TestEntityExpression.class, Entity.class, "lane-s3-test-entity");
        Skript.registerExpression(TestProjectileExpression.class, AbstractArrow.class, "lane-s3-test-projectile");
        new ExprUUID();
        new ExprVelocity();
        new ExprTimeLived();
        new ExprScoreboardTags();
        new ExprGameMode();
        new ExprSaturation();
        syntaxRegistered = true;
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    private static <T extends Enum<T>> void registerEnumClassInfo(Class<T> type, String codeName, String languageNode) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new EnumClassInfo<>(type, codeName, languageNode));
        }
    }

    @Test
    void syntaxS3ExpressionsParseWithAssignedSources() throws Exception {
        assertInstanceOf(ExprUUID.class, parseExpression("uuid of lane-s3-test-offlineplayer", UUID.class));
        assertInstanceOf(ExprUUID.class, parseExpression("uuid of lane-s3-test-entity", UUID.class));
        assertInstanceOf(ExprUUID.class, parseExpression("uuid of lane-s3-test-world", UUID.class));
        assertInstanceOf(ExprVelocity.class, parseExpression("velocity of lane-s3-test-entity", Vec3.class));
        assertInstanceOf(ExprTimeLived.class, parseExpression("time lived of lane-s3-test-entity", ch.njol.skript.util.Timespan.class));
        assertInstanceOf(ExprScoreboardTags.class, parseExpression("scoreboard tags of lane-s3-test-entity", String.class));
        assertInstanceOf(ExprGameMode.class, parseExpression("game mode of lane-s3-test-player", GameType.class));
        assertInstanceOf(ExprSaturation.class, parseExpression("saturation of lane-s3-test-player", Number.class));

        Statement velocity = parseStatement("set velocity of lane-s3-test-entity to vector(1, 0, 0)");
        assertInstanceOf(EffChange.class, velocity);
        assertInstanceOf(ExprVelocity.class, expression(velocity, "changed"));

        Statement timeLived = parseStatement("add 2 seconds to time lived of lane-s3-test-entity");
        assertInstanceOf(EffChange.class, timeLived);
        assertInstanceOf(ExprTimeLived.class, expression(timeLived, "changed"));

        Statement scoreboardTags = parseStatement("add \"lane-s3\" to scoreboard tags of lane-s3-test-entity");
        assertInstanceOf(EffChange.class, scoreboardTags);
        assertInstanceOf(ExprScoreboardTags.class, expression(scoreboardTags, "changed"));

        Statement gameMode = parseStatement("set game mode of lane-s3-test-player to creative");
        assertInstanceOf(EffChange.class, gameMode);
        assertInstanceOf(ExprGameMode.class, expression(gameMode, "changed"));

        Statement saturation = parseStatement("set saturation of lane-s3-test-player to 5");
        assertInstanceOf(EffChange.class, saturation);
        assertInstanceOf(ExprSaturation.class, expression(saturation, "changed"));
    }

    @Test
    void uuidExpressionUsesProfileAndWorldInputs() {
        ExprUUID expression = new ExprUUID();
        UUID offlinePlayerUuid = UUID.randomUUID();
        GameProfile profile = new GameProfile(offlinePlayerUuid, "LaneS3");
        assertEquals(offlinePlayerUuid, expression.convert(profile));
    }

    private Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private Statement parseStatement(String input) {
        return Statement.parse(input, "failed");
    }

    private Expression<?> expression(Object owner, String fieldName) throws Exception {
        Field field = findField(owner.getClass(), fieldName);
        field.setAccessible(true);
        Object value = field.get(owner);
        assertInstanceOf(Expression.class, value);
        return (Expression<?>) value;
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

    public static final class TestOfflinePlayerExpression extends SimpleExpression<GameProfile> {
        @Override
        protected GameProfile @Nullable [] get(SkriptEvent event) {
            return new GameProfile[]{new GameProfile(UUID.randomUUID(), "LaneS3")};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends GameProfile> getReturnType() {
            return GameProfile.class;
        }
    }

    public static final class TestWorldExpression extends SimpleExpression<ServerLevel> {
        @Override
        protected ServerLevel @Nullable [] get(SkriptEvent event) {
            return new ServerLevel[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerLevel> getReturnType() {
            return ServerLevel.class;
        }
    }

    public static final class TestPlayerExpression extends SimpleExpression<ServerPlayer> {
        @Override
        protected ServerPlayer @Nullable [] get(SkriptEvent event) {
            return new ServerPlayer[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ServerPlayer> getReturnType() {
            return ServerPlayer.class;
        }
    }

    public static final class TestEntityExpression extends SimpleExpression<Entity> {
        @Override
        protected Entity @Nullable [] get(SkriptEvent event) {
            return new Entity[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Entity> getReturnType() {
            return Entity.class;
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
