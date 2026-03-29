package ch.njol.skript.expressions;

import ch.njol.skript.test.TestBootstrap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.mojang.authlib.GameProfile;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricBlock;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.fabric.runtime.SkriptFabricBootstrap;
import org.skriptlang.skript.lang.event.SkriptEvent;

final class ExpressionCycle20260312ESyntax2CompatibilityTest {

    private static boolean syntaxRegistered;

    @BeforeAll
    static void bootstrapMinecraft() {
        TestBootstrap.bootstrap();
        SkriptFabricBootstrap.bootstrap();
        ensureSyntax();
    }

    @Test
    void parserBindsRecoveredSyntaxBatch() {
        assertInstanceOf(ExprSeaPickles.class, parseExpression("sea pickle count of lane-e2-block", Integer.class));
        assertInstanceOf(ExprPandaGene.class, parseExpression("main gene of lane-e2-livingentity", net.minecraft.world.entity.animal.panda.Panda.Gene.class));
        assertInstanceOf(ExprPlain.class, parseExpression("plain lane-e2-itemtype", FabricItemType.class));
        assertInstanceOf(ExprNamed.class, parseExpression("lane-e2-itemtype named \"Lane E\"", FabricItemType.class));
        assertInstanceOf(ExprWithItemFlags.class, parseExpression("lane-e2-itemtype with item flags \"hide enchants\"", FabricItemType.class));
        assertInstanceOf(ExprCarryingBlockData.class, parseExpression("carried blockdata of lane-e2-livingentity", BlockState.class));
        assertInstanceOf(ExprWardenAngryAt.class, parseExpression("most angered entity of lane-e2-livingentity", LivingEntity.class));
        assertInstanceOf(ExprWardenEntityAnger.class, parseExpression("anger level of lane-e2-livingentity towards lane-e2-livingentity", Integer.class));
    }

    @Disabled("Moved to GameTest")
    @Test
    void plainNamedAndItemFlagsProduceDerivedItemTypes() {
        FabricItemType base = new FabricItemType(new ItemStack(Items.DIAMOND_SWORD));

        ExprPlain plain = new ExprPlain();
        assertTrue(plain.init(new Expression[]{new SimpleLiteral<>(base, false)}, 0, Kleenean.FALSE, parseResult("")));
        FabricItemType plainType = plain.getSingle(SkriptEvent.EMPTY);
        assertNotNull(plainType);
        assertEquals(Items.DIAMOND_SWORD, plainType.item());
        assertEquals(null, plainType.name());

        ExprNamed named = new ExprNamed();
        assertTrue(named.init(
                new Expression[]{new SimpleLiteral<>(base, false), new SimpleLiteral<>("Lane E Sword", false)},
                0,
                Kleenean.FALSE,
                parseResult("")
        ));
        FabricItemType namedType = named.getSingle(SkriptEvent.EMPTY);
        assertNotNull(namedType);
        assertEquals("Lane E Sword", namedType.name());

        ExprWithItemFlags flags = new ExprWithItemFlags();
        assertTrue(flags.init(
                new Expression[]{new SimpleLiteral<>(base, false), new SimpleLiteral<>("hide enchants", false)},
                0,
                Kleenean.FALSE,
                parseResult("")
        ));
        FabricItemType flagged = flags.getSingle(SkriptEvent.EMPTY);
        assertNotNull(flagged);
        TooltipDisplay display = flagged.toStack().get(DataComponents.TOOLTIP_DISPLAY);
        assertNotNull(display);
        assertTrue(display.hiddenComponents().contains(DataComponents.ENCHANTMENTS));
    }

    @Test
    void seaPickleHelperReadsCountsFromState() {
        BlockState one = Blocks.SEA_PICKLE.defaultBlockState().setValue(Blocks.SEA_PICKLE.defaultBlockState().getBlock() instanceof net.minecraft.world.level.block.SeaPickleBlock ? net.minecraft.world.level.block.SeaPickleBlock.PICKLES : BlockStateProperties.PICKLES, 1);
        BlockState three = one.setValue(net.minecraft.world.level.block.SeaPickleBlock.PICKLES, 3);

        assertEquals(1, ExprSeaPickles.readPickleCount(one, false, false));
        assertEquals(3, ExprSeaPickles.readPickleCount(three, false, false));
        assertEquals(1, ExprSeaPickles.readPickleCount(three, true, false));
        assertEquals(4, ExprSeaPickles.readPickleCount(three, false, true));
    }

    private static void ensureSyntax() {
        if (syntaxRegistered) {
            return;
        }
        registerClassInfo(FabricBlock.class, "block");
        registerClassInfo(FabricItemType.class, "itemtype");
        registerClassInfo(LivingEntity.class, "livingentity");
        registerClassInfo(Entity.class, "entity");
        registerClassInfo(BlockState.class, "blockdata");
        registerClassInfo(net.minecraft.world.entity.animal.panda.Panda.Gene.class, "gene");
        new ExprSeaPickles();
        new ExprPandaGene();
        new ExprPlain();
        new ExprNamed();
        new ExprWithItemFlags();
        new ExprCarryingBlockData();
        new ExprWardenAngryAt();
        new ExprWardenEntityAnger();

        Skript.registerExpression(TestBlockExpression.class, FabricBlock.class, "lane-e2-block");
        Skript.registerExpression(TestItemTypeExpression.class, FabricItemType.class, "lane-e2-itemtype");
        Skript.registerExpression(TestLivingEntityExpression.class, LivingEntity.class, "lane-e2-livingentity");
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

    public static final class TestBlockExpression extends SimpleExpression<FabricBlock> {
        @Override
        protected FabricBlock @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends FabricBlock> getReturnType() {
            return FabricBlock.class;
        }
    }

    public static final class TestItemTypeExpression extends SimpleExpression<FabricItemType> {
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
    }

    public static final class TestLivingEntityExpression extends SimpleExpression<LivingEntity> {
        @Override
        protected LivingEntity @Nullable [] get(SkriptEvent event) {
            return null;
        }

        @Override
        public boolean isSingle() {
            return true;
        }

        @Override
        public Class<? extends LivingEntity> getReturnType() {
            return LivingEntity.class;
        }
    }
}
