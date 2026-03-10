package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Item with Lore")
@Description({
    "Returns the given item type with the specified lore added to it.",
    "If multiple strings are passed, each of them will be a separate line in the lore."
})
@Example("""
        set {_test} to stone with lore "line 1" and "line 2"
        give {_test} to player
    """)
@Since("2.3")
public class ExprItemWithLore extends PropertyExpression<FabricItemType, FabricItemType> {

    static {
        Skript.registerExpression(ExprItemWithLore.class, FabricItemType.class,
                "%itemtype% with [(a|the)] lore %strings%");
    }

    private Expression<String> lore;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        setExpr((Expression<FabricItemType>) exprs[0]);
        lore = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    protected FabricItemType[] get(SkriptEvent event, FabricItemType[] source) {
        List<Component> lines = new ArrayList<>();
        for (String value : lore.getArray(event)) {
            Arrays.stream(value.split("\n")).map(Component::literal).forEach(lines::add);
        }
        return get(source, item -> {
            ItemStack stack = item.toStack();
            stack.set(DataComponents.LORE, new ItemLore(lines));
            return new FabricItemType(stack);
        });
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        return getExpr().toString(event, debug) + " with lore " + lore.toString(event, debug);
    }
}
