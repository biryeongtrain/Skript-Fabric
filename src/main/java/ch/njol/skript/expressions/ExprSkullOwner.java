package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.fabric.compat.FabricItemType;
import org.skriptlang.skript.lang.event.SkriptEvent;

@Name("Skull Owner")
@Description("The owner profile of a player head item.")
@Example("set {_owner} to the skull owner of player's tool")
@Example("set skull owner of {_head} to {_offline player}")
@Since("2.9.0, Fabric")
public final class ExprSkullOwner extends SimplePropertyExpression<Object, GameProfile> {

    static {
        register(ExprSkullOwner.class, GameProfile.class, "(head|skull) owner", "slots/itemtypes/itemstacks/blocks");
    }

    @Override
    public @Nullable GameProfile convert(Object object) {
        ItemStack stack = asItemStack(object);
        if (stack == null || !stack.is(Items.PLAYER_HEAD)) {
            return null;
        }
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        return profile == null ? null : profile.partialProfile();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{GameProfile.class} : null;
    }

    @Override
    public void change(SkriptEvent event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0 || !(delta[0] instanceof GameProfile profile)) {
            return;
        }
        for (Object object : getExpr().getArray(event)) {
            ItemStack stack = asItemStack(object);
            if (stack == null || !stack.is(Items.PLAYER_HEAD)) {
                continue;
            }
            ItemStack updated = stack.copy();
            updated.set(DataComponents.PROFILE, ResolvableProfile.createResolved(profile));
            applyItemStack(object, updated);
        }
    }

    private @Nullable ItemStack asItemStack(Object object) {
        if (object instanceof ItemStack stack) {
            return stack;
        }
        if (object instanceof Slot slot) {
            return slot.getItem();
        }
        if (object instanceof FabricItemType itemType) {
            return itemType.toStack();
        }
        return null;
    }

    private void applyItemStack(Object object, ItemStack updated) {
        if (object instanceof Slot slot) {
            slot.set(updated);
        } else if (object instanceof ItemStack direct) {
            direct.applyComponents(updated.getComponents());
            direct.setCount(updated.getCount());
        } else if (object instanceof FabricItemType itemType) {
            itemType.applyPrototype(updated);
        }
    }

    @Override
    public Class<? extends GameProfile> getReturnType() {
        return GameProfile.class;
    }

    @Override
    protected String getPropertyName() {
        return "skull owner";
    }
}
