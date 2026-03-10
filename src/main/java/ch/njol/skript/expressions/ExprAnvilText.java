package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;

@Name("Anvil Text Input")
@Description("Gets the current text input of an anvil inventory when the backing holder exposes it.")
@Example("if anvil text input of {_anvil} is \"rename me\":")
@Since("2.7")
public final class ExprAnvilText extends SimplePropertyExpression<FabricInventory, String> {

    static {
        register(ExprAnvilText.class, String.class, "anvil [inventory] (rename|text) input", "inventories");
    }

    @Override
    public @Nullable String convert(FabricInventory inventory) {
        if (inventory.menuType() != net.minecraft.world.inventory.MenuType.ANVIL) {
            return null;
        }
        Object value = ReflectiveHandleAccess.invokeNoArg(
                inventory.holder(),
                "renameText",
                "textInput",
                "text",
                "getRenameText",
                "getTextInput"
        );
        return value != null ? String.valueOf(value) : null;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    protected String getPropertyName() {
        return "anvil text input";
    }
}
