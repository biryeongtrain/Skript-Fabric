package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.Statement;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.bukkit.base.effects.EffChange;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.compat.FabricLocation;
import org.skriptlang.skript.fabric.runtime.FabricUseEntityHandle;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Tag("isolated-registry")
final class ExpressionSyntaxS2CompatibilityTest {

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

    private static void ensureSyntax() {
        if (!syntaxRegistered) {
            registerClassInfo(Entity.class, "entity");
            registerClassInfo(ServerPlayer.class, "player");
            registerClassInfo(FabricBlock.class, "block");
            registerClassInfo(FabricLocation.class, "location");
            registerClassInfo(FabricInventory.class, "inventory");
            registerClassInfo(FabricItemType.class, "itemtype");
            registerClassInfo(ItemEntity.class, "itementity");
            registerClassInfo(ch.njol.skript.util.Timespan.class, "timespan");
            Skript.registerExpression(TestCommandBlockExpression.class, FabricBlock.class, "lane-e-test-command-block");
            Skript.registerExpression(TestCommandMinecartExpression.class, MinecartCommandBlock.class, "lane-e-test-command-minecart");
            Skript.registerExpression(TestPlayerExpression.class, ServerPlayer.class, "lane-e-test-player");
            Skript.registerExpression(TestItemEntityExpression.class, ItemEntity.class, "lane-e-test-itementity");
            syntaxRegistered = true;
        }
        new ExprItemCooldown();
        new ExprCommandBlockCommand();
        new ExprItemFlags();
        new ExprItemOwner();
        new ExprItemThrower();
        new ExprLevel();
        new ExprMaxDurability();
        new ExprMaxItemUseTime();
        new ExprMaxStack();
        new ExprRawName();
        new ExprSpeed();
    }

    private static <T> void registerClassInfo(Class<T> type, String codeName) {
        if (Classes.getExactClassInfo(type) == null) {
            Classes.registerClassInfo(new ClassInfo<>(type, codeName));
        }
    }

    @Test
    void itemCooldownExpressionParsesAndBindsAsChangeTarget() throws Exception {
        Expression<?> expression = parseExpressionInEvent(
                "item cooldown of diamond for event-player",
                new Class[]{ch.njol.skript.util.Timespan.class},
                FabricUseEntityHandle.class
        );
        assertInstanceOf(ExprItemCooldown.class, expression);

        Statement statement = parseStatementInEvent("set item cooldown of diamond for event-player to 2 seconds", FabricUseEntityHandle.class);
        assertInstanceOf(EffChange.class, statement);
        assertInstanceOf(ExprItemCooldown.class, expression(statement, "changed"));
    }

    @Test
    void commandBlockCommandExpressionParsesForBlocksAndEntities() throws Exception {
        assertInstanceOf(ExprCommandBlockCommand.class, parseExpression("command of lane-e-test-command-block", String.class));
        assertInstanceOf(ExprCommandBlockCommand.class, parseExpression("command of lane-e-test-command-minecart", String.class));

        Statement blockStatement = parseStatement("set command of lane-e-test-command-block to \"say lane e\"");
        assertInstanceOf(EffChange.class, blockStatement);
        assertEquals("command block command of lane-e-test-command-block",
                expression(blockStatement, "changed").toString(null, false));

        Statement minecartStatement = parseStatement("reset command of lane-e-test-command-minecart");
        assertInstanceOf(EffChange.class, minecartStatement);
        assertEquals("command block command of lane-e-test-command-minecart",
                expression(minecartStatement, "changed").toString(null, false));
    }

    @Test
    void importedPropertyExpressionsParseAndBindAsChangeTargets() throws Exception {
        assertInstanceOf(ExprLevel.class, parseExpression("xp level of lane-e-test-player", Long.class));
        assertInstanceOf(ExprSpeed.class, parseExpression("walk speed of lane-e-test-player", Number.class));
        assertInstanceOf(ExprRawName.class, parseExpression("raw name of diamond sword", String.class));
        assertInstanceOf(ExprItemFlags.class, parseExpression("item flags of diamond sword", String.class));
        assertInstanceOf(ExprMaxDurability.class, parseExpression("max durability of diamond sword", Integer.class));
        assertInstanceOf(ExprMaxItemUseTime.class, parseExpression("maximum item use duration of potion", ch.njol.skript.util.Timespan.class));
        assertInstanceOf(ExprMaxStack.class, parseExpression("max stack size of diamond", Integer.class));
        assertInstanceOf(ExprItemOwner.class, parseExpression("uuid of dropped item owner of lane-e-test-itementity", UUID.class));
        assertInstanceOf(ExprItemThrower.class, parseExpression("uuid of dropped item thrower of lane-e-test-itementity", UUID.class));

        Statement walkSpeed = parseStatement("set walking speed of lane-e-test-player to 0.4");
        assertInstanceOf(EffChange.class, walkSpeed);
        assertEquals("walk speed of lane-e-test-player", expression(walkSpeed, "changed").toString(null, false));

        Statement itemFlags = parseStatement("set item flags of diamond sword to \"hide enchants\"");
        assertInstanceOf(EffChange.class, itemFlags);
        assertEquals("item flags of diamond sword", expression(itemFlags, "changed").toString(null, false));

        Statement itemOwner = parseStatement("set uuid of dropped item owner of lane-e-test-itementity to lane-e-test-player");
        assertInstanceOf(EffChange.class, itemOwner);
        assertEquals("uuid of the dropped item owner of lane-e-test-itementity", expression(itemOwner, "changed").toString(null, false));
    }

    private Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT)
                .parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    private Expression<?> parseExpressionInEvent(String input, Class<?>[] returnTypes, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseExpression(input, returnTypes);
        } finally {
            restoreEventContext(parser, previousEventName, previousEventClasses);
        }
    }

    private Statement parseStatement(String input) {
        return Statement.parse(input, "failed");
    }

    private Statement parseStatementInEvent(String input, Class<?>... eventClasses) {
        ParserInstance parser = ParserInstance.get();
        String previousEventName = parser.getCurrentEventName();
        Class<?>[] previousEventClasses = parser.getCurrentEventClasses();
        try {
            parser.setCurrentEvent("gametest", eventClasses);
            return parseStatement(input);
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

    public static final class TestCommandBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return new FabricBlock[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-command-block";
        }
    }

    public static final class TestCommandMinecartExpression extends SimpleExpression<MinecartCommandBlock> {
        @Override
        protected MinecartCommandBlock @Nullable [] get(SkriptEvent event) {
            return new MinecartCommandBlock[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends MinecartCommandBlock> getReturnType() {
            return MinecartCommandBlock.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-command-minecart";
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

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-player";
        }
    }

    public static final class TestItemEntityExpression extends SimpleExpression<ItemEntity> {
        @Override
        protected ItemEntity @Nullable [] get(SkriptEvent event) {
            return new ItemEntity[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ItemEntity> getReturnType() {
            return ItemEntity.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-e-test-itementity";
        }
    }
}
