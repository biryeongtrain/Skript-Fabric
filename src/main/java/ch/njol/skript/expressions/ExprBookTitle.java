package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Book Title")
@Description("The title of a book.")
@Example("""
	on book sign:
		message "Book Title: %title of event-item%"
	""")
@Since("2.2-dev31")
public class ExprBookTitle extends SimplePropertyExpression<ItemStack, String> {

    static {
        register(ExprBookTitle.class, String.class, "book (name|title)", "itemstacks");
    }

    @Override
    public @Nullable String convert(ItemStack item) {
        WrittenBookContent content = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
        return content == null ? null : content.title().raw();
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
        String title = delta == null || delta.length == 0 ? "" : String.valueOf(delta[0]);
        for (ItemStack item : getExpr().getArray(event)) {
            WrittenBookContent content = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (content == null) {
                continue;
            }
            item.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                    Filterable.passThrough(title),
                    content.author(),
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
        return "book title";
    }
}
