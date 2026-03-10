package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.skript.util.Timespan;

class ExpressionItemCompatibilityTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @AfterEach
    void cleanupRegistry() {
        Skript.instance().syntaxRegistry().clear(SyntaxRegistry.EXPRESSION);
    }

    @Test
    void blockHardnessUsesUnderlyingBlockDestroyTime() {
        ExprBlockHardness hardness = new ExprBlockHardness();
        assertEquals(1.5F, hardness.convert(new FabricItemType(Items.STONE)));
    }

    @Test
    void bookAuthorTitleAndPagesReadAndChangeWrittenContent() {
        ItemStack writtenBook = writtenBook("Lane E", "Codex", "one", "two");

        ExprBookAuthor author = new ExprBookAuthor();
        author.init(new Expression[]{new SimpleLiteral<>(writtenBook, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("Codex", author.getSingle(SkriptEvent.EMPTY));
        author.change(SkriptEvent.EMPTY, new Object[]{"Surface"}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals("Surface", writtenBook.get(DataComponents.WRITTEN_BOOK_CONTENT).author());

        ExprBookTitle title = new ExprBookTitle();
        title.init(new Expression[]{new SimpleLiteral<>(writtenBook, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals("Lane E", title.getSingle(SkriptEvent.EMPTY));
        title.change(SkriptEvent.EMPTY, new Object[]{"Merged"}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals("Merged", writtenBook.get(DataComponents.WRITTEN_BOOK_CONTENT).title().raw());

        ExprBookPages allPages = new ExprBookPages();
        allPages.init(new Expression[]{new SimpleLiteral<>(writtenBook, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"one", "two"}, allPages.getArray(SkriptEvent.EMPTY));
        allPages.change(SkriptEvent.EMPTY, new Object[]{"three"}, ch.njol.skript.classes.Changer.ChangeMode.ADD);
        assertArrayEquals(new String[]{"one", "two", "three"}, allPages.getArray(SkriptEvent.EMPTY));

        ExprBookPages singlePage = new ExprBookPages();
        singlePage.init(new Expression[]{
                new SimpleLiteral<>(2, false),
                new SimpleLiteral<>(writtenBook, false)
        }, 2, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"two"}, singlePage.getArray(SkriptEvent.EMPTY));
        singlePage.change(SkriptEvent.EMPTY, new Object[]{"updated"}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertArrayEquals(new String[]{"updated"}, singlePage.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void bookPagesAlsoReadWritableBookContent() {
        ItemStack writable = new ItemStack(Items.WRITABLE_BOOK);
        writable.set(DataComponents.WRITABLE_BOOK_CONTENT,
                new WritableBookContent(List.of(Filterable.passThrough("draft"), Filterable.passThrough("note"))));

        ExprBookPages pages = new ExprBookPages();
        pages.init(new Expression[]{new SimpleLiteral<>(writable, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertArrayEquals(new String[]{"draft", "note"}, pages.getArray(SkriptEvent.EMPTY));
    }

    @Test
    void customModelDataSupportsLegacyValueAndTypedLists() {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(7.0F), List.of(true), List.of("flag"), List.of(0x123456)));

        ExprCustomModelData legacy = new ExprCustomModelData();
        legacy.init(new Expression[]{new SimpleLiteral<>(stack, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(7, legacy.getSingle(SkriptEvent.EMPTY));

        ExprCustomModelData colors = new ExprCustomModelData();
        SkriptParser.ParseResult colorParse = parseResult("");
        colorParse.mark = 4;
        colors.init(new Expression[]{new SimpleLiteral<>(stack, false)}, 0, Kleenean.FALSE, colorParse);
        assertInstanceOf(ColorRGB.class, colors.getSingle(SkriptEvent.EMPTY));

        ExprCustomModelData full = new ExprCustomModelData();
        SkriptParser.ParseResult fullParse = parseResult("");
        fullParse.mark = 5;
        full.init(new Expression[]{new SimpleLiteral<>(stack, false)}, 0, Kleenean.FALSE, fullParse);
        full.change(SkriptEvent.EMPTY, new Object[]{2.5F, false, "blade", new ColorRGB(1, 2, 3)},
                ch.njol.skript.classes.Changer.ChangeMode.SET);

        CustomModelData updated = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        assertEquals(List.of(2.5F), updated.floats());
        assertEquals(List.of(false), updated.flags());
        assertEquals(List.of("blade"), updated.strings());
        assertEquals(List.of(0x010203), updated.colors());
    }

    @Test
    void importedItemExpressionsPreservePrototypeComponents() {
        ExprItemWithLore lore = new ExprItemWithLore();
        lore.init(new Expression[]{
                new SimpleLiteral<>(new FabricItemType(Items.STICK), false),
                new SimpleLiteral<>("line 1\nline 2", false)
        }, 0, Kleenean.FALSE, parseResult(""));
        FabricItemType loreItem = lore.getSingle(SkriptEvent.EMPTY);
        assertEquals(List.of(Component.literal("line 1"), Component.literal("line 2")),
                loreItem.toStack().get(DataComponents.LORE).lines());

        ExprItemWithCustomModelData customModelData = new ExprItemWithCustomModelData();
        ExpressionList<Object> customModelDataValues = new ExpressionList<>(
                new Expression[]{
                        new SimpleLiteral<>(2.5F, false),
                        new SimpleLiteral<>(false, false),
                        new SimpleLiteral<>("blade", false),
                        new SimpleLiteral<>(new ColorRGB(1, 2, 3), false)
                },
                Object.class,
                true
        );
        customModelData.init(new Expression[]{
                new SimpleLiteral<>(new FabricItemType(Items.STICK), false),
                customModelDataValues
        }, 0, Kleenean.FALSE, parseResult(""));
        FabricItemType modelItem = customModelData.getSingle(SkriptEvent.EMPTY);
        CustomModelData data = modelItem.toStack().get(DataComponents.CUSTOM_MODEL_DATA);
        assertEquals(List.of(2.5F), data.floats());
        assertEquals(List.of(false), data.flags());
        assertEquals(List.of("blade"), data.strings());
        assertEquals(List.of(0x010203), data.colors());

        ExprItemWithEnchantmentGlint glint = new ExprItemWithEnchantmentGlint();
        glint.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STICK), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(Boolean.TRUE, glint.getSingle(SkriptEvent.EMPTY).toStack().get(DataComponents.ENCHANTMENT_GLINT_OVERRIDE));

        ExprItemWithTooltip tooltip = new ExprItemWithTooltip();
        SkriptParser.ParseResult tooltipParse = parseResult("");
        tooltipParse.tags.add("out");
        tooltip.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STICK), false)}, 0, Kleenean.FALSE, tooltipParse);
        TooltipDisplay tooltipDisplay = tooltip.getSingle(SkriptEvent.EMPTY).toStack().get(DataComponents.TOOLTIP_DISPLAY);
        assertTrue(tooltipDisplay.hideTooltip());
    }

    @Test
    void itemFlagsReadAndMutateTooltipHiddenComponents() {
        FabricItemType item = new FabricItemType(new ItemStack(Items.DIAMOND_SWORD));
        ExprItemFlags flags = new ExprItemFlags();
        flags.init(new Expression[]{new SimpleLiteral<>(item, false)}, 0, Kleenean.FALSE, parseResult(""));

        flags.change(SkriptEvent.EMPTY, new Object[]{"hide enchants", "hide unbreakable"}, ch.njol.skript.classes.Changer.ChangeMode.ADD);
        assertEquals(Set.of("HIDE_ENCHANTS", "HIDE_UNBREAKABLE"), Set.of(flags.getArray(SkriptEvent.EMPTY)));

        TooltipDisplay display = item.toStack().get(DataComponents.TOOLTIP_DISPLAY);
        assertTrue(display.hiddenComponents().contains(DataComponents.ENCHANTMENTS));
        assertTrue(display.hiddenComponents().contains(DataComponents.UNBREAKABLE));

        flags.change(SkriptEvent.EMPTY, new Object[]{"hide enchants"}, ch.njol.skript.classes.Changer.ChangeMode.REMOVE);
        assertEquals(Set.of("HIDE_UNBREAKABLE"), Set.of(flags.getArray(SkriptEvent.EMPTY)));

        flags.change(SkriptEvent.EMPTY, null, ch.njol.skript.classes.Changer.ChangeMode.DELETE);
        assertEquals(0, flags.getArray(SkriptEvent.EMPTY).length);
    }

    @Test
    void itemAmountAndInventoryCountExpressionsHandleCompatTypes() {
        FabricItemType stackType = new FabricItemType(Items.STICK, 3, null);
        ExprItemAmount amount = new ExprItemAmount();
        amount.init(new Expression[]{new SimpleLiteral<>(stackType, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(3L, amount.getSingle(SkriptEvent.EMPTY));
        amount.change(SkriptEvent.EMPTY, new Object[]{2L}, ch.njol.skript.classes.Changer.ChangeMode.ADD);
        assertEquals(5, stackType.amount());

        SimpleContainer container = new SimpleContainer(2);
        container.setItem(0, new ItemStack(Items.STICK, 4));
        container.setItem(1, new ItemStack(Items.DIRT, 2));
        ExprAmountOfItems inventoryCount = new ExprAmountOfItems();
        inventoryCount.init(new Expression[]{
                new SimpleLiteral<>(new FabricItemType(Items.STICK), false),
                new SimpleLiteral<>(new FabricInventory(container), false)
        }, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(4L, inventoryCount.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void itemsExpressionReturnsBlocksAndTypedItems() {
        ExprItems blocks = new ExprItems();
        blocks.init(new Expression[0], 0, Kleenean.FALSE, parseResult(""));
        FabricItemType[] allBlocks = blocks.getArray(SkriptEvent.EMPTY);
        assertTrue(Arrays.stream(allBlocks).anyMatch(item -> item.item() == Items.STONE));
        assertFalse(Arrays.stream(allBlocks).anyMatch(item -> item.item() == Items.STICK));

        ExprItems items = new ExprItems();
        items.init(new Expression[]{new SimpleLiteral<>(new FabricItemType(Items.STICK), false)}, 3, Kleenean.FALSE, parseResult(""));
        FabricItemType[] resolved = items.getArray(SkriptEvent.EMPTY);
        assertEquals(1, resolved.length);
        assertEquals(Items.STICK, resolved[0].item());
    }

    @Test
    void damagedItemAndDurabilityHandleItemStacksAndSlots() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

        ExprDamagedItem damaged = new ExprDamagedItem();
        damaged.init(new Expression[]{
                new SimpleLiteral<>(sword, false),
                new SimpleLiteral<>(7, false)
        }, 0, Kleenean.FALSE, parseResult(""));
        ItemStack damagedSword = damaged.getSingle(SkriptEvent.EMPTY);
        assertEquals(7, damagedSword.getDamageValue());
        assertEquals(0, sword.getDamageValue());

        ExprDurability damageValue = new ExprDurability();
        damageValue.init(new Expression[]{new SimpleLiteral<>(damagedSword, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(7, damageValue.getSingle(SkriptEvent.EMPTY));

        ExprDurability remaining = new ExprDurability();
        SkriptParser.ParseResult durabilityParse = parseResult("");
        durabilityParse.mark = 1;
        remaining.init(new Expression[]{new SimpleLiteral<>(damagedSword, false)}, 0, Kleenean.FALSE, durabilityParse);
        assertEquals(damagedSword.getMaxDamage() - 7, remaining.getSingle(SkriptEvent.EMPTY));

        SimpleContainer container = new SimpleContainer(1);
        Slot slot = new Slot(container, 0, 0, 0);
        slot.set(new ItemStack(Items.DIAMOND_SWORD));
        ExprDurability slotDamage = new ExprDurability();
        slotDamage.init(new Expression[]{new SimpleLiteral<>(slot, false)}, 0, Kleenean.FALSE, parseResult(""));
        slotDamage.change(SkriptEvent.EMPTY, new Object[]{12}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals(12, slot.getItem().getDamageValue());
    }

    @Test
    void rawNameMaxStackAndMaxDurabilityOperateOnCompatItemTypes() {
        FabricItemType diamondSword = new FabricItemType(Items.DIAMOND_SWORD);

        ExprRawName rawName = new ExprRawName();
        rawName.init(new Expression[]{new SimpleLiteral<>(diamondSword, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(diamondSword.itemId(), rawName.getSingle(SkriptEvent.EMPTY));

        ExprMaxDurability maxDurability = new ExprMaxDurability();
        maxDurability.init(new Expression[]{new SimpleLiteral<>(diamondSword, false)}, 0, Kleenean.FALSE, parseResult(""));
        Integer originalDurability = maxDurability.getSingle(SkriptEvent.EMPTY);
        assertTrue(originalDurability != null && originalDurability > 0);
        maxDurability.change(SkriptEvent.EMPTY, new Object[]{originalDurability + 12}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals(originalDurability + 12, maxDurability.getSingle(SkriptEvent.EMPTY));

        ExprMaxStack maxStack = new ExprMaxStack();
        FabricItemType sticks = new FabricItemType(Items.STICK);
        maxStack.init(new Expression[]{new SimpleLiteral<>(sticks, false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(64, maxStack.getSingle(SkriptEvent.EMPTY));
        maxStack.change(SkriptEvent.EMPTY, new Object[]{16}, ch.njol.skript.classes.Changer.ChangeMode.SET);
        assertEquals(16, maxStack.getSingle(SkriptEvent.EMPTY));
        maxStack.change(SkriptEvent.EMPTY, null, ch.njol.skript.classes.Changer.ChangeMode.RESET);
        assertEquals(64, maxStack.getSingle(SkriptEvent.EMPTY));
    }

    @Test
    void maxItemUseTimeAndInventoryStackSizeReadCurrentValues() {
        ExprMaxItemUseTime maxUseTime = new ExprMaxItemUseTime();
        maxUseTime.init(new Expression[]{new SimpleLiteral<>(new ItemStack(Items.POTION), false)}, 0, Kleenean.FALSE, parseResult(""));
        Timespan duration = maxUseTime.getSingle(SkriptEvent.EMPTY);
        assertTrue(duration != null && duration.getAs(Timespan.TimePeriod.TICK) > 0);

        SimpleContainer container = new SimpleContainer(3);
        ExprMaxStack maxStack = new ExprMaxStack();
        maxStack.init(new Expression[]{new SimpleLiteral<>(new FabricInventory(container), false)}, 0, Kleenean.FALSE, parseResult(""));
        assertEquals(container.getMaxStackSize(), maxStack.getSingle(SkriptEvent.EMPTY));
        assertNull(maxStack.acceptChange(ch.njol.skript.classes.Changer.ChangeMode.SET));
    }

    @Test
    void importedExpressionsInstantiate() {
        assertDoesNotThrow(ExprAmountOfItems::new);
        assertDoesNotThrow(ExprBookAuthor::new);
        assertDoesNotThrow(ExprBookPages::new);
        assertDoesNotThrow(ExprBookTitle::new);
        assertDoesNotThrow(ExprBlockHardness::new);
        assertDoesNotThrow(ExprBrushableItem::new);
        assertDoesNotThrow(ExprCharges::new);
        assertDoesNotThrow(ExprCustomModelData::new);
        assertDoesNotThrow(ExprDamagedItem::new);
        assertDoesNotThrow(ExprDurability::new);
        assertDoesNotThrow(ExprEgg::new);
        assertDoesNotThrow(ExprExactItem::new);
        assertDoesNotThrow(ExprItem::new);
        assertDoesNotThrow(ExprItemAmount::new);
        assertDoesNotThrow(ExprItemOwner::new);
        assertDoesNotThrow(ExprItemThrower::new);
        assertDoesNotThrow(ExprItems::new);
        assertDoesNotThrow(ExprLevel::new);
        assertDoesNotThrow(ExprMaxDurability::new);
        assertDoesNotThrow(ExprMaxHealth::new);
        assertDoesNotThrow(ExprMaxItemUseTime::new);
        assertDoesNotThrow(ExprMaxStack::new);
        assertDoesNotThrow(ExprNoDamageTicks::new);
        assertDoesNotThrow(ExprRawName::new);
        assertDoesNotThrow(ExprSpeed::new);
        assertDoesNotThrow(ExprItemWithCustomModelData::new);
        assertDoesNotThrow(ExprItemWithEnchantmentGlint::new);
        assertDoesNotThrow(ExprItemWithLore::new);
        assertDoesNotThrow(ExprItemWithTooltip::new);
        assertTrue(true);
    }

    private static ItemStack writtenBook(String title, String author, String... pages) {
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
        stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough(title),
                author,
                0,
                Arrays.stream(pages).<Filterable<Component>>map(page -> Filterable.passThrough(Component.literal(page))).toList(),
                false
        ));
        return stack;
    }

    private static SkriptParser.ParseResult parseResult(String expr) {
        SkriptParser.ParseResult result = new SkriptParser.ParseResult();
        result.expr = expr;
        return result;
    }
}
