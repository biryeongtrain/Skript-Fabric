package ch.njol.skript.expressions;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.List;
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
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;
import org.skriptlang.skript.registration.SyntaxRegistry;

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
    void importedExpressionsInstantiate() {
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
