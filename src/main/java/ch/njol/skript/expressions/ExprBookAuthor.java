package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Book Author")
@Description("The author of a book.")
@Example("""
	on book sign:
		message "Book Title: %author of event-item%"
	""")
@Since("2.2-dev31")
public class ExprBookAuthor extends SimplePropertyExpression<ItemStack, String> {

    static {
        register(ExprBookAuthor.class, String.class, "[book] (author|writer|publisher)", "itemstacks");
    }

    @Override
    public @Nullable String convert(ItemStack item) {
        WrittenBookContent content = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content == null ? null : content.author();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{String.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        String author = delta == null || delta.length == 0 ? null : String.valueOf(delta[0]);
        for (ItemStack item : getExpr().getArray(event)) {
            WrittenBookContent content = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                continue;
            }
            item.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                    content.title(),
                    author == null ? "" : author,
                    content.generation(),
                    content.pages(),
                    content.resolved()
            ));
        }
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "book author";
    }
}
