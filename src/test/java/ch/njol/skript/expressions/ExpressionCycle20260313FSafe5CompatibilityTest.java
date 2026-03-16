package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import sun.misc.Unsafe;

final class ExpressionCycle20260313FSafe5CompatibilityTest {

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

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void expressionsParseAgainstLaneLocalSources() throws Exception {
        Class<?> commandEventClass = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        ParserInstance.get().setCurrentEvent("command", commandEventClass);

        assertInstanceOf(ExprEnchantmentLevel.class, parseExpression("lane-safe5-enchant level of lane-safe5-item", Long.class));
        assertInstanceOf(ExprEnchantments.class, parseExpression("enchantments of lane-safe5-item", Enchantment.class));
        assertInstanceOf(ExprTypeOf.class, parseExpression("type of lane-safe5-itemstack", FabricItemType.class));
        assertInstanceOf(ExprSkullOwner.class, parseExpression("skull owner of lane-safe5-skull-item", GameProfile.class));
        assertInstanceOf(ExprMe.class, parseExpression("me", ServerPlayer.class));
    }

    @Test
    void enchantmentLevelAndTypeOfWorkOnCompatItems() {
        Enchantment sharpness = enchantment("sharpness");
        ItemStack swordStack = new ItemStack(Items.DIAMOND_SWORD);
        swordStack.enchant(net.minecraft.core.Holder.direct(sharpness), 2);
        FabricItemType sword = new FabricItemType(swordStack);

        ExprEnchantmentLevel level = new ExprEnchantmentLevel();
        level.init(
                new Expression[]{
                        new ch.njol.skript.lang.util.SimpleLiteral<>(sharpness, false),
                        new ch.njol.skript.lang.util.SimpleLiteral<>(sword, false)
                },
                1,
                Kleenean.FALSE,
                parseResult("")
        );
        assertArrayEquals(new Long[]{2L}, level.getArray(SkriptEvent.EMPTY));
        level.change(SkriptEvent.EMPTY, new Object[]{3}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertArrayEquals(new Long[]{3L}, level.getArray(SkriptEvent.EMPTY));

        ExprTypeOf typeOf = new ExprTypeOf();
        typeOf.init(new Expression[]{new ch.njol.skript.lang.util.SimpleLiteral<>(sword.toStack(), false)}, 0, Kleenean.FALSE, parseResult(""));
        Object type = typeOf.getSingle(SkriptEvent.EMPTY);
        assertNotNull(type);
        assertInstanceOf(FabricItemType.class, type);
        assertEquals(Items.DIAMOND_SWORD, ((FabricItemType) type).item());
    }

    @Test
    void meExpressionReadsCommandPlayerContext() throws Exception {
        ServerPlayer player = allocate(ServerPlayer.class);
        ExprMe me = new ExprMe();
        ParserInstance.get().setCurrentEvent("command", Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command"));
        assertTrue(me.init(new Expression[0], 0, Kleenean.FALSE, parseResult("me")));
        assertSame(player, me.getSingle(new SkriptEvent(newCommandHandle("/say hi"), null, null, player)));
    }

    @Test
    void enchantmentsAndSkullOwnerWorkOnCompatItems() {
        Enchantment sharpness = enchantment("sharpness");
        Enchantment unbreaking = enchantment("unbreaking");

        ItemStack swordStack = new ItemStack(Items.DIAMOND_SWORD);
        swordStack.enchant(net.minecraft.core.Holder.direct(sharpness), 2);
        swordStack.enchant(net.minecraft.core.Holder.direct(unbreaking), 1);
        FabricItemType sword = new FabricItemType(swordStack);

        ExprEnchantments enchantments = new ExprEnchantments();
        enchantments.init(new Expression[]{new ch.njol.skript.lang.util.SimpleLiteral<>(sword, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(Set.of(sharpness, unbreaking), Set.of(enchantments.getArray(SkriptEvent.EMPTY)));
        enchantments.change(SkriptEvent.EMPTY, new Object[]{sharpness}, ch.njol.skript.classes.Changer.ChangeMode.REMOVE);
        assertEquals(Set.of(unbreaking), Set.of(enchantments.getArray(SkriptEvent.EMPTY)));

        GameProfile firstOwner = new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000f5"), "safe5-a");
        ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);
        headStack.set(DataComponents.PROFILE, new ResolvableProfile(firstOwner));
        FabricItemType head = new FabricItemType(headStack);

        ExprSkullOwner skullOwner = new ExprSkullOwner();
        skullOwner.init(new Expression[]{new ch.njol.skript.lang.util.SimpleLiteral<>(head, false)}, 0, Kleenean.FALSE, parseResult(""));
        GameProfile resolvedOwner = skullOwner.getSingle(SkriptEvent.EMPTY);
        assertNotNull(resolvedOwner);
        assertEquals(firstOwner.getId(), resolvedOwner.getId());
        assertEquals(firstOwner.getName(), resolvedOwner.getName());

        GameProfile nextOwner = new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000f6"), "safe5-b");
        skullOwner.change(SkriptEvent.EMPTY, new Object[]{nextOwner}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        ResolvableProfile updatedProfile = head.toStack().get(DataComponents.PROFILE);
        assertNotNull(updatedProfile);
        assertEquals(nextOwner.getId(), updatedProfile.gameProfile().getId());
        assertEquals(nextOwner.getName(), updatedProfile.gameProfile().getName());
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(ServerPlayer.class, "player");
        registerClassInfo(GameProfile.class, "offlineplayer");
        if (Classes.getClassInfoNoError("enchantment") == null) {
            Classes.registerClassInfo(new ClassInfo<>(Enchantment.class, "enchantment").user("enchantments?"));
        }
        Skript.registerExpression(TestItemExpression.class, FabricItemType.class, "lane-safe5-item");
        Skript.registerExpression(TestEnchantmentExpression.class, Enchantment.class, "lane-safe5-enchant");
        Skript.registerExpression(TestItemStackExpression.class, ItemStack.class, "lane-safe5-itemstack");
        Skript.registerExpression(TestSkullItemExpression.class, FabricItemType.class, "lane-safe5-skull-item");
        new ExprEnchantmentLevel();
        new ExprEnchantments();
        new ExprTypeOf();
        new ExprSkullOwner();
        new ExprMe();
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

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }

    private static Enchantment enchantment(String path) {
        var registry = VanillaRegistries.createLookup().lookupOrThrow(Registries.ENCHANTMENT);
        return registry.getOrThrow(switch (path) {
            case "sharpness" -> Enchantments.SHARPNESS;
            case "unbreaking" -> Enchantments.UNBREAKING;
            default -> throw new IllegalArgumentException(ResourceLocation.withDefaultNamespace(path).toString());
        }).value();
    }

    private static Object newCommandHandle(String command) throws ReflectiveOperationException {
        Class<?> type = Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command");
        Constructor<?> constructor = type.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(command);
    }

    @SuppressWarnings("unchecked")
    private static <T> T allocate(Class<T> type) throws ReflectiveOperationException {
        return (T) unsafe().allocateInstance(type);
    }

    private static Unsafe unsafe() throws ReflectiveOperationException {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    public static final class TestItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[]{new FabricItemType(Items.DIAMOND_SWORD)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-safe5-item";
        }
    }

    public static final class TestEnchantmentExpression extends SimpleExpression<Enchantment> {
        @Override
        protected Enchantment @Nullable [] get(SkriptEvent event) {
            return new Enchantment[]{enchantment("sharpness")};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Enchantment> getReturnType() {
            return Enchantment.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-safe5-enchant";
        }
    }

    public static final class TestItemStackExpression extends SimpleExpression<ItemStack> {
        @Override
        protected ItemStack @Nullable [] get(SkriptEvent event) {
            return new ItemStack[]{new ItemStack(Items.DIAMOND_SWORD)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ItemStack> getReturnType() {
            return ItemStack.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-safe5-itemstack";
        }
    }

    public static final class TestSkullItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
            stack.set(
                    DataComponents.PROFILE,
                    new ResolvableProfile(new GameProfile(UUID.fromString("00000000-0000-0000-0000-0000000000f5"), "safe5-a"))
            );
            return new FabricItemType[]{new FabricItemType(stack)};
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }

        @Override
        public String toString(@Nullable SkriptEvent event, boolean debug) {
            return "lane-safe5-skull-item";
        }
    }
}
