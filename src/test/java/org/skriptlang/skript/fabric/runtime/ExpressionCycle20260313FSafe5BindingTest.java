package org.skriptlang.skript.fabric.runtime;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.ExprEnchantmentLevel;
import ch.njol.skript.expressions.ExprEnchantments;
import ch.njol.skript.expressions.ExprMe;
import ch.njol.skript.expressions.ExprSkullOwner;
import ch.njol.skript.expressions.ExprTypeOf;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Tag("isolated-registry")
final class ExpressionCycle20260313FSafe5BindingTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @AfterEach
    void cleanupParserState() {
        ParserInstance.get().deleteCurrentEvent();
    }

    @Test
    void safe5ItemExpressionsParseThroughBootstrap() {
        assertInstanceOf(ExprEnchantmentLevel.class, parseExpression("lane-safe5-enchantment level of lane-safe5-item", Long.class));
        assertInstanceOf(ExprEnchantments.class, parseExpression("enchantments of lane-safe5-item", Enchantment.class));
        assertInstanceOf(ExprTypeOf.class, parseExpression("type of lane-safe5-itemstack", FabricItemType.class));
        assertInstanceOf(ExprSkullOwner.class, parseExpression("skull owner of lane-safe5-skull-item", GameProfile.class));
    }

    @Test
    void meParsesInCommandContextThroughBootstrap() throws Exception {
        ParserInstance.get().setCurrentEvent("command", Class.forName("ch.njol.skript.events.FabricPlayerEventHandles$Command"));
        assertInstanceOf(ExprMe.class, parseExpression("me", ServerPlayer.class));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        Skript.registerExpression(TestItemExpression.class, FabricItemType.class, "lane-safe5-item");
        Skript.registerExpression(TestEnchantmentExpression.class, Enchantment.class, "lane-safe5-enchantment");
        Skript.registerExpression(TestItemStackExpression.class, ItemStack.class, "lane-safe5-itemstack");
        Skript.registerExpression(TestSkullItemExpression.class, FabricItemType.class, "lane-safe5-skull-item");
        syntaxRegistered = true;
    }

    private static Expression<?> parseExpression(String input, Class<?>... returnTypes) {
        Expression<?> parsed = new SkriptParser(input, SkriptParser.ALL_FLAGS, ParseContext.DEFAULT).parseExpression(returnTypes);
        assertNotNull(parsed, input);
        return parsed;
    }

    public static final class TestItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }

    public static final class TestEnchantmentExpression extends SimpleExpression<Enchantment> {
        @Override
        protected Enchantment @Nullable [] get(SkriptEvent event) {
            return new Enchantment[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends Enchantment> getReturnType() {
            return Enchantment.class;
        }
    }

    public static final class TestItemStackExpression extends SimpleExpression<ItemStack> {
        @Override
        protected ItemStack @Nullable [] get(SkriptEvent event) {
            return new ItemStack[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends ItemStack> getReturnType() {
            return ItemStack.class;
        }
    }

    public static final class TestSkullItemExpression extends SimpleExpression<FabricItemType> {
        @Override
        protected FabricItemType @Nullable [] get(SkriptEvent event) {
            return new FabricItemType[0];
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricItemType> getReturnType() {
            return FabricItemType.class;
        }
    }
}
