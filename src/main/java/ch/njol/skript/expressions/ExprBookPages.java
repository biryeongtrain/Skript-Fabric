package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Book Pages")
@Description({
    "The pages of a book (Supports Skript's chat format)",
    "Note: In order to modify the pages of a new written book, you must have the title and author",
    "of the book set. Skript will do this for you, but if you want your own, please set those values."
})
@Example("""
    on book sign:
    	message "Book Pages: %pages of event-item%"
    	message "Book Page 1: %page 1 of event-item%"
    """)
@Example("set page 1 of player's held item to \"Book writing\"")
@Since("2.2-dev31, 2.7 (changers)")
public class ExprBookPages extends SimpleExpression<String> {

    static {
        ch.njol.skript.Skript.registerExpression(ExprBookPages.class, String.class,
                "[all [[of] the]|the] [book] (pages|content) of %itemstacks%",
                "%itemstacks%'[s] [book] (pages|content)",
                "[book] page %number% of %itemstacks%",
                "%itemstacks%'[s] [book] page %number%"
        );
    }

    private Expression<ItemStack> items;
    private @Nullable Expression<Number> page;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (matchedPattern < 2) {
            items = (Expression<ItemStack>) exprs[0];
        } else if (matchedPattern == 2) {
            page = (Expression<Number>) exprs[0];
            items = (Expression<ItemStack>) exprs[1];
        } else {
            items = (Expression<ItemStack>) exprs[0];
            page = (Expression<Number>) exprs[1];
        }
        return true;
    }

    @Override
    protected String @Nullable [] get(SkriptEvent event) {
        List<String> results = new ArrayList<>();
        for (ItemStack item : items.getArray(event)) {
            List<String> pages = readPages(item);
            if (isAllPages()) {
                results.addAll(pages);
                continue;
            }
            Number pageNumber = page == null ? null : page.getSingle(event);
            if (pageNumber == null) {
                continue;
            }
            int index = pageNumber.intValue() - 1;
            if (index >= 0 && index < pages.size()) {
                results.add(pages.get(index));
            }
        }
        return results.toArray(String[]::new);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case RESET, DELETE -> new Class[0];
            case SET -> new Class[]{isAllPages() ? String[].class : String.class};
            case ADD -> isAllPages() ? new Class[]{String[].class} : null;
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        int targetPage = !isAllPages() && page != null ? page.getOptionalSingle(event).orElse(-1).intValue() : -1;
        String[] incoming = delta == null ? new String[0] : Arrays.stream(delta).map(String::valueOf).toArray(String[]::new);
        for (ItemStack item : items.getArray(event)) {
            List<String> pages = new ArrayList<>(readPages(item));
            if (isAllPages()) {
                switch (mode) {
                    case DELETE, RESET -> pages = new ArrayList<>(Collections.singletonList(""));
                    case SET -> pages = new ArrayList<>(Arrays.asList(incoming));
                    case ADD -> pages.addAll(Arrays.asList(incoming));
                    default -> {
                    }
                }
            } else {
                switch (mode) {
                    case DELETE, RESET -> {
                        if (targetPage > 0 && targetPage <= pages.size()) {
                            pages.remove(targetPage - 1);
                        }
                    }
                    case SET -> {
                        if (incoming.length == 0 || targetPage <= 0) {
                            continue;
                        }
                        while (pages.size() < targetPage) {
                            pages.add("");
                        }
                        pages.set(targetPage - 1, incoming[0]);
                    }
                    default -> {
                    }
                }
            }
            writePages(item, pages);
        }
    }

    @Override
    public boolean isSingle() {
        return !isAllPages() && items.isSingle();
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return isAllPages()
                ? "pages of " + items.toString(event, debug)
                : "page " + (page == null ? "?" : page.toString(event, debug)) + " of " + items.toString(event, debug);
    }

    private boolean isAllPages() {
        return page == null;
    }

    private static List<String> readPages(ItemStack item) {
        WrittenBookContent written = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null) {
            return written.getPages(false).stream().map(Component::getString).toList();
        }
        WritableBookContent writable = item.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writable != null) {
            return writable.getPages(false).toList();
        }
        return List.of();
    }

    private static void writePages(ItemStack item, List<String> pages) {
        WrittenBookContent written = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (written != null) {
            item.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                    written.title(),
                    written.author(),
                    written.generation(),
                    pages.stream()
                            .<Filterable<Component>>map(page -> Filterable.passThrough(Component.literal(page)))
                            .toList(),
                    written.resolved()
            ));
            return;
        }
        WritableBookContent writable = item.get(DataComponents.WRITABLE_BOOK_CONTENT);
        if (writable != null) {
            item.set(DataComponents.WRITABLE_BOOK_CONTENT,
                    new WritableBookContent(pages.stream().map(Filterable::passThrough).toList()));
        }
    }
}
