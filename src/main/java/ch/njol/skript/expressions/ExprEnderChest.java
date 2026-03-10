package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricInventory;

@Name("Ender Chest")
@Description("The ender chest of a player.")
@Example("open the player's ender chest to the player")
@Since("2.0")
public final class ExprEnderChest extends SimplePropertyExpression<ServerPlayer, FabricInventory> {

    @Override
    public @Nullable FabricInventory convert(ServerPlayer player) {
        return new FabricInventory(
                player.getEnderChestInventory(),
                MenuType.GENERIC_9x3,
                Component.translatable("container.enderchest"),
                player
        );
    }

    @Override
    public Class<? extends FabricInventory> getReturnType() {
        return FabricInventory.class;
    }

    @Override
    protected String getPropertyName() {
        return "ender chest";
    }
}
