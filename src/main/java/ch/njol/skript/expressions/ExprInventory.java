package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;

@Name("Inventory")
@Description("The inventory of a player or inventory object.")
@Example("clear the player's inventory")
@Example("remove 5 wool from the inventory of player")
@Since("1.0")
public final class ExprInventory extends SimplePropertyExpression<Object, FabricInventory> {

    @Override
    public @Nullable FabricInventory convert(Object from) {
        if (from instanceof FabricInventory inventory) {
            return inventory;
        }
        if (from instanceof ServerPlayer player) {
            return new FabricInventory(player.getInventory(), MenuType.GENERIC_9x5, player.getName(), player);
        }
        return null;
    }

    @Override
    public Class<? extends FabricInventory> getReturnType() {
        return FabricInventory.class;
    }

    @Override
    protected String getPropertyName() {
        return "inventory";
    }
}
