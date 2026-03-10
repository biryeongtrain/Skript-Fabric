package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Anvil Repair Cost")
@Description({
        "Returns the current or maximum repair cost of an anvil inventory when the backing holder exposes that state.",
        "This remains import-only until a runtime anvil state producer is wired."
})
@Example("set max repair cost of {_anvil} to 40")
@Since("2.8.0")
public final class ExprAnvilRepairCost extends SimplePropertyExpression<FabricInventory, Integer> {

    static {
        registerDefault(ExprAnvilRepairCost.class, Integer.class, "[anvil] [item] [:max[imum]] repair cost", "inventories");
    }

    private boolean maximum;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        maximum = parseResult.hasTag("max");
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Integer convert(FabricInventory inventory) {
        if (inventory.menuType() != net.minecraft.world.inventory.MenuType.ANVIL) {
            return null;
        }
        Object value = ReflectiveHandleAccess.invokeNoArg(
                inventory.holder(),
                maximum ? "maximumRepairCost" : "repairCost",
                maximum ? "getMaximumRepairCost" : "getRepairCost"
        );
        return value instanceof Number number ? number.intValue() : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof Number number)) {
            return;
        }
        for (FabricInventory inventory : getExpr().getArray(event)) {
            if (inventory.menuType() != net.minecraft.world.inventory.MenuType.ANVIL) {
                continue;
            }
            Integer current = convert(inventory);
            int base = mode == ChangeMode.SET || current == null ? 0 : current;
            int amount = number.intValue() * (mode == ChangeMode.REMOVE ? -1 : 1);
            int next = Math.max(0, base + amount);
            String setter = maximum ? "setMaximumRepairCost" : "setRepairCost";
            ReflectiveHandleAccess.invokeSingleArg(inventory.holder(), setter, next);
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return maximum ? "maximum anvil repair cost" : "anvil repair cost";
    }
}
