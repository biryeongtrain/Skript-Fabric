package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.fabric.placeholder.SkriptTextPlaceholders;
import org.skriptlang.skript.lang.event.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;

@Name("Custom Chest Inventory")
@Description("Returns a chest inventory with the given amount of rows and optional title.")
@Example("open chest inventory with 1 row named \"test\" to player")
@Example("set {_inventory} to a chest inventory named \"Menu\" with 3 rows")
@Since("2.2-dev34")
public final class ExprChestInventory extends SimpleExpression<FabricInventory> {

    private static final int DEFAULT_ROWS = 3;

    private @Nullable Expression<Number> rows;
    private @Nullable Expression<String> title;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        title = (Expression<String>) exprs[matchedPattern];
        rows = (Expression<Number>) exprs[matchedPattern ^ 1];
        return true;
    }

    @Override
    protected FabricInventory @Nullable [] get(SkriptEvent event) {
        int resolvedRows = rows != null ? rows.getOptionalSingle(event).orElse(DEFAULT_ROWS).intValue() : DEFAULT_ROWS;
        resolvedRows = Math.clamp(resolvedRows, 1, 6);
        String resolvedTitle = title != null ? title.getOptionalSingle(event).orElse("") : "";
        Component component = resolvedTitle.isBlank()
                ? Component.translatable("container.chest")
                : SkriptTextPlaceholders.resolveComponent(resolvedTitle, event);
        return new FabricInventory[]{FabricInventory.chest(resolvedRows, component)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends FabricInventory> getReturnType() {
        return FabricInventory.class;
    }

    @Override
    public String toString(@Nullable SkriptEvent event, boolean debug) {
        String titleText = title != null ? title.toString(event, debug) : "\"Chest\"";
        String rowsText = rows != null ? rows.toString(event, debug) : Integer.toString(DEFAULT_ROWS);
        return "chest inventory named " + titleText + " with " + rowsText + " rows";
    }
}
