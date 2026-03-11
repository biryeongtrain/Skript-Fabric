package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

@Name("Slot Index")
@Description({
        "Index of an inventory slot.",
        "The raw index is the unique menu position for the slot, while the regular index is the container slot index."
})
@Example("if index of event-slot is 10:\n\tsend \"You bought a pie!\"")
@Since("2.2-dev35, 2.8.0 (raw index)")
public class ExprSlotIndex extends SimplePropertyExpression<Slot, Long> {

    static {
        register(ExprSlotIndex.class, Long.class, "[raw:(raw|unique)] index", "slots");
    }

    private boolean raw;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        raw = parseResult.hasTag("raw");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Long convert(Slot slot) {
        return (long) (raw ? slot.index : slot.getContainerSlot());
    }

    @Override
    public Class<? extends Long> getReturnType() {
        return Long.class;
    }

    @Override
    protected String getPropertyName() {
        return raw ? "raw index" : "slot";
    }
}
