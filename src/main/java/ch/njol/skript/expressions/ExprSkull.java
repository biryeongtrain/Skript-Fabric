package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;

@Name("Player Skull")
@Description("Gets a player head item representing an offline player.")
@Example("give the victim's skull to the attacker")
@Example("set the block at the entity to the entity's skull")
@Since("2.0, Fabric")
public class ExprSkull extends SimplePropertyExpression<GameProfile, FabricItemType> {

    static {
        register(ExprSkull.class, FabricItemType.class, "skull", "offlineplayers");
    }

    @Override
    public @Nullable FabricItemType convert(GameProfile profile) {
        ItemStack skull = new ItemStack(Items.PLAYER_HEAD);
        skull.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        return new FabricItemType(skull);
    }

    @Override
    public Class<? extends FabricItemType> getReturnType() {
        return FabricItemType.class;
    }

    @Override
    protected String getPropertyName() {
        return "skull";
    }
}
